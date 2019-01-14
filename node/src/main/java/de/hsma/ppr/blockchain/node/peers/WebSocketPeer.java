package de.hsma.ppr.blockchain.node.peers;

import org.whispersystems.websocket.messages.WebSocketRequestMessage;
import org.whispersystems.websocket.messages.WebSocketResponseMessage;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.nodes.resource.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WebSocketInterface;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;

public class WebSocketPeer
{

	private final WebSocketInterface webSocketInterface;

	public WebSocketPeer(WebSocketInterface webSocketInterface)
	{
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
		return new WebSocketPeer(WebSocketInterface.connect(peer.getAddress()));
	}

	public void broadCastNewBlock(BlockChain blockChain)
	{
		webSocketInterface.sendRequest("PUT", "/ws/blockChain", BlockChain.toJsonByteArray(blockChain));
	}
}
