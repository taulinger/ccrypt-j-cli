package se.jabberwocky.ccryptj;

import java.io.IOException;
import java.io.InputStream;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Decrypts a ccrypt InputStream.
 */
public final class CCryptInputStream extends InputStream {

    private final InputStream source;
    private final BufferedBlockCipher blockCipher;
    private byte[] cipherText = new byte[32];
    private byte[] plainText = new byte[32];
    private int index;
    private int bytesInBuffer;

    private final boolean verifyMagic;

    public CCryptInputStream(SecretKey key, InputStream source)
            throws IOException {
        this(key, source, true);
    }

    public CCryptInputStream(SecretKey key, InputStream source, boolean verify)
            throws IOException {
        this(new RijndaelEngine(256), key, source, verify);
    }

    CCryptInputStream(RijndaelEngine engine, SecretKey key,
                      InputStream source) throws IOException {
        this(engine, key, source, false);
    }

    CCryptInputStream(RijndaelEngine engine, SecretKey key,
                      InputStream source, boolean verify) throws IOException {

        //engine.reset();

        this.source = source;
        this.verifyMagic = verify;

        CFBBlockCipher cfb = new CFBBlockCipher(engine, 256);
        blockCipher = new BufferedBlockCipher(cfb);

        CipherParameters keyParam = new KeyParameter(key.getEncoded());

        engine.init(false, keyParam);
        if (source.read(cipherText, 0, 32) < 32) {
            throw new IOException("Could only read " + bytesInBuffer
                    + " bytes from ccrypt InputStream for the "
                    + "Initializing Vector (IV) before end of file was"
                    + "reached; the ccrypt stream should be at least "
                    + "32 bytes long, i.e. larger than the size of the IV");
        }
        engine.processBlock(cipherText, 0,plainText, 0 );
        assertMagic();


        CipherParameters params = new ParametersWithIV(keyParam, cipherText.clone());
        blockCipher.init(false, params);
        readAndDecryptCipherBlock();
    }

    private void assertMagic() {
        if (verifyMagic) {
            for (int i = 0; i < CCryptConstants.CCRYPT_MAGIC_BYTES.length; i++) {
                if (CCryptConstants.CCRYPT_MAGIC_BYTES[i] != plainText[i]) {
                    throw new IllegalArgumentException("InputStream Magic "
                            + "Number does not match; expected '" +
                            new String(CCryptConstants.CCRYPT_MAGIC_NUMBER) +
                            "', found '" +
                            new String(plainText, 0, 4)
                            + "'; wrong version of ccrypt?");
                }
            }
        }
    }

    // -- InputStream

    @Override
    public int read() throws IOException {

        if (index < bytesInBuffer) {
            return Byte.toUnsignedInt(plainText[index++]);
        }

        if (index == 32) {
            readAndDecryptCipherBlock();
            if (bytesInBuffer == 0) {
                return -1;
            } else {
                return Byte.toUnsignedInt(plainText[index++]);
            }
        }

        return -1;
    }

    // -- CCryptInputStream

    public boolean isVerifyMagic() {
        return verifyMagic;
    }

    private final void readAndDecryptCipherBlock() throws IOException {
        bytesInBuffer = 0;
        index = 0;
        int len = 0;

        while (len != -1 && bytesInBuffer < 32) {
            len = source.read(cipherText, bytesInBuffer, 32 - bytesInBuffer);
            if (len == -1) {
                break;
            }
            bytesInBuffer += len;
        }

        len = blockCipher.processBytes(cipherText, 0, bytesInBuffer, plainText, 0);
        if (len < 32) {
            try {
                blockCipher.doFinal(plainText, len);
            } catch (DataLengthException | IllegalStateException
                    | InvalidCipherTextException e) {
                throw new IOException("Could not process final byte", e);
            }
        }

    }

}
