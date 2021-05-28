import ch.microfast.hivemq.smoker.crypto.EddsaCryptoProvider;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.junit.Before;
import org.junit.Test;

import java.security.*;

import static org.junit.Assert.*;

public class TestEddsaCryptoProvider {

    private ICryptoProvider cryptoProvider;

    @Before
    public void SetUp() throws NoSuchAlgorithmException {
        cryptoProvider = new EddsaCryptoProvider();
    }

    @Test
    public void testGenerateKeyPair() {
        // Arrange & Act
        KeyPair keyPair = cryptoProvider.generateKeyPair();

        // Assert
        assertNotNull(keyPair);
        assertTrue(keyPair.getPublic() instanceof EdDSAPublicKey);
        assertTrue(keyPair.getPrivate() instanceof EdDSAPrivateKey);
    }

    @Test
    public void testSignVerify() throws SignatureException, InvalidKeyException {
        // Arrange
        String correctMessage = "this is my test message";
        String wrongMessage = "worong message";
        KeyPair keyPair = cryptoProvider.generateKeyPair();

        // Act
        byte[] signature = cryptoProvider.sign(keyPair.getPrivate(), correctMessage.getBytes());
        boolean verifyCorrect = cryptoProvider.verify(keyPair.getPublic(), signature, correctMessage.getBytes());
        boolean verifyWrong = cryptoProvider.verify(keyPair.getPublic(), signature, wrongMessage.getBytes());

        // Assert
        assertNotNull(signature);
        assertTrue(verifyCorrect);
        assertFalse(verifyWrong);
    }

    @Test
    public void testGenerateRandomBytes() {
        // Arrange
        int byteCount = 32;

        // Arrange & Act
        byte[] bytes = cryptoProvider.generateRandomBytes(byteCount);

        // Assert
        assertNotNull(bytes);
        assertEquals(byteCount, bytes.length);
    }

    @Test
    public void testConvertByteArrayToPublicKey() throws SignatureException, InvalidKeyException {
        // Arrange
        String correctMessage = "this is my test message";
        KeyPair keyPair = cryptoProvider.generateKeyPair();
        byte[] signature = cryptoProvider.sign(keyPair.getPrivate(), correctMessage.getBytes());
        EdDSAPublicKey aPublic = (EdDSAPublicKey) keyPair.getPublic();
        byte[] publicKeyBytes = aPublic.getAbyte();

        // Act
        PublicKey publicKey = cryptoProvider.convertByteArrayToPublicKey(publicKeyBytes);
        boolean verify = cryptoProvider.verify(publicKey, signature, correctMessage.getBytes());

        // Assert
        assertTrue(publicKey instanceof EdDSAPublicKey);
        assertTrue(verify);
    }
}
