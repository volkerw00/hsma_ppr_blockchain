package de.hsma.ppr.blockchain.node.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.whispersystems.websocket.configuration.WebSocketConfiguration;

import de.hsma.ppr.blockchain.nodes.peers.Peers;

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

	@Valid
	@NotNull
	private Peers peers = new Peers();

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

	@JsonProperty("peers")
	public Peers getPeers()
	{
		return peers;
	}

	@JsonProperty("peers")
	public void setPeers(Peers peers)
	{
		this.peers = peers;
	}
}
