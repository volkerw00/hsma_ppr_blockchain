package de.hsma.ppr.blockchain.node.resource;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.node.miner.Miner;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class MinerResource
{
	private final Miner miner;

	private MinerResource(Miner miner)
	{
		this.miner = miner;
	}

	public static MinerResource withMiner(Miner miner)
	{
		return new MinerResource(miner);
	}

	@POST
	@Path("/mine")
	@Consumes(MediaType.APPLICATION_JSON)
	public Block mine(Map<String, String> data)
	{
		return miner.mineBlock(data);
	}
}
