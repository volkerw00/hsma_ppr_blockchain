package de.hsma.ppr.blockchain.mining;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import java.util.HashMap;

import javax.ws.rs.client.Entity;

import org.junit.ClassRule;
import org.junit.Test;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.node.mining.Miner;
import de.hsma.ppr.blockchain.node.mining.MinerResource;
import de.hsma.ppr.blockchain.nodes.data.DataPool;
import de.hsma.ppr.blockchain.nodes.data.DataResource;
import de.hsma.ppr.blockchain.nodes.peers.Peers;
import io.dropwizard.testing.junit.ResourceTestRule;

public class MinerResourceTest
{
	private static BlockChain blockChain = new BlockChain();

	private static DataPool dataPool = DataPool.withPeers(new Peers());

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
	                                                                 .addResource(MinerResource.withMiner(Miner.miner()
	                                                                                                           .withBlockChain(blockChain)
	                                                                                                           .withDataPool(dataPool)))
	                                                                 .addResource(DataResource.dataResource()
	                                                                                          .withBlockChain(blockChain)
	                                                                                          .withDataPool(dataPool))
	                                                                 .build();

	@Test
	public void shouldMineABlock()
	{
		HashMap<String, String> data = new HashMap<>();
		data.put("foo", "bar");

		resources.target("/data").request().put(Entity.json(data));
		resources.target("/mine").request().post(Entity.json(""));

		assertThat(resources.target("/data").request().get(String.class), containsString("{\"foo\":\"bar\"}"));
	}
}
