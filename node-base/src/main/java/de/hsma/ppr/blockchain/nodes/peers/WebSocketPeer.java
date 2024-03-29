package de.hsma.ppr.blockchain.nodes.peers;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.messages.WebSocketRequestMessage;
import org.whispersystems.websocket.messages.WebSocketResponseMessage;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.exception.ByteConversionFailedExcetion;
import de.hsma.ppr.blockchain.nodes.peers.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WebSocketInterface;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;

public class WebSocketPeer
{
	private static final Logger logger = LoggerFactory.getLogger(WebSocketPeer.class);

	private final WebSocketInterface	webSocketInterface;
	private final Peer								target;

	public WebSocketPeer(WebSocketInterface webSocketInterface, Peer target)
	{
		this.target = target;
		this.webSocketInterface = webSocketInterface;
		webSocketInterface.setListener(new WebSocketInterface.Listener() {
			@Override
			public void onReceivedRequest(WebSocketRequestMessage requestMessage)
			{

			}

			@Override
			public void onReceivedResponse(WebSocketResponseMessage responseMessage)
			{

			}
		});
	}

	public static WebSocketPeer connect(Peer peer) throws WsConnectException
	{
		return new WebSocketPeer(WebSocketInterface.connect(peer.getAddress()), peer);
	}

	public void broadCastNewBlock(BlockChain blockChain)
	{
		logger.info("Broadcasting {} to {}", blockChain.lastBlock(), target);
		webSocketInterface.sendRequest("PUT", "/ws/blockChain", BlockChain.toJsonByteArray(blockChain));
	}

	public void broadCastNewPeer(Peer peer)
	{
		logger.info("Broadcasting {} to {}", peer, target);
		webSocketInterface.sendRequest("POST", "/ws/peers", peer.asJsonByteArray());
	}

	public void broadCastNewData(Map<String, String> data)
	{
		logger.info("Broadcasting {} to {}", data, target);
		webSocketInterface.sendRequest("POST", "/ws/data", toJsonByteArray(data));
	}

	private byte[] toJsonByteArray(Map<String, String> data)
	{
		try
		{
			return new ObjectMapper().writeValueAsBytes(data);
		} catch (JsonProcessingException e)
		{
			throw new ByteConversionFailedExcetion(e);
		}
	}
}
