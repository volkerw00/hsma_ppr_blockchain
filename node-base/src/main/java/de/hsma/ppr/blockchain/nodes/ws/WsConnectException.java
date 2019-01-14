package de.hsma.ppr.blockchain.nodes.ws;

public class WsConnectException extends WsException
{
	private static final long serialVersionUID = 1L;

	public WsConnectException(String target, Throwable cause)
	{
		super(String.format("Failed to establish websocket connection to %s.", target), cause);
	}
}
