import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.domain.Permission;
import ch.microfast.hivemq.smoker.authz.domain.Restriction;
import ch.microfast.hivemq.smoker.authz.domain.RestrictionType;
import ch.microfast.hivemq.smoker.authz.persistance.MemoryClaimStore;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.authz.services.AuthzService;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import ch.microfast.hivemq.smoker.crypto.EddsaCryptoProvider;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.junit.Assert.assertEquals;

public class TestAuthzService extends TestBase {

    private ICryptoProvider _cryptoProvider;

    @Mock
    private IClaimValidator _claimValidator;

    private AuthzService _authzService;

    @Before
    public void SetUp() throws NoSuchAlgorithmException {
        _cryptoProvider = new EddsaCryptoProvider();
        _authzService = new AuthzService(new MemoryClaimStore(_claimValidator));
    }

    @Test
    public void testFindClaims() throws IOException, SignatureException, InvalidKeyException, InvalidClaimException {
        // Arrange
        KeyPair keyPair = _cryptoProvider.generateKeyPair();
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keyPair.getPublic();

        Permission permission = new Permission();
        permission.setClientId("*");
        permission.setActivity(TopicPermission.MqttActivity.PUBLISH);

        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);
        restriction.addPermission(permission);

        byte[] signaturePayload = new SmokerJsonSerializer().writeValueAsBytes(restriction);
        var signature = _cryptoProvider.sign(keyPair.getPrivate(), signaturePayload);

        Claim validClaim = new Claim();
        validClaim.setSignature(Base64.encodeBase64String(signature));
        validClaim.setRestriction(restriction);

        // Act
        _authzService.claim(validClaim);
        var claims = _authzService.getClaimsForClient(restriction.getOwner());

        // Assert
        assertEquals(1, claims.getAllClaims().size());
    }
}
