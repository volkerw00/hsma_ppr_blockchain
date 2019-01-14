package de.hsma.ppr.blockchain.node.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.ListenableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.node.bootnode.BootNodeClient;
import de.hsma.ppr.blockchain.node.peers.Peers;
import de.hsma.ppr.blockchain.nodes.async.FutureCallback;
import de.hsma.ppr.blockchain.nodes.async.Futures;
import de.hsma.ppr.blockchain.nodes.resource.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;
import de.hsma.ppr.blockchain.nodes.ws.WsRuntimeException;

public class BootNodes
{
	public interface Listener
	{
		void onConnectionEstablished();

		static class NoOpListener implements Listener
		{
			@Override
			public void onConnectionEstablished()
			{
			}
		}
	}

	public static class BootNodeConnectionBuilder
	{
		private final Map<String, BootNodeClient>	bootNodeConnections;
		private String														localAddress;
		private Peers															peers;
		private BlockChain												blockChain;
		private BootNodes.Listener								listener	= new Listener.NoOpListener();

		BootNodeConnectionBuilder(Map<String, BootNodeClient> bootNodeConnections)
		{
			this.bootNodeConnections = bootNodeConnections;
		}

		public BootNodeConnectionBuilder connect(String localAddress)
		{
			this.localAddress = localAddress;
			return this;
		}

		public BootNodeConnectionBuilder fetchPeers(Peers peers)
		{
			this.peers = peers;
			return this;
		}

		public BootNodeConnectionBuilder fetchBlockChain(BlockChain blockChain)
		{
			this.blockChain = blockChain;
			return this;
		}

		public void establish()
		{
			for (Entry<String, BootNodeClient> bootNodeConnection : bootNodeConnections.entrySet())
			{
				String bootNode = bootNodeConnection.getKey();
				BootNodeClient bootNodeClient = bootNodeConnection.getValue();

				ListenableFuture<Boolean> response = bootNodeClient.hello(localAddress);

				Futures.addCallback(response, FutureCallback.<Boolean>callback().onSuccess(result -> {
					if (result)
					{
						logger.debug("Connection attempt to bootNode {} successful.", bootNode);

						fetchInitialPeers(bootNodeClient).forEach(peers::addPeer);
						blockChain.replace(BootNodes.fetchBlockChain(bootNodeClient));

						logger.info("BootNode initialization done.");
						listener.onConnectionEstablished();
					} else
					{
						logger.error("Connection attempt to bootNode {} not successful.", bootNode);
					}
				}).onFailure(t -> {
					logger.error("Failed to connect to bootNode {}", bootNode, t);
				}));
			}
		}

		public BootNodeConnectionBuilder onConnectionEstablished(BootNodes.Listener listener)
		{
			this.listener = listener;
			return this;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(BootNodes.class);

	@Valid
	@NotNull
	private Set<String> bootNodes = Collections.emptySet();

	@JsonProperty("urls")
	public Set<String> getBootNodes()
	{
		return bootNodes;
	}

	@JsonProperty("urls")
	public void setBootNodes(Set<String> bootNodes)
	{
		this.bootNodes = bootNodes;
	}

	public BootNodeConnectionBuilder connect(String localAdress)
	{
		Map<String, BootNodeClient> bootNodeConnections = new HashMap<>();
		// should only connect to one bootNode, but momentarily there is just one
		for (String bootNode : bootNodes)
		{
			logger.debug("Connecting to bootNode {}...", bootNode);
			bootNodeConnections.put(bootNode, connectTo(bootNode));
		}
		return new BootNodeConnectionBuilder(bootNodeConnections).connect(localAdress);
	}

	private static BootNodeClient connectTo(String bootNode)
	{
		try
		{
			return BootNodeClient.connect(bootNode);
		} catch (WsConnectException e)
		{
			// should be properly handled, but we don't care at the moment
			throw new WsRuntimeException(e);
		}
	}

	private static BlockChain fetchBlockChain(BootNodeClient bootNodeClient)
	{
		try
		{
			return bootNodeClient.getBlockChain().get();
		} catch (InterruptedException | ExecutionException e)
		{
			// TODO handle concurrency exception
			throw new RuntimeException(e);
		}
	}

	private static Set<Peer> fetchInitialPeers(BootNodeClient bootNodeClient)
	{
		try
		{
			return bootNodeClient.getPeers().get();
		} catch (InterruptedException | ExecutionException e)
		{
			// TODO handle concurrency exception
			throw new RuntimeException(e);
		}
	}
}
