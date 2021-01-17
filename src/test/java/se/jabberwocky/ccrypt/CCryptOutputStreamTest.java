package se.jabberwocky.ccrypt;

import se.jabberwocky.ccryptj.CCryptInputStream;
import se.jabberwocky.ccryptj.CCryptOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import se.jabberwocky.ccryptj.jce.CCryptKeySpec;
import se.jabberwocky.ccryptj.jce.CCryptSecretKeyFactorySpi;

public class CCryptOutputStreamTest {

    private SecretKey key;
    private byte[] expected;
    private byte[] actual;
    private CCryptSecretKeyFactorySpi keyFactory;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException, IOException,
            InvalidKeySpecException {

        CCryptKeySpec spec = new CCryptKeySpec("through the looking glass");
        keyFactory = new CCryptSecretKeyFactorySpi();
        key = keyFactory.engineGenerateSecret(spec);

        InputStream in = getClass().getResourceAsStream("jabberwocky.txt");
        assertNotNull(in, "Plaintext source cannot be null!");
        expected = IOUtils.toByteArray(in);
    }

    @Test
    public void encrypt_decrypt() throws IOException {

        ByteArrayOutputStream outbuffer = new ByteArrayOutputStream();
        CCryptOutputStream output = new CCryptOutputStream(key, outbuffer);

        IOUtils.write(expected, output);
        output.close();
        byte[] ciphertext = outbuffer.toByteArray();

        assertEquals(expected.length + 32, ciphertext.length, "Ciphertext should be the same length as the plaintext "
                + "plus 32 bytes");

        ByteArrayInputStream inbuffer = new ByteArrayInputStream(ciphertext);
        CCryptInputStream input = new CCryptInputStream(key, inbuffer, true);

        actual = IOUtils.toByteArray(input);

        String actualString = new String(actual);
        String expectedString = new String(expected);

        assertEquals(expectedString.length(), actualString.length());
        assertEquals(expectedString, actualString);
    }

}
