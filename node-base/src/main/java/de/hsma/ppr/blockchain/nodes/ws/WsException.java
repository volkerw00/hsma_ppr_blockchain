package de.hsma.ppr.blockchain.nodes.ws;

public class WsException extends Exception
{
	private static final long serialVersionUID = 1L;

	public WsException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
