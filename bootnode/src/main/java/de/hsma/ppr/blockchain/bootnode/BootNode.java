package de.hsma.ppr.blockchain.bootnode;

import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebSocketServlet;
import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebsocketComponent;

import org.whispersystems.websocket.setup.WebSocketEnvironment;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.nodes.peers.Peers;
import de.hsma.ppr.blockchain.nodes.resource.BlockChainResource;
import de.hsma.ppr.blockchain.nodes.resource.BlockChainWsResource;
import de.hsma.ppr.blockchain.nodes.resource.PeerWsResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class BootNode extends Application<Configuration>
{
	@Override
	public void run(Configuration configuration, Environment environment) throws Exception
	{
		BlockChain blockChain = new BlockChain();
		
		BootNodes bootNodes = configuration.getBootNodes();
		BootNodePeerWsResource bootNodePeerWsresource = new BootNodePeerWsResource(bootNodes.getSelf());
		bootNodes.connect(blockChain);

		Peers peers = new Peers();
		PeerWsResource peerWsResource = new PeerWsResource(peers);

		BlockChainWsResource blockChainWsResource = new BlockChainWsResource(blockChain, peers);

		WebSocketEnvironment webSocketEnvironment = registerWebsocketComponent(
		                                                                       configuration.getWebSocketConfiguration(),
		                                                                       environment,
																																					 bootNodePeerWsresource,
																																					 peerWsResource,
		                                                                       blockChainWsResource);
		registerWebSocketServlet(environment, webSocketEnvironment);

		environment.jersey().register(BlockChainResource.withBlockChain(blockChain));
	}
	public static void main(String[] args) throws Exception
	{
		new BootNode().run(args);
	}
}
