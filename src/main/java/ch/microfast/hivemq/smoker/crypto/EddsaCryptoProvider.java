package ch.microfast.hivemq.smoker.crypto;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import java.security.*;
import java.util.Arrays;

/**
 * The implementation of a {@link ICryptoProvider} with a EdDSA signature scheme.
 */
public class EddsaCryptoProvider implements ICryptoProvider {

    /**
     * We are working with EdDSA signatures that are 64 bytes in length.
     */
    private static final int SIGNATURE_BYTE_COUNT = 64;
    private final Signature signature;
    private final KeyPairGenerator keyPairGenerator;
    private final SecureRandom random;
    private final EdDSAParameterSpec parameterSpec;

    /**
     * Initializes the crypto provider with a EdDSA (on curve 25519) signature scheme using the SHA-512 message digest.
     *
     * @throws NoSuchAlgorithmException thrown if the systen cannot initialize the EdDSA signature scheme correctly
     */
    public EddsaCryptoProvider() throws NoSuchAlgorithmException {
        this.keyPairGenerator = new KeyPairGenerator();
        this.random = new SecureRandom();
        this.signature = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
        this.parameterSpec = EdDSANamedCurveTable.getByName("ed25519");
    }

    @Override
    public KeyPair generateKeyPair() {
        return keyPairGenerator.generateKeyPair();
    }

    @Override
    public byte[] sign(PrivateKey privateKey, byte[] message) throws InvalidKeyException, SignatureException {
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign();
    }

    @Override
    public boolean verify(PublicKey publicKey, byte[] signature, byte[] expectedMessage) throws InvalidKeyException, SignatureException {
        byte[] signatureBytes;

        // Expecting a 64 byte EdDSA signature of a message
        // Some EdDSA implementations append the message to the signature bytes - therefore cut this suffix if so
        if (signature != null && signature.length > SIGNATURE_BYTE_COUNT) {
            signatureBytes = Arrays.copyOfRange(signature, 0, SIGNATURE_BYTE_COUNT);
        } else {
            signatureBytes = signature;
        }

        this.signature.initVerify(publicKey);
        this.signature.update(expectedMessage);
        return this.signature.verify(signatureBytes);
    }

    @Override
    public byte[] generateRandomBytes(int byteCount) {
        byte[] bytes = new byte[byteCount];
        random.nextBytes(bytes);
        return bytes;
    }

    @Override
    public PublicKey convertByteArrayToPublicKey(byte[] publicKeyBytes) {
        EdDSAPublicKeySpec spec = new EdDSAPublicKeySpec(publicKeyBytes, parameterSpec);
        return new EdDSAPublicKey(spec);
    }
}
