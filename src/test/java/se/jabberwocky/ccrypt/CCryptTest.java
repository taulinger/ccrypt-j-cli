package se.jabberwocky.ccrypt;

import se.jabberwocky.ccryptj.CCrypt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCryptTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @BeforeAll
    public static void addBouncyCastleProvider() {
        // TODO move to configuration files
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void encrypt_file_and_delete() throws IOException, InvalidKeySpecException {
        URL url = getClass().getResource("jabberwocky.txt");
        assertNotNull(url);
        File plain = File.createTempFile("jabberwocky-", ".txt");
        plain.deleteOnExit();

        File cipher = new File(plain.getAbsolutePath() + ".cpt");

        FileUtils.copyURLToFile(url, plain);

        CCrypt cCrypt = new CCrypt("through the looking glass");
        cCrypt.encrypt(plain);

        assertFalse(plain.exists());
        assertTrue(cipher.exists());
    }

    @Test
    public void decrypt_file_and_delete() throws IOException, InvalidKeySpecException {
        URL url = getClass().getResource("jabberwocky.txt.cpt");
        assertNotNull(url);
        File cipher = File.createTempFile("jabberwocky-", ".txt.cpt");
        cipher.deleteOnExit();
        String filename = cipher.getAbsolutePath();
        File plain = new File(filename.substring(0, filename.length() - ".cpt".length()));

        FileUtils.copyURLToFile(url, cipher);

        CCrypt cCrypt = new CCrypt("through the looking glass");
        cCrypt.decrypt(cipher);

        assertTrue(plain.exists());
        assertFalse(cipher.exists());
    }

}
