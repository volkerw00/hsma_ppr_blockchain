package de.hsma.ppr.blockchain.core;

import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BlockTest
{

	@Test
	public void shouldCalculateAHash()
	{
		String hash = Block.hash(Instant.ofEpochMilli(1546300800), "f1r5t-h45h", new HashMap<>(), 0, 1);

		assertThat(hash, is("1f0ba602991d1273ebac7755720d33892b78bd6e5230a252cd57819b018bce7d"));
	}

	@Test
	public void minedBlockLastHash_shouldMatchPriorsBlockHash()
	{
		Block priorBlock = Block.genesis();
		Block newBlock = Block.mineBlock(priorBlock, new HashMap<>());

		assertThat(newBlock.lastHash(), is(priorBlock.hash()));
	}
}
