package de.hsma.ppr.blockchain.node.bootnode;

import static java.util.Arrays.stream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.node.configuration.BootNodes;
import de.hsma.ppr.blockchain.node.peers.Peers;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

public class BootNodeConnector
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

	private BootNodes										bootNodes;
	private LifecycleEnvironment				lifecycleEnvironment;
	private Peers												peers;
	private BlockChain									blockChain;
	private BootNodeConnector.Listener	listener	= new Listener.NoOpListener();

	private BootNodeConnector()
	{
	}

	public static BootNodeConnector bootNodeConnector()
	{
		return new BootNodeConnector();
	}

	public BootNodeConnector bootNodes(BootNodes bootNodes)
	{
		this.bootNodes = bootNodes;
		return this;
	}

	public BootNodeConnector lifeCycleEnvironment(LifecycleEnvironment lifecycleEnvironment)
	{
		this.lifecycleEnvironment = lifecycleEnvironment;
		return this;
	}

	public BootNodeConnector peers(Peers peers)
	{
		this.peers = peers;
		return this;
	}

	public BootNodeConnector blockChain(BlockChain blockChain)
	{
		this.blockChain = blockChain;
		return this;
	}

	public BootNodeConnector onConnectionEstablished(BootNodeConnector.Listener listener)
	{
		this.listener = listener;
		return this;
	}

	public void connect()
	{
		setupBootNodeConnection(bootNodes, lifecycleEnvironment, peers, blockChain);
	}

	private void setupBootNodeConnection(BootNodes bootNodes,
	                                     LifecycleEnvironment lifeCycleEnvironment,
	                                     Peers peers,
	                                     BlockChain blockChain)
	{
		lifeCycleEnvironment.addServerLifecycleListener(server -> {
			Stream<ServerConnector> connectors = getApplicationConnectors(server.getConnectors());
			connectors.map(c -> getLocalAdress(c))
			          .forEach(localAdress -> {
				          bootNodes.connect(localAdress)
				                   .fetchPeers(peers)
				                   .fetchBlockChain(blockChain)
				                   .onConnectionEstablished(() -> listener.onConnectionEstablished())
				                   .establish();
			          });
		});
	}

	private Stream<ServerConnector> getApplicationConnectors(Connector[] connectors)
	{
		return stream(connectors).filter(c -> c instanceof ServerConnector)
		                         .map(c -> (ServerConnector) c)
		                         .filter(c -> "application".equals(c.getName()));
	}

	private String getLocalAdress(ServerConnector connector)
	{
		try
		{
			return String.format("ws://%s:%s",
			                     InetAddress.getLocalHost().getHostAddress(),
			                     connector.getLocalPort());
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
			// TODO
			throw new RuntimeException(e);
		}
	}
}
