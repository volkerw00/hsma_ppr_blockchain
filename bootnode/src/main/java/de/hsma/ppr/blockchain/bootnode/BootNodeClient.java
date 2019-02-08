package de.hsma.ppr.blockchain.bootnode;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.messages.WebSocketRequestMessage;
import org.whispersystems.websocket.messages.WebSocketResponseMessage;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.exception.BlockChainNotValidException;
import de.hsma.ppr.blockchain.nodes.async.FutureCallback;
import de.hsma.ppr.blockchain.nodes.async.Futures;
import de.hsma.ppr.blockchain.nodes.peers.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WebSocketInterface;
import de.hsma.ppr.blockchain.nodes.ws.WebSocketInterface.WebSocketListener;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;
import de.hsma.ppr.blockchain.nodes.ws.WsRuntimeException;

public class BootNodeClient
{
	private static final Logger	logger	= LoggerFactory.getLogger(BootNodeClient.class);
	private String							localAddress;

	public class BootNodeWebSocketListener extends WebSocketListener
	{
		private final WebSocketInterface webSocket;

		public BootNodeWebSocketListener(WebSocketInterface webSocket)
		{
			this.webSocket = webSocket;
		}

		@Override
		public void onReceivedRequest(WebSocketRequestMessage requestMessage)
		{
			super.onReceivedRequest(requestMessage);
			if ("GET".equals(requestMessage.getVerb()) && "/peerAddress".equals(requestMessage.getPath()))
			{
				try
				{
					byte[] body = Peer.forAddress(localAddress).asJsonByteArray();
					webSocket.sendResponse(requestMessage.getRequestId(), 200, "OK", body);
				} catch (IOException e)
				{
					throw new WsRuntimeException(String.format("Failed to send response for request with id %s",
					                                           requestMessage.getRequestId()),
					                             e);
				}
			} else
			{
				try
				{
					webSocket.sendResponse(requestMessage.getRequestId(), 404, "NOT FOUND");
				} catch (IOException e)
				{
					throw new WsRuntimeException(String.format("Failed to send response for request with id %s",
					                                           requestMessage.getRequestId()),
					                             e);
				}
			}
		}
	}

	private final String				bootNode;
	private WebSocketInterface	webSocket;

	public BootNodeClient(String bootNode)
	{
		this.bootNode = bootNode;
	}

	public static BootNodeClient connect(String bootNode) throws WsConnectException
	{
		return new BootNodeClient(bootNode).connect();
	}

	private BootNodeClient connect() throws WsConnectException
	{
		webSocket = WebSocketInterface.connect(this.bootNode);
		webSocket.setListener(new BootNodeWebSocketListener(webSocket));
		return this;
	}

	public ListenableFuture<Boolean> hello(String localAddress)
	{
		this.localAddress = localAddress;
		logger.debug("Sending hello...");

		byte[] body = Peer.forAddress(localAddress).asJsonByteArray();
		ListenableFuture<WebSocketResponseMessage> response = webSocket.sendRequest("POST", "/boot/ws/peers", body);

		SettableFuture<Boolean> future = SettableFuture.create();

		Futures.addCallback(response, FutureCallback.<WebSocketResponseMessage>callback().onSuccess(r -> {
			if (r.getStatus() == 200)
			{
				logger.trace("Successful hello.");
				future.set(Boolean.TRUE);
			} else
			{
				logger.trace("Hello unsuccessful.");
				future.set(Boolean.FALSE);
			}
		}).onFailure(t -> {
			logger.error("Hello failed.");
			future.setException(t);
		}));

		return future;
	}

	public ListenableFuture<Set<Peer>> getPeers()
	{
		ListenableFuture<WebSocketResponseMessage> response = webSocket.sendRequest("GET", "/boot/ws/peers");
		SettableFuture<Set<Peer>> future = SettableFuture.create();

		Futures.addCallback(response, FutureCallback.<WebSocketResponseMessage>callback().onSuccess(result -> {
			if (result.getStatus() == 200 && result.getBody().isPresent())
			{
				byte[] body = result.getBody().get();
				Set<Peer> peers = Peer.fromJsonArray(body);
				peers = removeSelf(peers);
				logger.info("Fetched peers: {}", peers.toString());
				future.set(peers);
			}
		}));
		return future;
	}

	private Set<Peer> removeSelf(Set<Peer> peers)
	{
		return peers.stream().filter(peer -> !Objects.equals(peer.getAddress(), localAddress)).collect(toSet());
	}

	public ListenableFuture<BlockChain> getBlockChain()
	{
		ListenableFuture<WebSocketResponseMessage> response = webSocket.sendRequest("GET", "/ws/blockChain");
		SettableFuture<BlockChain> future = SettableFuture.create();

		Futures.addCallback(response, FutureCallback.<WebSocketResponseMessage>callback().onSuccess(result -> {
			if (result.getStatus() == 200 && result.getBody().isPresent())
			{
				byte[] body = result.getBody().get();
				BlockChain blockChain;
				try
				{
					blockChain = BlockChain.fromJsonArray(body);
					logger.info("Fetched Blockchain ending on block: {}", blockChain.lastBlock().hash());
					future.set(blockChain);
				} catch (BlockChainNotValidException e)
				{
					future.setException(e);
				}
			}
		}));

		return future;
	}
}
