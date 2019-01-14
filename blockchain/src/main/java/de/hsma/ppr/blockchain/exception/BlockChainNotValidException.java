package de.hsma.ppr.blockchain.exception;

public class BlockChainNotValidException extends BcException
{
	private static final long serialVersionUID = 1L;

	public BlockChainNotValidException()
	{
		super("The supplied blocks do not build a valid blockchain");
	}
}
