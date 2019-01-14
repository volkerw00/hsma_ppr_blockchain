package de.hsma.ppr.blockchain.bootnode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

class BootNodes
{

	@Valid
	@NotNull
	private String self;

	@JsonProperty("self")
	public String getSelf()
	{
		return self;
	}

	@JsonProperty("self")
	public void setSelf(String self)
	{
		this.self = self;
	}
}
