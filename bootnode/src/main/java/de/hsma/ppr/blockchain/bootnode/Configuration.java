package de.hsma.ppr.blockchain.bootnode;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.whispersystems.websocket.configuration.WebSocketConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Configuration extends io.dropwizard.Configuration
{
	@Valid
	@NotNull
	@JsonProperty
	private WebSocketConfiguration webSocketConfiguration = new WebSocketConfiguration();

	@Valid
	@NotNull
	private BootNodes bootNodes = new BootNodes();

	public WebSocketConfiguration getWebSocketConfiguration()
	{
		return webSocketConfiguration;
	}

	@JsonProperty("bootnodes")
	public BootNodes getBootNodes()
	{
		return bootNodes;
	}

	@JsonProperty("bootnodes")
	public void setBootNodes(BootNodes bootNodes)
	{
		this.bootNodes = bootNodes;
	}
}
