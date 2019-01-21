package de.hsma.ppr.blockchain.node.mining;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.exception.BlockChainNotValidException;
import de.hsma.ppr.blockchain.nodes.data.DataPool;

public class Miner
{
	private static final Logger logger = LoggerFactory.getLogger(Miner.class);

	@Nullable
	private DataPool dataPool;

	@Nullable
	private BlockChain blockChain;

	private Miner()
	{
	}

	public static Miner miner()
	{
		return new Miner();
	}

	public Miner withDataPool(DataPool dataPool)
	{
		this.dataPool = dataPool;
		return this;
	}

	public Miner withBlockChain(BlockChain blockChain)
	{
		this.blockChain = blockChain;
		return this;
	}

	public Block mineBlock()
	{
		Map<String, String> nextData = dataPool.nextData();
		logger.debug("Adding data {} to next block", nextData);

		Block lastBlock = blockChain.lastBlock();
		Block newBlock = Block.mineBlock(lastBlock, nextData);
		try
		{
			blockChain.addBlock(newBlock);
		} catch (BlockChainNotValidException e)
		{
			logger.debug("Mined invalid chain", e);
			return mineBlock();
		}
		return newBlock;
	}

	public void mine()
	{
		int nThreads = 2;
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		for (int i = 0; i < nThreads; i++)
		{
			executorService.execute(() -> {
				while (true)
				{
					Block block = mineBlock();
					logger.info("\u2692 Mined {}", block);
					dataPool.removeData(block);
				}
			});
		}
	}
}
