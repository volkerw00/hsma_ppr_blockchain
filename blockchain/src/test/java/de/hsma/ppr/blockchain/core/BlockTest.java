package de.hsma.ppr.blockchain.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class BlockTest
{
	@Test
	public void shouldCalculateAHash()
	{
		String hash = Block.hash(Instant.ofEpochMilli(1546300800), "f1r5t-h45h", new HashMap<>(), 0, 1);

		assertThat(hash, is("1f0ba602991d1273ebac7755720d33892b78bd6e5230a252cd57819b018bce7d"));
	}

	@Test
	public void minedBlockLastHash_shouldMatchPriorsBlockHash() throws Exception
	{
		adjustMineRate(2000);

		Block priorBlock = Block.genesis();
		priorBlock = Block.mineBlock(priorBlock, new HashMap<>());
		Block newBlock = Block.mineBlock(priorBlock, new HashMap<>());

		assertThat(newBlock.lastHash(), is(priorBlock.hash()));
	}

	@Test
	@Ignore
	public void benchmark_difficultyAdjustment() throws Exception
	{
		adjustMineRate(500);

		Block priorBlock = Block.genesis();
		Instant time = Instant.now();
		List<Long> times = new ArrayList<>();
		for (int i = 0; i < 100; i++)
		{
			priorBlock = Block.mineBlock(priorBlock, new HashMap<>());
			times.add(Instant.now().toEpochMilli() - time.toEpochMilli());
			long average = times.stream().reduce((total, num) -> total + num).get() / times.size();
			System.out.printf("Time: %5s ms || Average: %5s ms || Difficulty: %s %n",
			                  Instant.now().toEpochMilli() - time.toEpochMilli(),
			                  average,
			                  priorBlock.difficulty());
			time = Instant.now();
		}
		Block newBlock = Block.mineBlock(priorBlock, new HashMap<>());

		assertThat(newBlock.lastHash(), is(priorBlock.hash()));
	}

	private void adjustMineRate(int l) throws NoSuchFieldException, IllegalAccessException
	{
		Field difficulty = Difficulty.class.getDeclaredField("MINE_RATE");
		difficulty.setAccessible(true);
		difficulty.setLong(null, l);
	}
}
