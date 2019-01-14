package de.hsma.ppr.blockchain.nodes.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;

import de.hsma.ppr.blockchain.exception.ByteConversionFailedExcetion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class Peer
{
	private String address;

	public static Peer forAddress(String address)
	{
		Peer peer = new Peer();
		peer.setAddress(address);
		return peer;
	}

	@JsonProperty
	public String getAddress()
	{
		return address;
	}

	@JsonProperty
	public void setAddress(String address)
	{
		this.address = address;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Peer peer = (Peer) o;
		return Objects.equals(address, peer.address);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(address);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
		                  .add("address", address)
		                  .toString();
	}

	public byte[] asJsonByteArray()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			new ObjectMapper().writeValue(out, this);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	public static Set<Peer> fromJsonArray(byte[] body)
	{
		try
		{
			Peer[] peerArray = new ObjectMapper().readValue(body, Peer[].class);
			List<Peer> peerList = Arrays.asList(peerArray);
			return new HashSet<Peer>(peerList);
		} catch (IOException e)
		{
			throw new ByteConversionFailedExcetion(e);
		}
	}
}
