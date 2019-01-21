package de.hsma.ppr.blockchain.nodes.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

import de.hsma.ppr.blockchain.core.Block;
import de.hsma.ppr.blockchain.nodes.peers.Peers;

public class DataPool
{
	private final LinkedList<Map<String, String>>	data	= new LinkedList<>();
	private final Peers														peers;

	@Nullable
	private Map<String, String> currentData;

	private DataPool(Peers peers)
	{
		this.peers = peers;
	}

	public static DataPool withPeers(Peers peers)
	{
		return new DataPool(peers);
	}

	public void save(Map<String, String> data)
	{
		this.data.add(data);
		broadCastData(data);
	}

	private void broadCastData(Map<String, String> data)
	{
		peers.broadCastNewData(data);
	}

	public synchronized Map<String, String> nextData()
	{
		if (currentData != null)
		{
			return currentData;
		} else
		{
			try
			{
				currentData = data.removeFirst();

			} catch (NoSuchElementException e)
			{
				currentData = new HashMap<>();
			}
			return nextData();
		}
	}

	public synchronized void removeData(Block block)
	{
		data.remove(block.data());
		currentData = null;
	}
}
