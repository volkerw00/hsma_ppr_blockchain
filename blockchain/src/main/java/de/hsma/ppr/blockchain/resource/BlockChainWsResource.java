package de.hsma.ppr.blockchain.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;

@Path("/ws/blockChain")
public class BlockChainWsResource
{
	private static final Logger logger = LoggerFactory.getLogger(BlockChainWsResource.class);

	private final BlockChain blockChain;

	public BlockChainWsResource(BlockChain blockChain)
	{
		this.blockChain = blockChain;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void newBlock(List<Block> newBlocks)
	{
		BlockChain newBlockChain = BlockChain.forBlocks(newBlocks);
		logger.info("Received new block {}", newBlockChain.lastBlock().hash());
		boolean chainReplaced = blockChain.replace(newBlockChain);
		if (chainReplaced)
		{
			logger.info("Replaced chain with received chain ending on {}", blockChain.lastBlock().hash());
		} else
		{
			logger.info("Received blockchain ending on {} is not a valid update to existing chain ending on {}.",
			            newBlockChain.lastBlock().hash(),
			            blockChain.lastBlock().hash());
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Block> getBlockChain()
	{
		return blockChain.blocks();
	}
}
