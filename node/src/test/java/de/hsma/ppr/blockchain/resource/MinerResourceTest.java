package de.hsma.ppr.blockchain.resource;

import de.hsma.ppr.blockchain.core.BlockChain;
import de.hsma.ppr.blockchain.node.miner.Miner;
import de.hsma.ppr.blockchain.node.resource.MinerResource;
import de.hsma.ppr.blockchain.nodes.resource.BlockChainResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

public class MinerResourceTest
{
	private static BlockChain blockChain = new BlockChain();

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
	                                                                 .addResource(MinerResource.withMiner(Miner.withBlockChain(blockChain)))
	                                                                 .addResource(BlockChainResource.withBlockChain(blockChain))
	                                                                 .build();

	@Test
	public void shouldMineABlock()
	{
		HashMap<String, String> data = new HashMap<>();
		data.put("foo", "bar");

		resources.target("/mine").request().post(Entity.entity(data, MediaType.APPLICATION_JSON));

		assertThat(resources.target("/blocks").request().get(String.class), containsString("\"data\":{\"foo\":\"bar\"}"));
	}
}
