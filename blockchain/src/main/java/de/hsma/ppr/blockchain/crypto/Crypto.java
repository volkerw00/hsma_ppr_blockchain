package de.hsma.ppr.blockchain.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Crypto
{
	private static Crypto						INSTANCE;
	private final KeyPairGenerator	keyPairGenerator;

	private Crypto(KeyPairGenerator keyPairGenerator)
	{
		this.keyPairGenerator = keyPairGenerator;
	}

	public static KeyPair createKeyPair()
	{
		return Crypto.getInstance().generateKeyPair();
	}

	private KeyPair generateKeyPair()
	{
		return keyPairGenerator.generateKeyPair();
	}

	private static Crypto getInstance()
	{
		if (INSTANCE != null) { return INSTANCE; }
		Security.addProvider(new BouncyCastleProvider());

		INSTANCE = new Crypto(getKeyPairGenerator());
		return INSTANCE;
	}

	private static KeyPairGenerator getKeyPairGenerator()
	{
		ECGenParameterSpec secp256k1 = new ECGenParameterSpec("secp256k1");
		KeyPairGenerator keyPairGenerator = null;
		try
		{
			keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
			keyPairGenerator.initialize(secp256k1, new SecureRandom());
		} catch (GeneralSecurityException e)
		{
			// TODO
			throw new RuntimeException(e);
		}
		return keyPairGenerator;
	}
}
