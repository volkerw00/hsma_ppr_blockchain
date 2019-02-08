package de.hsma.ppr.blockchain.bootnode;

import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebSocketServlet;
import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebsocketComponent;

import org.whispersystems.websocket.setup.WebSocketEnvironment;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.nodes.blockchain.BlockChainResource;
import de.hsma.ppr.blockchain.nodes.blockchain.BlockChainWsResource;
import de.hsma.ppr.blockchain.nodes.data.DataPool;
import de.hsma.ppr.blockchain.nodes.data.DataWsResource;
import de.hsma.ppr.blockchain.nodes.peers.PeerWsResource;
import de.hsma.ppr.blockchain.nodes.peers.Peers;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class BootNode extends Application<Configuration>
{
	@Override
	public void run(Configuration configuration, Environment environment) throws Exception
	{
		BlockChain blockChain = new BlockChain();
		Peers peers = new Peers();
		DataPool dataPool = DataPool.withPeers(peers);

		BootNodes bootNodes = configuration.getBootNodes();
		BootNodePeerWsResource bootNodePeerWsresource = new BootNodePeerWsResource(bootNodes.getSelf());
		bootNodes.connect(blockChain);

		PeerWsResource peerWsResource = new PeerWsResource(peers);
		BlockChainWsResource blockChainWsResource = BlockChainWsResource.blockChainWsResource()
		                                                                .withBlockChain(blockChain)
		                                                                .withDataPool(dataPool)
		                                                                .withPeers(peers);
		DataWsResource dataWsresource = DataWsResource.withDataPool(dataPool);

		WebSocketEnvironment webSocketEnvironment = registerWebsocketComponent(configuration.getWebSocketConfiguration(),
		                                                                       environment,
		                                                                       bootNodePeerWsresource,
		                                                                       peerWsResource,
		                                                                       blockChainWsResource,
		                                                                       dataWsresource);
		registerWebSocketServlet(environment, webSocketEnvironment);

		environment.jersey().register(BlockChainResource.withBlockChain(blockChain));
	}

	public static void main(String[] args) throws Exception
	{
		new BootNode().run(args);
	}
}
