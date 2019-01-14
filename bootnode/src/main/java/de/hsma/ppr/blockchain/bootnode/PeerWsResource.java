package de.hsma.ppr.blockchain.bootnode;

import de.hsma.ppr.blockchain.nodes.resource.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WebSocketInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.session.WebSocketSession;
import org.whispersystems.websocket.session.WebSocketSessionContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Path("/ws/peers")
public class PeerWsResource
{
	private static final Logger logger = LoggerFactory.getLogger(PeerWsResource.class);

	private Set<Peer> peers = new HashSet<>();

	public PeerWsResource(String self)
	{
		peers.add(Peer.forAddress(self));
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response hello(@WebSocketSession WebSocketSessionContext context, Peer newPeer)
	{
		if (!peers.contains(newPeer))
		{
			try
			{
				this.peers.add(newPeer);
				logger.info("Learned new peer {}.", newPeer.getAddress());
				return Response.ok().build();
			} finally
			{
				broadcastNewPeer(newPeer);
			}
		}
		return Response.ok().build();
	}

	private void broadcastNewPeer(Peer newPeer)
	{
		logger.info("Broadcasting peer {}", newPeer);
		shallowCopy(this.peers).stream().filter(peer -> !Objects.equals(peer, newPeer)).forEach(peer -> {
			try
			{
				WebSocketInterface webSocket = connect(peer);
				webSocket.sendRequest("POST", "/ws/peers", newPeer.asJsonByteArray());
			} catch (ConnectException e)
			{
				removePeer(peer);
			}
		});
	}

	private ArrayList<Peer> shallowCopy(Set<Peer> peers)
	{
		return new ArrayList<>(peers);
	}

	private WebSocketInterface connect(Peer peer) throws ConnectException
	{
		return WebSocketInterface.connect(peer.getAddress());
	}

	private void removePeer(Peer removedPeer)
	{
		logger.info("Broadcasting removal of peer {}", removedPeer);
		peers.remove(removedPeer);
		shallowCopy(peers).stream().forEach(peer -> {
			try
			{
				WebSocketInterface client = connect(peer);
				client.sendRequest("DELETE", "/ws/peers", removedPeer.asJsonByteArray());
			} catch (ConnectException e)
			{
				removePeer(peer);
			}
		});
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<Peer> peers()
	{
		return peers;
	}
}
