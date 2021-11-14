package se.jabberwocky.ccrypt;

import se.jabberwocky.ccryptj.CCryptInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.jabberwocky.ccryptj.jce.CCryptKeySpec;
import se.jabberwocky.ccryptj.jce.CCryptSecretKeyFactorySpi;

public class CCryptInputStreamTest {

	private SecretKey key;

	private byte[] expected;

	private byte[] actual;

	private CCryptSecretKeyFactorySpi keyFactory;

	@BeforeEach
	public void setup() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IOException,
			InvalidKeySpecException {

		keyFactory = new CCryptSecretKeyFactorySpi();
		CCryptKeySpec spec = new CCryptKeySpec("through the looking glass");
		key = keyFactory.engineGenerateSecret(spec);

		InputStream in = getClass().getResourceAsStream("jabberwocky.txt");
		assertNotNull(in, "Plaintext source cannot be null!");
		expected = IOUtils.toByteArray(in);
	}

	@Test
	public void test() throws IOException {
		InputStream in = getClass().getResourceAsStream("jabberwocky.txt.cpt");
		assertNotNull(in, "Ciphertext source cannot be null!");
		// TODO test with magic number validation
		CCryptInputStream ccryptStream = new CCryptInputStream(key, in, false);
		actual = IOUtils.toByteArray(ccryptStream);

		String actualString = new String(actual);
		String expectedString = new String(expected);

		assertEquals(expectedString.length(), actualString.length());
		assertEquals(expectedString, actualString);
	}

}
