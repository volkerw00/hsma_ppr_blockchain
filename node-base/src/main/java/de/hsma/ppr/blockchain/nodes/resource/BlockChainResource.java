package de.hsma.ppr.blockchain.nodes.resource;

import java.util.LinkedList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class BlockChainResource
{
	private final BlockChain blockChain;

	private BlockChainResource(BlockChain blockChain)
	{
		this.blockChain = blockChain;
	}

	public static BlockChainResource withBlockChain(BlockChain blockChain)
	{
		return new BlockChainResource(blockChain);
	}

	@GET
	@Path("/blocks")
	public LinkedList<Block> blocks()
	{
		return blockChain.blocks();
	}
}
