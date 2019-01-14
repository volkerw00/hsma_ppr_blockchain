package de.hsma.ppr.blockchain.exception;

import java.io.IOException;

public class ByteConversionFailedExcetion extends BcRuntimeException
{
	private static final long serialVersionUID = 1L;

	public ByteConversionFailedExcetion(IOException e)
	{
		super(e);
	}
}
