package de.hsma.ppr.blockchain.nodes.data;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/ws/data")
public class DataWsResource
{
	private static final Logger	logger	= LoggerFactory.getLogger(DataWsResource.class);
	private DataPool						dataPool;

	private DataWsResource(DataPool dataPool)
	{
		this.dataPool = dataPool;
	}

	public static DataWsResource withDataPool(DataPool dataPool)
	{
		return new DataWsResource(dataPool);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newData(Map<String, String> data)
	{
		logger.info("Recived data {}", data);
		dataPool.save(data);
		return Response.ok().build();
	}
}
