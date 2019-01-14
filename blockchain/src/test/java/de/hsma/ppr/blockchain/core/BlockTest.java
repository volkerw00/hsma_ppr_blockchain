package de.hsma.ppr.blockchain.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BlockTest
{
	@Before
	public void resetDifficulty() throws NoSuchFieldException, IllegalAccessException
	{
		adjustDifficulty(6);
	}

	private void adjustDifficulty(int difficulty) throws NoSuchFieldException, IllegalAccessException
	{
		Field field = Difficulty.class.getDeclaredField("DIFFICULTY");
		field.setAccessible(true);
		field.setInt(null, difficulty);
	}

	@Test
	public void shouldCalculateAHash()
	{
		String hash = Block.hash(Instant.ofEpochMilli(1546300800), "f1r5t-h45h", new HashMap<>(), 0, Difficulty.get());

		assertThat(hash, is("ba2f48cfe381e92de22672d2f1fdd4b8da21a03a64ef2909288375809e63073b"));
	}

	@Test
	public void minedBlockLastHash_shouldMatchPriorsBlockHash() throws Exception
	{
		adjustMineRate(2000);
		adjustDifficulty(6);

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
		adjustDifficulty(6);

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
