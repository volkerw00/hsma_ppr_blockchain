package de.hsma.ppr.blockchain.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class BlockChain
{
	public static BlockChain forBlocks(List<Block> blocks)
	{
		LinkedList<Block> list = new LinkedList<>(blocks);

		Collections.sort(list, (lb, rb) -> {
			if (lb.hash().equals(rb.lastHash())) { return -1; }
			return 1;
		});

		if (list.remove(Block.genesis()))
		{
			list.push(Block.genesis());
		}

		BlockChain blockChain = new BlockChain(blocks);
		if (!isValid(blockChain))
		{
			// TODO checked exception
			throw new RuntimeException("blocks do not build a valid blockChain");
		}
		return blockChain;
	}

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
				    || Difficulty.isDifficultyJumped(lastBlock, block)) { return false; }
				lastBlock = block;
			}
		}
		return true;
	}

	public Block addBlock(Map<String, String> data)
	{
		Block lastBlock = this.chain.getLast();
		Block newBlock = Block.mineBlock(lastBlock, data);
		this.chain.add(newBlock);
		listener.onBlockMined(newBlock, this);
		return newBlock;
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
			e.printStackTrace();
			// TODO
			throw new RuntimeException(e);
		}
	}

	public static BlockChain fromJsonArray(byte[] bytes)
	{
		try
		{
			Block[] blocks = new ObjectMapper().readValue(bytes, Block[].class);
			return BlockChain.forBlocks(Arrays.asList(blocks));
		} catch (IOException e)
		{
			e.printStackTrace();
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
}
