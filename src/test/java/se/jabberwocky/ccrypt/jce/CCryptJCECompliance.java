package se.jabberwocky.ccrypt.jce;

import se.jabberwocky.ccryptj.jce.CCryptSecretKeyFactorySpi;
import se.jabberwocky.ccryptj.jce.CCryptKeySpec;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCryptJCECompliance {

	private Logger log = LoggerFactory.getLogger(getClass());

	private SecretKey key;

	private Cipher cipher;

	private byte[] ciphertext = new byte[32];

	@BeforeAll
	public static void addBouncyCastleProvider() {
		// TODO move to configuration files
		Security.addProvider(new BouncyCastleProvider());
	}

	@Test
	public void aes256BitCompliance() throws InvalidAlgorithmParameterException, IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeySpecException {

		assertThrows(InvalidAlgorithmParameterException.class, () -> {
			CCryptSecretKeyFactorySpi keyFactory = new CCryptSecretKeyFactorySpi();
			key = keyFactory.engineGenerateSecret(new CCryptKeySpec("through the looking glass"));

			cipher = Cipher.getInstance("AES/CFB/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
			// ccrypt requires an IV of 32 bytes but AES only supports 16 bytes.
			// Thus a the compliant AES JCE implementation cannot be used. The
			// following method thus provokes an InvalidAlgorithmParameterException
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ciphertext, 0, 32));
		});
	}

	@Test
	public void checkUnlimitedStrength() throws NoSuchAlgorithmException {
		log.info("Max allowed key length is {} bits for AES", Cipher.getMaxAllowedKeyLength("AES"));
		assertFalse(Cipher.getMaxAllowedKeyLength("AES") < 256, "Unlimited cryptographic strength unavailable");
	}

}
