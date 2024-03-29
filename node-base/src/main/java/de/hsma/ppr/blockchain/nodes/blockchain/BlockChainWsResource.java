package de.hsma.ppr.blockchain.nodes.blockchain;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.exception.BlockChainNotValidException;
import de.hsma.ppr.blockchain.nodes.data.DataPool;
import de.hsma.ppr.blockchain.nodes.peers.Peers;

@Path("/ws/blockChain")
public class BlockChainWsResource
{
	private static final Logger logger = LoggerFactory.getLogger(BlockChainWsResource.class);

	private BlockChain	blockChain;
	private DataPool		dataPool;
	private Peers				peers;

	private BlockChainWsResource()
	{
	}

	public static BlockChainWsResource blockChainWsResource()
	{
		return new BlockChainWsResource();
	}

	public BlockChainWsResource withBlockChain(BlockChain blockChain)
	{
		this.blockChain = blockChain;
		return this;
	}

	public BlockChainWsResource withDataPool(DataPool dataPool)
	{
		this.dataPool = dataPool;
		return this;
	}

	public BlockChainWsResource withPeers(Peers peers)
	{
		this.peers = peers;
		return this;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newBlock(List<Block> newBlocks)
	{
		BlockChain newBlockChain;
		try
		{
			newBlockChain = BlockChain.forBlocks(newBlocks);
		} catch (BlockChainNotValidException e)
		{
			logger.error("Received blocks do not constitute a valid blockchain.");
			return Response.status(Status.BAD_REQUEST).build();
		}
		logger.info("Received new block {}", newBlockChain.lastBlock().hash());
		boolean chainReplaced = blockChain.replace(newBlockChain);
		if (chainReplaced)
		{
			logger.info("Replaced chain with received chain ending on {}", blockChain.lastBlock().hash());
			dataPool.removeData(blockChain.lastBlock());
			broadCastBlock(blockChain);
		} else
		{
			logger.info("Received blockchain ending on {} is not a valid update to existing chain ending on {}.",
			            newBlockChain.lastBlock().hash(),
			            blockChain.lastBlock().hash());
		}
		return Response.ok().build();
	}

	private void broadCastBlock(BlockChain blockChain)
	{
		peers.broadCastNewBlock(blockChain.lastBlock(), blockChain);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Block> getBlockChain()
	{
		return blockChain.blocks();
	}
}
