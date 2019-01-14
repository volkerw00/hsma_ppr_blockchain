package de.hsma.ppr.blockchain.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Block
{
	private final Instant							timestamp;
	private final String							lastHash;
	private final String							hash;
	private final Map<String, String>	data;
	private final int									nonce;
	private int												difficulty;

	private Block(Instant timestamp,
	              String lastHash,
	              String hash,
	              Map<String, String> data,
	              int nonce,
	              int difficulty)
	{
		this.timestamp = timestamp;
		this.lastHash = lastHash;
		this.hash = hash;
		this.data = data;
		this.nonce = nonce;
		this.difficulty = difficulty;
	}

	@JsonCreator
	static final Block fromJson(@JsonProperty("timeStamp") long timestamp,
	                            @JsonProperty("lastHash") String lastHash,
	                            @JsonProperty("hash") String hash,
	                            @JsonProperty("data") Map<String, String> data,
	                            @JsonProperty("nonce") int nonce,
	                            @JsonProperty("difficulty") int difficulty)
	{
		return new Block(Instant.ofEpochMilli(timestamp),
		                 lastHash,
		                 hash,
		                 data != null ? data : new HashMap<>(),
		                 nonce,
		                 difficulty);
	}

	static Block genesis()
	{
		return new Block(Instant.ofEpochMilli(1546300800),
		                 "----- genesis -----",
		                 "g3n3s1s-h45h",
		                 new HashMap<>(),
		                 0,
		                 1);
	}

	static Block mineBlock(Block lastBlock, Map<String, String> data)
	{
		Map<String, String> d = data != null ? new HashMap<>(data) : new HashMap<>();
		String hash = "";
		Instant timestamp = Instant.now();
		int nonce = 0;
		int difficulty = Difficulty.get();

		do
		{
			timestamp = Instant.now();
			Difficulty.adjust(lastBlock, timestamp);
			difficulty = Difficulty.get();
			hash = hash(timestamp, lastBlock.hash, d, nonce, difficulty);
			nonce = nonce + 1;

		} while (!Difficulty.isSatisfied(hash));

		Block block = new Block(timestamp, lastBlock.hash, hash, d, nonce - 1, difficulty);
		return block;
	}

	static String hash(Instant timestamp, String lastHash, Map<String, String> data, int nonce, int difficulty)
	{
		return Hashing.sha256()
		              .hashString(String.format("%s%s%s%s%s",
		                                        timestamp.toEpochMilli(),
		                                        lastHash,
		                                        data,
		                                        nonce,
		                                        difficulty),
		                          StandardCharsets.UTF_8)
		              .toString();
	}

	public static String hash(Block block)
	{
		return hash(block.timestamp, block.lastHash, block.data, block.nonce, block.difficulty);
	}

	static boolean isValidHash(Block block)
	{
		return Objects.equals(block, Block.genesis()) || Objects.equals(block.hash(), Block.hash(block));
	}

	@JsonProperty
	public long timeStamp()
	{
		return timestamp.toEpochMilli();
	}

	@JsonProperty
	public String lastHash()
	{
		return lastHash;
	}

	@JsonProperty
	public String hash()
	{
		return hash;
	}

	@JsonProperty
	public Map<String, String> data()
	{
		return this.data;
	}

	@JsonProperty
	public int nonce()
	{
		return this.nonce;
	}

	@JsonProperty
	public int difficulty()
	{
		return difficulty;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		  return true;
		if (o == null || getClass() != o.getClass())
		  return false;
		Block block = (Block) o;
		return nonce == block.nonce && difficulty == block.difficulty
		       && Objects.equals(timestamp, block.timestamp)
		       && Objects.equals(lastHash, block.lastHash)
		       && Objects.equals(hash, block.hash)
		       && Objects.equals(data, block.data);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(timestamp, lastHash, hash, data, nonce, difficulty);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
		                  .add("timestamp", timestamp)
		                  .add("lastHash", lastHash)
		                  .add("hash", hash)
		                  .add("data", data)
		                  .add("nonce", nonce)
		                  .add("difficulty", difficulty)
		                  .toString();
	}

	boolean isParent(Block lastBlock)
	{
		return Objects.equals(this.lastHash, lastBlock.hash());
	}
}
