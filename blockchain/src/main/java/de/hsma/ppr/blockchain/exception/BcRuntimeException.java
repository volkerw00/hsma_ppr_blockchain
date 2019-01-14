package de.hsma.ppr.blockchain.exception;

import java.io.IOException;

public class BcRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 5031858102115932406L;

	public BcRuntimeException(IOException e)
	{
		super(e);
	}
}
