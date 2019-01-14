package de.hsma.ppr.blockchain.nodes.async;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Futures
{

	private static final Executor executor = Executors.newFixedThreadPool(2);

	private Futures()
	{
	}

	public static <V> void addCallback(
	                                   final ListenableFuture<V> future,
	                                   final FutureCallback<? super V> callback)
	{
		com.google.common.util.concurrent.Futures.addCallback(future, callback.toGuavaCallback(), executor);
	}

	public static <V> FutureCallback<V> callback()
	{
		return FutureCallback.callback();
	}
}
