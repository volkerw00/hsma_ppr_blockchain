package de.hsma.ppr.blockchain.node;

import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebSocketServlet;
import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebsocketComponent;

import javax.servlet.ServletException;

import org.slf4j.LoggerFactory;
import org.whispersystems.websocket.setup.WebSocketEnvironment;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.node.bootnode.BootNodeConnector;
import de.hsma.ppr.blockchain.node.configuration.Configuration;
import de.hsma.ppr.blockchain.node.mining.Miner;
import de.hsma.ppr.blockchain.node.mining.MinerResource;
import de.hsma.ppr.blockchain.nodes.blockchain.BlockChainResource;
import de.hsma.ppr.blockchain.nodes.blockchain.BlockChainWsResource;
import de.hsma.ppr.blockchain.nodes.data.DataPool;
import de.hsma.ppr.blockchain.nodes.data.DataResource;
import de.hsma.ppr.blockchain.nodes.data.DataWsResource;
import de.hsma.ppr.blockchain.nodes.peers.PeerWsResource;
import de.hsma.ppr.blockchain.nodes.peers.Peers;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;

public class Node extends io.dropwizard.Application<Configuration>
{
	@Override
	public void run(Configuration configuration, Environment environment) throws Exception
	{
		Peers peers = configuration.getPeers();
		peers.addPeers(configuration.getBootNodes().getBootNodes());
		peers.broadCastSelf(environment.lifecycle());

		BlockChain blockChain = new BlockChain();
		blockChain.addListener(peers.peerUpdateListener());

		DataPool dataPool = DataPool.withPeers(peers);
		Miner miner = configuration.getMinerFactory()
		                           .miner()
		                           .withBlockChain(blockChain)
		                           .withDataPool(dataPool);

		BlockChainWsResource blockChainWsResource = BlockChainWsResource.blockChainWsResource()
		                                                                .withBlockChain(blockChain)
		                                                                .withDataPool(dataPool)
		                                                                .withPeers(peers);
		PeerWsResource peerWsResource = new PeerWsResource(peers);
		DataWsResource dataWsResource = DataWsResource.withDataPool(dataPool);

		setupWebSocketResource(configuration,
		                       environment,
		                       peerWsResource,
		                       blockChainWsResource,
		                       dataWsResource);

		BootNodeConnector.bootNodeConnector()
		                 .bootNodes(configuration.getBootNodes())
		                 .lifeCycleEnvironment(environment.lifecycle())
		                 .peers(peers)
		                 .blockChain(blockChain)
		                 .onConnectionEstablished(() -> {
			                 LoggerFactory.getLogger(Node.class).info("BOOTNODE CONNECTION ESTABLISHED");
		                 })
		                 .connect();

		JerseyEnvironment jersey = environment.jersey();
		jersey.register(MinerResource.withMiner(miner));
		jersey.register(BlockChainResource.withBlockChain(blockChain));
		jersey.register(DataResource.dataResource().withBlockChain(blockChain).withDataPool(dataPool));

		miner.mine();
	}

	private void setupWebSocketResource(Configuration configuration, Environment environment, Object... components)
	    throws ServletException
	{
		WebSocketEnvironment webSocketEnvironment = registerWebsocketComponent(configuration.getWebSocketConfiguration(),
		                                                                       environment,
		                                                                       components);
		registerWebSocketServlet(environment, webSocketEnvironment);
	}

	public static void main(String[] args) throws Exception
	{
		new Node().run(args);
	}
}
