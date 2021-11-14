package se.jabberwocky.ccrypt.jce;

import java.security.spec.InvalidKeySpecException;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.jabberwocky.ccryptj.jce.CCryptKey;
import se.jabberwocky.ccryptj.jce.CCryptSecretKeyFactorySpi;
import se.jabberwocky.ccryptj.jce.CCryptKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CCryptKeyFactoryTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CCryptSecretKeyFactorySpi keyFactory;

	private CCryptKeySpec keySpec;

	private CCryptKey key;

	@BeforeEach
	public void setup() {
		keyFactory = new CCryptSecretKeyFactorySpi();
		keySpec = new CCryptKeySpec("Much ado about nothing!");
	}

	/**
	 * Test method for
	 * {@link se.jabberwocky.ccrypt.jce.CCryptSecretKeyFactorySpi#engineGenerateSecret(se.jabberwocky.ccrypt.jce.CCryptKeySpec)}
	 * .
	 * @throws InvalidKeySpecException
	 */
	@Test
	public void testGenerateKey() throws InvalidKeySpecException {
		key = keyFactory.engineGenerateSecret(keySpec);
		assertNotNull(key);
		log.debug("CCryptKey: '{}'", key);
	}

}
