package ch.microfast.hivemq.smoker.authn;

import ch.microfast.hivemq.smoker.authn.common.AuthenticationConsts;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import com.google.inject.name.Named;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.EnhancedAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthConnectInput;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthInput;
import com.hivemq.extension.sdk.api.auth.parameter.EnhancedAuthOutput;
import com.hivemq.extension.sdk.api.packets.general.DisconnectedReasonCode;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Optional;

/**
 * Implementation of the SMOKER enhanced authentication mechanism.
 * Check out <a href="https://arxiv.org/pdf/1904.00389.pdf">this paper</a> for further technical details.
 *
 * @author Cédric von Allmen
 */
public class SmokerEnhancedAuthenticator implements EnhancedAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(SmokerEnhancedAuthenticator.class);

    private static final int NONCE_BYTE_COUNT = 32;
    private final ICryptoProvider cryptoProvider;
    private byte[] generatedNonce = null;

    @Inject
    @Named("allow.non.smoker.clients")
    private boolean nonSmokerClientsAllowed;

    @Inject
    public SmokerEnhancedAuthenticator(ICryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
    }

    @Override
    public void onReAuth(@NotNull EnhancedAuthInput enhancedAuthInput, @NotNull EnhancedAuthOutput enhancedAuthOutput) {
        onAuth(enhancedAuthInput, enhancedAuthOutput);
    }

    @Override
    public void onConnect(@NotNull EnhancedAuthConnectInput enhancedAuthConnectInput, @NotNull EnhancedAuthOutput enhancedAuthOutput) {
        try {
            String clientId = enhancedAuthConnectInput.getConnectPacket().getClientId();
            @NotNull Optional<String> authenticationMethod = enhancedAuthConnectInput.getConnectPacket().getAuthenticationMethod();

            if (authenticationMethod.isPresent() && AuthenticationConsts.SMOKER_AUTH_METHOD.equalsIgnoreCase(authenticationMethod.get())) {
                this.generatedNonce = cryptoProvider.generateRandomBytes(NONCE_BYTE_COUNT);
                enhancedAuthOutput.continueAuthentication(this.generatedNonce);
                return;
            }

            if (nonSmokerClientsAllowed) {
                log.info("Non-Smoker client connection accepted. clientId:=" + clientId);
                enhancedAuthOutput.authenticateSuccessfully();
            } else {
                log.info("Non-smoker clients are not allowed to connect using the smoker plugin - the connection request is delegated to the next plugin. clientId:=" + clientId);
                enhancedAuthOutput.nextExtensionOrDefault();
            }
        } catch(Exception ex) {
            log.error("Unexpected exception", ex);
            throw ex;
        }
    }

    @Override
    public void onAuth(@NotNull EnhancedAuthInput enhancedAuthInput, @NotNull EnhancedAuthOutput enhancedAuthOutput) {
        try {

            String clientId = enhancedAuthInput.getClientInformation().getClientId();
            String authMethod = enhancedAuthInput.getAuthPacket().getAuthenticationMethod();

            if (AuthenticationConsts.SMOKER_AUTH_METHOD.equalsIgnoreCase(authMethod)) {
                @NotNull Optional<byte[]> authenticationDataAsArray = enhancedAuthInput.getAuthPacket().getAuthenticationDataAsArray();

                if (authenticationDataAsArray.isEmpty()) {
                    String reason = "Authentication data in AUTH packet missing - the signed nonce is expected";
                    log.info("Fail authentication. Reason:=" + reason + ", clientId:=" + clientId);
                    enhancedAuthOutput.failAuthentication(DisconnectedReasonCode.NOT_AUTHORIZED, reason);
                    return;
                }

                try {
                    byte[] publicKeyBytes = new Base32().decode(clientId);
                    PublicKey publicKey = cryptoProvider.convertByteArrayToPublicKey(publicKeyBytes);
                    boolean success = cryptoProvider.verify(publicKey, authenticationDataAsArray.get(), this.generatedNonce);
                    if (success) {
                        log.info("Smoker client connection accepted. clientId:=" + clientId);
                        enhancedAuthOutput.authenticateSuccessfully();
                        enhancedAuthInput.getConnectionInformation().getConnectionAttributeStore().putAsString(AuthenticationConsts.IS_SMOKER_AUTH_ATTRIBUTE_KEY, Boolean.TRUE.toString());
                    } else {
                        String failReason = "The provided signature could not be verified successfully";
                        failAuthenticationWithReason(enhancedAuthOutput, DisconnectedReasonCode.NOT_AUTHORIZED, failReason, clientId);
                    }
                } catch (InvalidKeyException e) {
                    String failReason = "The clientID is not a valid base32 representation of an EdDSA public key";
                    failAuthenticationWithReason(enhancedAuthOutput, DisconnectedReasonCode.CLIENT_IDENTIFIER_NOT_VALID, failReason, clientId);
                } catch (SignatureException e) {
                    String failReason = "The provided signature seems to be invalid";
                    failAuthenticationWithReason(enhancedAuthOutput, DisconnectedReasonCode.NOT_AUTHORIZED, failReason, clientId);
                }

                return;
            }

            enhancedAuthOutput.nextExtensionOrDefault();
        } catch(Exception ex) {
            log.error("Unexpected exception", ex);
            throw ex;
        }
    }

    private void failAuthenticationWithReason(EnhancedAuthOutput output, DisconnectedReasonCode reasonCode, String reasonString, String clientId) {
        log.info("Fail authentication. ReasonCode:=" + reasonCode.name() + " Reason:=" + reasonString + ", clientId:=" + clientId);
        output.failAuthentication(reasonCode, reasonString);
    }
}