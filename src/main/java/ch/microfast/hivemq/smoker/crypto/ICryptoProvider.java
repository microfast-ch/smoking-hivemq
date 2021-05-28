package ch.microfast.hivemq.smoker.crypto;

import java.security.*;

/**
 * The {@link ICryptoProvider} provides all crypto functions needed to implement the SMOKER authentication and
 * authorization approach.
 */
public interface ICryptoProvider {

    /**
     * Generates a classic {@link KeyPair}
     *
     * @return the newly generated {@link KeyPair}
     */
    KeyPair generateKeyPair();

    /**
     * Sign a given message. The signature scheme is defined by the underlying implementation.
     *
     * @param privateKey the signing secret key part of the initial generated {@link KeyPair}
     * @param message    the message to be signed
     * @return a byte array representing the singature
     * @throws InvalidKeyException if the given private key does not match to the defined signature scheme
     * @throws SignatureException  if signing fails
     */
    byte[] sign(PrivateKey privateKey, byte[] message) throws InvalidKeyException, SignatureException;

    /**
     * Verify a given signature. The signature scheme is defined by the underlying implementation.
     *
     * @param publicKey       the verifiying public key part of the initial generated {@link KeyPair}
     * @param signature       the signature to be verified
     * @param expectedMessage the expected messages which was signed
     * @return true if the verification was successful and false otherwise
     * @throws InvalidKeyException if the given public key does not match to the defined signature scheme
     * @throws SignatureException  if the given signature is not verifieable by the defined signature scheme
     */
    boolean verify(PublicKey publicKey, byte[] signature, byte[] expectedMessage) throws InvalidKeyException, SignatureException;

    /**
     * Generates random bytes.
     *
     * @param bytes the byte count that should be generated
     * @return the generated random byte array
     */
    byte[] generateRandomBytes(int bytes);

    /**
     * Converts a byte array to a valid public key that fits to the defined signature scheme
     *
     * @param publicKeyBytes the public key as a byte array
     * @return the converted public key
     */
    PublicKey convertByteArrayToPublicKey(byte[] publicKeyBytes);
}
