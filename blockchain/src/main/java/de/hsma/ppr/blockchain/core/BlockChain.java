package de.hsma.ppr.blockchain.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.hsma.ppr.blockchain.exception.BlockChainNotValidException;
import de.hsma.ppr.blockchain.exception.ByteConversionFailedExcetion;

public class BlockChain
{
	public interface Listener
	{
		void onBlockMined(Block block, BlockChain blockChain);

		public static class NoOp implements Listener
		{
			@Override
			public void onBlockMined(Block block, BlockChain blockChain)
			{

			}
		}
	}

	private LinkedList<Block>	chain			= initialChain();
	private Listener					listener	= new Listener.NoOp();
	
	public BlockChain()
	{
	}
	
	private BlockChain(List<Block> blocks)
	{
		this.chain = new LinkedList<>(blocks);
	}
	
		public static BlockChain forBlocks(List<Block> blocks) throws BlockChainNotValidException
		{
			LinkedList<Block> list = new LinkedList<>(blocks);
	
			Collections.sort(list, (lb, rb) -> {
				if (lb.hash().equals(rb.lastHash()))
				{ return -1; }
				return 1;
			});
	
			if (list.remove(Block.genesis()))
			{
				list.push(Block.genesis());
			}
	
			BlockChain blockChain = new BlockChain(blocks);
			if (!isValid(blockChain))
			{ throw new BlockChainNotValidException(); }
			return blockChain;
		}

	public void addListener(Listener listener)
	{
		this.listener = listener;
	}

	private static LinkedList<Block> initialChain()
	{
		LinkedList<Block> initialChain = new LinkedList<>();
		initialChain.add(Block.genesis());
		return initialChain;
	}

	static boolean isValid(BlockChain blockChain)
	{
		if (Objects.equals(blockChain.chain.getFirst().hash(), Block.genesis().hash()))
		{

			Block lastBlock = blockChain.chain.getFirst();
			Block block;

			Iterator<Block> i$blockChain = blockChain.chain.iterator();
			if (i$blockChain.hasNext())
			{
				i$blockChain.next();
			}
			while (i$blockChain.hasNext())
			{
				block = i$blockChain.next();
				if (!block.isParent(lastBlock)
				    || !Block.isValidHash(block)
				    || Difficulty.isDifficultyJumped(lastBlock, block))
				{ return false; }
				lastBlock = block;
			}
		}
		return true;
	}

	public Block mineBlock(Map<String, String> data)
	{
		Block lastBlock = this.chain.getLast();
		Block newBlock = Block.mineBlock(lastBlock, data);
		this.chain.add(newBlock);
		listener.onBlockMined(newBlock, this);
		return newBlock;
	}

	public void addBlock(Block newBlock) throws BlockChainNotValidException
	{
		List<Block> newBlocks = new LinkedList<>(blocks());
		newBlocks.add(newBlock);

		replace(BlockChain.forBlocks(newBlocks));
	}

	public Block lastBlock()
	{
		return this.chain.getLast();
	}

	public boolean replace(BlockChain newChain)
	{
		if (newChain.chain.size() > this.chain.size() && isValid(newChain))
		{
			this.chain = newChain.chain;
			return true;
		}
		return false;
	}

	public LinkedList<Block> blocks()
	{
		return chain;
	}

	public static byte[] toJsonByteArray(BlockChain blockChain)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectMapper().writeValue(out, blockChain.blocks());
			return out.toByteArray();
		} catch (IOException e)
		{
			throw new ByteConversionFailedExcetion(e);
		}
	}

	public static BlockChain fromJsonArray(byte[] bytes) throws BlockChainNotValidException
	{
		try
		{
			Block[] blocks = new ObjectMapper().readValue(bytes, Block[].class);
			return BlockChain.forBlocks(Arrays.asList(blocks));
		} catch (IOException e)
		{
			throw new ByteConversionFailedExcetion(e);
		}
	}
}
