package de.hsma.ppr.blockchain.nodes.ws;

public class WsRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public WsRuntimeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public WsRuntimeException(Throwable cause)
	{
		super(cause);
	}
}
