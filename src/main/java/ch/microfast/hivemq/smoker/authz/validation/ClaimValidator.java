package ch.microfast.hivemq.smoker.authz.validation;

import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimValidator implements IClaimValidator {

    private static final Logger log = LoggerFactory.getLogger(ClaimValidator.class);

    private final Validator validator;

    private final ICryptoProvider cryptoProvider;

    @Inject
    public ClaimValidator(ICryptoProvider cryptoProvider) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.cryptoProvider = cryptoProvider;
    }

    @Override
    public void validateClaim(Claim claim) throws InvalidClaimException {
        List<String> errorMessages = new ArrayList<String>();

        // base validation (claim, restriction, permission)
        Set<ConstraintViolation> baseValidationResult = new HashSet<>();
        baseValidationResult.addAll(validator.validate(claim));

        if (claim.getRestriction() != null) {
            baseValidationResult.addAll(validator.validate(claim.getRestriction()));
        }

        if (claim.getRestriction() != null && claim.getRestriction().getPermissions() != null) {
            claim.getRestriction().getPermissions().stream().forEach(perm -> baseValidationResult.addAll(validator.validate(perm)));
        }

        if (!baseValidationResult.isEmpty()) {
            baseValidationResult.stream().forEach(result -> errorMessages.add(result.getPropertyPath().toString() + " " + result.getMessage()));
        }

        // only do further validation if base validations succeed
        if (errorMessages.isEmpty()) {

            // validate signature
            try {
                byte[] signature = Base64.decodeBase64(claim.getSignature());
                byte[] clientIdArray = new Base32().decode(claim.getRestriction().getOwner());
                byte[] expect = new SmokerJsonSerializer().writeValueAsString(claim.getRestriction()).getBytes(StandardCharsets.UTF_8);

                PublicKey publicKey = cryptoProvider.convertByteArrayToPublicKey(clientIdArray);

                if (!cryptoProvider.verify(publicKey, signature, expect)) {
                    errorMessages.add("The claims signature could not be verified successfully");
                }
            } catch (IOException | InvalidKeyException | SignatureException | IllegalArgumentException e) {
                String msg = MessageFormat.format("Exception while verifying the claims signature. Message:={0}", e.getMessage());
                log.error(msg, e);
                errorMessages.add(msg);
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new InvalidClaimException(errorMessages);
        }
    }
}
