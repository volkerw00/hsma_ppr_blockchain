package de.hsma.ppr.blockchain.node.peers;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.core.BlockChain.Listener;
import de.hsma.ppr.blockchain.node.configuration.Configuration;
import de.hsma.ppr.blockchain.nodes.resource.Peer;
import de.hsma.ppr.blockchain.nodes.ws.WsConnectException;

public class Peers
{
	private static final Logger logger = LoggerFactory.getLogger(Peers.class);

	private final Set<Peer> peers = new HashSet<>();

	private Peers(Set<String> initialPeers)
	{
		peers.addAll(initialPeers.stream().map(peer -> Peer.forAddress(peer)).collect(toSet()));
	}

	public void addPeer(Peer peer)
	{
		logger.debug("Learned new Peer {}", peer);
		this.peers.add(peer);
	}

	public void removePeer(Peer peer)
	{
		logger.debug("Lost Peer {}", peer);
		peers.remove(peer);
	}

	public void broadCastNewBlock(Block block, BlockChain blockChain)
	{
		logger.debug("Broadcasting new Block {}", block);
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

	public static Peers createPeers(Configuration configuration)
	{
		return new Peers(configuration.getBootNodes().getBootNodes());
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
}
