package de.hsma.ppr.blockchain.nodes.data;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.hsma.ppr.blockchain.core.BlockChain;

@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class DataResource
{
	@Nullable
	private DataPool		dataPool;
	private BlockChain	blockChain;

	private DataResource()
	{
	}

	public static DataResource dataResource()
	{
		return new DataResource();
	}

	public DataResource withDataPool(DataPool dataPool)
	{
		this.dataPool = dataPool;
		return this;
	}

	public DataResource withBlockChain(BlockChain blockChain)
	{
		this.blockChain = blockChain;
		return this;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void persist(Map<String, String> data)
	{
		this.dataPool.save(data);
	}

	@GET
	public Map<String, String> load()
	{
		return blockChain.blocks()
		                 .stream()
		                 .map(block -> block.data())
		                 .reduce(new HashMap<String, String>(), (a, b) -> {
			                 a.putAll(b);
			                 return a;
		                 });
	}
}
