package de.hsma.ppr.blockchain.core;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import de.hsma.ppr.blockchain.crypto.Crypto;

public class Wallet
{
	private final KeyPair keyPair;

	Wallet(KeyPair keyPair)
	{
		this.keyPair = keyPair;
	}

	public static Wallet create()
	{
		return new Wallet(Crypto.createKeyPair());
	}

	public static Wallet from(PrivateKey privateKey, PublicKey publicKey)
	{
		KeyPair keyPair = new KeyPair(publicKey, privateKey);
		return new Wallet(keyPair);
	}

	public String sign(byte[] data)
	{
		try
		{
			PrivateKey privateKey = keyPair.getPrivate();
			Signature sha256withECDSA = Signature.getInstance("SHA256withECDSA");
			sha256withECDSA.initSign(privateKey);
			sha256withECDSA.update(data);
			byte[] signature = sha256withECDSA.sign();
			return new BigInteger(1, signature).toString(16);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			// TODO
			throw new RuntimeException(e);
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
			// TODO
			throw new RuntimeException(e);
		} catch (SignatureException e)
		{
			e.printStackTrace();
			// TODO
			throw new RuntimeException(e);
		}
	}
}
