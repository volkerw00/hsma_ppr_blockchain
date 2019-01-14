package de.hsma.ppr.blockchain.core;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BlockChainTest
{
	@Test
	public void blockChain_shouldStartWithGenesisBlock()
	{
		assertThat(new BlockChain().lastBlock().hash(), is(Block.genesis().hash()));
	}

	@Test
	public void blockChain_shouldAddANewBlock()
	{
		BlockChain blockChain = new BlockChain();
		blockChain.addBlock(data("data"));

		assertThat(blockChain.lastBlock().data(), hasValue("data"));
	}

	@Test
	public void isValid_shouldReturnTrueForAValidChain()
	{
		BlockChain blockChain = new BlockChain();
		blockChain.addBlock(data("foo"));
		blockChain.addBlock(data("bar"));

		assertTrue(BlockChain.isValid(blockChain));
	}

	@Test
	public void isValid_shouldReturnFalseForTamperedWithChain()
	{
		BlockChain blockChain = new BlockChain();
		Block foo = blockChain.addBlock(data("foo"));
		foo.data().replace("foo", "bar");

		assertFalse(BlockChain.isValid(blockChain));
	}

	@Test
	public void blockChain_shouldBeReplacedByLongerValidChain()
	{
		BlockChain blockChain = new BlockChain();

		BlockChain newChain = new BlockChain();
		newChain.addBlock(data("data"));

		blockChain.replace(newChain);

		assertThat(blockChain.lastBlock().data(), hasValue("data"));
	}

	@Test
	public void blockChain_shouldNotBeReplacedByEquallyLongOrSmallerChain()
	{
		BlockChain blockChain = new BlockChain();
		blockChain.addBlock(data("foo"));

		BlockChain otherBlockChain = new BlockChain();
		otherBlockChain.addBlock(data("bar"));

		blockChain.replace(otherBlockChain);

		assertThat(blockChain.lastBlock().data(), hasValue("foo"));
	}

	@Test
	public void blockChain_shouldNotBeReplacedByTamperedWithChain()
	{
		BlockChain blockChain = new BlockChain();
		blockChain.addBlock(data("foo"));

		BlockChain otherBlockChain = new BlockChain();
		Block bar = otherBlockChain.addBlock(data("bar"));
		bar.data().replace("bar", "foo");
		otherBlockChain.addBlock(data("baz"));

		blockChain.replace(otherBlockChain);

		assertThat(blockChain.lastBlock().data(), hasValue("foo"));
	}

	private static HashMap<String, String> data(String input)
	{
		HashMap<String, String> data = new HashMap<>();
		data.put(input, input);
		return data;
	}
}
