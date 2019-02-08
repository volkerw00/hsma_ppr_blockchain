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

	private Map<String, String>	staticData;
	private int									threads;

	private Miner(int threads, Map<String, String> data)
	{
		this.threads = threads;
		this.staticData = data;
	}

	public static Miner miner(int threads, Map<String, String> data)
	{
		return new Miner(threads, data);
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
		nextData.putAll(staticData);
		try
		{
			return blockChain.mineBlock(nextData);
		} catch (BlockChainNotValidException e)
		{
			logger.debug("Mined invalid chain", e);
			return mineBlock();
		}
	}

	public void mine()
	{
		ExecutorService executorService = Executors.newFixedThreadPool(threads);
		for (int i = 0; i < threads; i++)
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
