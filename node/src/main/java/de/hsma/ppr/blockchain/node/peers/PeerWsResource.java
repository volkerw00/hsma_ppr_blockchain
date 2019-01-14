package de.hsma.ppr.blockchain.node.peers;

import de.hsma.ppr.blockchain.nodes.resource.Peer;
import org.whispersystems.websocket.session.WebSocketSession;
import org.whispersystems.websocket.session.WebSocketSessionContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/ws/peers")
@Consumes(MediaType.APPLICATION_JSON)
public class PeerWsResource
{
	private final Peers peers;

	public PeerWsResource(Peers peers)
	{
		this.peers = peers;
	}

	@POST
	public void newPeer(@WebSocketSession WebSocketSessionContext context, Peer newPeer)
	{
		peers.addPeer(newPeer);
	}

	@DELETE
	public void lostPeer(Peer lostPeer)
	{
		peers.removePeer(lostPeer);
	}
}
