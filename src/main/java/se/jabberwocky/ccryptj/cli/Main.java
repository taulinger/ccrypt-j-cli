package se.jabberwocky.ccryptj.cli;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import se.jabberwocky.ccryptj.CCrypt;

/**
 *
 * @author thomas
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InvalidKeySpecException, IOException {

        CCrypt crypto = new CCrypt("secret");
        crypto.encrypt(new File("/home/thomas/Schreibtisch/test.mp3"));
    }

}
