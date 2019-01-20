package de.hsma.ppr.blockchain.nodes.peers;

import static java.util.stream.Collectors.toSet;
import static java.util.Arrays.stream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.core.BlockChain.Listener;
import de.hsma.ppr.blockchain.nodes.exception.NodeRuntimeException;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

public class Peers
{
	private static final Logger logger = LoggerFactory.getLogger(Peers.class);

	@Valid
	@NotNull
	private Set<Peer> peers = new HashSet<>();

	@Valid
	@NotNull
	private boolean broadcastSelf = true;

	@Valid
	@NotNull
	private boolean broadcast = true;

	@JsonProperty("urls")
	public Set<String> getPeers()
	{
		return peers.stream().map(Peer::getAddress).collect(Collectors.toSet());
	}

	@JsonProperty("urls")
	public void setPeers(Set<String> initialPeers)
	{
		this.peers = initialPeers.stream().map(peer -> Peer.forAddress(peer)).collect(toSet());
	}

	@JsonProperty("broadcastSelf")
	public boolean getBroadcastSelf()
	{
		return broadcastSelf;
	}

	@JsonProperty("broadcastSelf")
	public void setBroadcastSelf(boolean broadcastSelf)
	{
		this.broadcastSelf = broadcastSelf;
	}

	@JsonProperty("broadcast")
	public boolean getBroadcast()
	{
		return broadcast;
	}

	@JsonProperty("broadcast")
	public void setBroadcast(boolean broadcast)
	{
		this.broadcast = broadcast;
	}

	public void addPeers(Set<String> addresses)
	{
		addresses.forEach(address -> addPeer(Peer.forAddress(address)));
	}

	public void addPeer(String address)
	{
		addPeer(Peer.forAddress(address));
	}

	public void addPeer(Peer newPeer)
	{
		if (!peers.contains(newPeer))
		{
			List<Peer> broadCastReceivers = shallowCopy(peers);

			logger.info("Learned {}", newPeer);
			peers.add(newPeer);

			if (broadcast)
			{
				broadCastReceivers.forEach(peer -> {
					try
					{
						logger.debug("Broadcasting {} to {}", newPeer, peer);
						WebSocketPeer.connect(peer).broadCastNewPeer(newPeer);
					} catch (WsConnectException e)
					{
						removePeer(peer);
					}
				});
			}
		}
	}

	public void removePeer(Peer peer)
	{
		logger.info("Lost {}", peer);
		peers.remove(peer);
	}

	public void broadCastNewBlock(Block block, BlockChain blockChain)
	{
		logger.info("Broadcasting {}", block);
		shallowCopy(peers).stream().forEach(peer -> {
			try
			{
				WebSocketPeer.connect(peer).broadCastNewBlock(blockChain);
			} catch (WsConnectException e)
			{
				removePeer(peer);
			}
		});
	}

	private ArrayList<Peer> shallowCopy(Set<Peer> peers)
	{
		return new ArrayList<>(peers);
	}

	public Listener peerUpdateListener()
	{
		return new BlockChain.Listener() {
			@Override
			public void onBlockMined(Block block, BlockChain blockChain)
			{
				broadCastNewBlock(block, blockChain);
			}
		};
	}

	public void broadCastSelf(LifecycleEnvironment lifeCycleEnvironment)
	{
		if (broadcastSelf)
		{
			logger.info("Broadcasting self to {} known peers.", peers.size());
			logger.debug("Broadcasting self to {}", peers);
			lifeCycleEnvironment.addServerLifecycleListener(server -> {
				Stream<ServerConnector> connectors = getApplicationConnectors(server.getConnectors());
				connectors.map(c -> getLocalAddress(c))
				          .forEach(localAddress -> {
					          shallowCopy(peers).forEach(peer -> {
						          try
						          {
							          WebSocketPeer.connect(peer).broadCastNewPeer(Peer.forAddress(localAddress));
						          } catch (WsConnectException e)
						          {
							          removePeer(peer);
						          }
					          });
				          });
			});
			logger.info("Self broadcast finished.");
		} else
		{
			logger.info("Self not broadcasted to known peers.");
		}
	}

	private Stream<ServerConnector> getApplicationConnectors(Connector[] connectors)
	{
		return stream(connectors).filter(c -> c instanceof ServerConnector)
		                         .map(c -> (ServerConnector) c)
		                         .filter(c -> "application".equals(c.getName()));
	}

	private String getLocalAddress(ServerConnector connector)
	{
		try
		{
			return String.format("ws://%s:%s",
			                     InetAddress.getLocalHost().getHostAddress(),
			                     connector.getLocalPort());
		} catch (UnknownHostException e)
		{
			throw new NodeRuntimeException(e);
		}
	}
}
