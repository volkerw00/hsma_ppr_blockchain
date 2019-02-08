package de.hsma.ppr.blockchain.node.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.hsma.ppr.blockchain.node.mining.Miner;

public class MinerFactory
{
	@Valid
	@NotNull
	private int threads;

	@Valid
	@NotNull
	private Map<String, String> data = new HashMap<>();

	@JsonProperty("threads")
	public void setThreads(int threads)
	{
		this.threads = threads;
	}

	@JsonProperty("data")
	public void setData(Map<String, String> data)
	{
		this.data = data;
	}

	public Miner miner()
	{
		return Miner.miner(threads, data);
	}
}
