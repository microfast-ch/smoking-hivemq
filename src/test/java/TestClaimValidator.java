import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.domain.Permission;
import ch.microfast.hivemq.smoker.authz.domain.Restriction;
import ch.microfast.hivemq.smoker.authz.domain.RestrictionType;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.authz.validation.ClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import ch.microfast.hivemq.smoker.crypto.EddsaCryptoProvider;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class TestClaimValidator extends TestBase {

    private ICryptoProvider cryptoProvider;

    private ClaimValidator claimValidator;

    private SmokerJsonSerializer jsonSerializer;

    @Before
    public void SetUp() throws NoSuchAlgorithmException {
        cryptoProvider = new EddsaCryptoProvider();
        claimValidator = new ClaimValidator(cryptoProvider);
        jsonSerializer = new SmokerJsonSerializer();
    }

    @Test
    public void testValidateValidClaim() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidClaimException {
        // Arrange
        KeyPair keyPair = cryptoProvider.generateKeyPair();
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keyPair.getPublic();
        String owner = new Base32().encodeAsString(publicKey.getAbyte());

        Permission permission = new Permission();
        permission.setClientId("*");
        permission.setActivity(TopicPermission.MqttActivity.PUBLISH);

        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/" + owner + "/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);
        restriction.addPermission(permission);

        byte[] signaturePayload = jsonSerializer.writeValueAsString(restriction).getBytes(StandardCharsets.UTF_8);
        var signature = cryptoProvider.sign(keyPair.getPrivate(), signaturePayload);

        Claim validClaim = new Claim();
        validClaim.setSignature(Base64.encodeBase64String(signature));
        validClaim.setRestriction(restriction);

        // Act
        claimValidator.validateClaim(validClaim);

        // Assert
        // if no exception is thrown the validation is OK
    }

    @Test
    public void testValidateInvalidClaim() throws InvalidClaimException {
        // Arrange
        Claim validClaim = new Claim();
        validClaim.setSignature(null);
        validClaim.setRestriction(null);

        exceptionRule.expect(InvalidClaimException.class);

        // Act
        claimValidator.validateClaim(validClaim);
    }

    @Test
    public void testValidateInvalidRestriction() throws InvalidClaimException {
        // Arrange &
        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);

        Claim invalidClaim = new Claim();
        invalidClaim.setSignature("ABCDEFGH");
        invalidClaim.setRestriction(restriction);

        exceptionRule.expect(InvalidClaimException.class);

        // Act
        claimValidator.validateClaim(invalidClaim);
    }

    @Test
    public void testValidateInvalidPermission() throws InvalidClaimException {
        // Arrange &
        Permission permission = new Permission();
        permission.setClientId(null);
        permission.setActivity(null);

        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);
        restriction.addPermission(permission);

        Claim invalidClaim = new Claim();
        invalidClaim.setSignature("ABCDEFGH");
        invalidClaim.setRestriction(restriction);

        exceptionRule.expect(InvalidClaimException.class);

        // Act
        claimValidator.validateClaim(invalidClaim);
    }


}
