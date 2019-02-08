package de.hsma.ppr.blockchain.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

public class Difficulty
{
	private static long MINE_RATE = 20000;

	private int difficulty;

	private Difficulty(int difficulty)
	{
		this.difficulty = difficulty;
	}

	public static Difficulty get(int difficulty)
	{
		return new Difficulty(difficulty);
	}

	public boolean isSatisfied(String hash)
	{
		return hexToBin(hash).startsWith(Strings.repeat("0", difficulty)); // TODO benchmark hex vs bin
	}

	public void adjust(Block lastBlock, Instant currentTimestamp)
	{
		long lastBlockTime = lastBlock.timeStamp();
		long currentBlockTime = currentTimestamp.toEpochMilli();

		difficulty = currentBlockTime - lastBlockTime > MINE_RATE ? lastBlock.difficulty() - 1 : lastBlock.difficulty() + 1;
		if (difficulty < 1) difficulty = 1;
	}

	public int get()
	{
		return difficulty;
	}

	private static Map<String, String> digiMap = new HashMap<>();

	static
	{
		digiMap.put("0", "0000");
		digiMap.put("1", "0001");
		digiMap.put("2", "0010");
		digiMap.put("3", "0011");
		digiMap.put("4", "0100");
		digiMap.put("5", "0101");
		digiMap.put("6", "0110");
		digiMap.put("7", "0111");
		digiMap.put("8", "1000");
		digiMap.put("9", "1001");
		digiMap.put("A", "1010");
		digiMap.put("B", "1011");
		digiMap.put("C", "1100");
		digiMap.put("D", "1101");
		digiMap.put("E", "1110");
		digiMap.put("F", "1111");
		digiMap.put("a", "1010");
		digiMap.put("b", "1011");
		digiMap.put("c", "1100");
		digiMap.put("d", "1101");
		digiMap.put("e", "1110");
		digiMap.put("f", "1111");
	}

	static String hexToBin(String s)
	{
		char[] hex = s.toCharArray();
		String binaryString = "";
		for (char h : hex)
		{
			binaryString = binaryString + digiMap.get(String.valueOf(h));
		}

		return binaryString;
	}

	static boolean isDifficultyJumped(Block lastBlock, Block block)
	{
		return Math.abs(lastBlock.difficulty() - block.difficulty()) > 1;
	}
}
