package de.hsma.ppr.blockchain.bootnode;

import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebSocketServlet;
import static de.hsma.ppr.blockchain.nodes.dropwizard.WebsocketApplicationHelper.registerWebsocketComponent;

import org.whispersystems.websocket.setup.WebSocketEnvironment;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.resource.BlockChainResource;
import de.hsma.ppr.blockchain.resource.BlockChainWsResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class BootNode extends Application<Configuration>
{
	@Override
	public void run(Configuration configuration, Environment environment) throws Exception
	{
		BlockChain blockChain = new BlockChain();
		PeerWsResource peerResource = new PeerWsResource(configuration.getBootNodes().getSelf());
		BlockChainWsResource blockChainWsResource = new BlockChainWsResource(blockChain);
		WebSocketEnvironment webSocketEnvironment = registerWebsocketComponent(
		                                                                       configuration.getWebSocketConfiguration(),
		                                                                       environment,
		                                                                       peerResource,
		                                                                       blockChainWsResource);
		registerWebSocketServlet(environment, webSocketEnvironment);

		environment.jersey().register(BlockChainResource.withBlockChain(blockChain));
	}
	public static void main(String[] args) throws Exception
	{
		new BootNode().run(args);
	}
}
