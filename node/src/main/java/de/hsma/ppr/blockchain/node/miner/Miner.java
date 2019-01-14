package de.hsma.ppr.blockchain.node.miner;

import java.util.Map;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;

public class Miner
{
	private final BlockChain blockChain;

	private Miner(BlockChain blockChain)
	{
		this.blockChain = blockChain;
	}

	public static Miner withBlockChain(BlockChain blockChain)
	{
		return new Miner(blockChain);
	}

	public Block mineBlock(Map<String, String> data)
	{
		return blockChain.addBlock(data);
	}
}
