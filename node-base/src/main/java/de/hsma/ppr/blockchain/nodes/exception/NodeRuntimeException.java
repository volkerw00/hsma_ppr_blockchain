package de.hsma.ppr.blockchain.nodes.exception;

public class NodeRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NodeRuntimeException(Throwable cause)
	{
		super(cause);
	}
}
