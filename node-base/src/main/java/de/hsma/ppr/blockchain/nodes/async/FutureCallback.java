package de.hsma.ppr.blockchain.nodes.async;

import java.util.Optional;
import java.util.function.Consumer;

import org.checkerframework.checker.nullness.qual.Nullable;

public class FutureCallback<T>
{
	private Optional<Consumer<T>>					onSuccessCallback	= Optional.empty();
	private Optional<Consumer<Throwable>>	onFailureCallback	= Optional.of(t -> {
																														throw new RuntimeException(t);
																													});

	private com.google.common.util.concurrent.FutureCallback<T> guavaFutureCallback = new com.google.common.util.concurrent.FutureCallback<T>() {
		@Override
		public void onSuccess(@Nullable T result)
		{
			onSuccessCallback.ifPresent(callback -> callback.accept(result));
		}

		@Override
		public void onFailure(Throwable t)
		{
			onFailureCallback.ifPresent(callback -> callback.accept(t));
		}
	};

	private FutureCallback()
	{
	}

	public static <T> FutureCallback<T> callback()
	{
		return new FutureCallback<>();
	}

	public FutureCallback<T> onSuccess(Consumer<T> onSuccessCallback)
	{
		this.onSuccessCallback = Optional.of(onSuccessCallback);
		return this;
	}

	public FutureCallback<T> onFailure(Consumer<Throwable> onFailureCallback)
	{
		this.onFailureCallback = Optional.of(onFailureCallback);
		return this;
	}

	com.google.common.util.concurrent.FutureCallback<T> toGuavaCallback()
	{
		return guavaFutureCallback;
	}
}
