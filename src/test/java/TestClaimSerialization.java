import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.domain.Permission;
import ch.microfast.hivemq.smoker.authz.domain.Restriction;
import ch.microfast.hivemq.smoker.authz.domain.RestrictionType;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestClaimSerialization {

    private SmokerJsonSerializer mapper;

    private Claim testClaim;

    @Before
    public void SetUp() {
        mapper = new SmokerJsonSerializer();

        Permission permission = new Permission();
        permission.setClientId("*");
        permission.setActivity(TopicPermission.MqttActivity.PUBLISH);

        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);
        restriction.addPermission(permission);

        testClaim = new Claim();
        testClaim.setSignature("ABCDEFGH");
        testClaim.setRestriction(restriction);
    }

    @Test
    public void testSerializeDeserializeClaim() throws IOException {
        // Arrange & Act
        String claimJson = mapper.writeValueAsString(testClaim);
        Claim claim = mapper.readValue(claimJson, Claim.class);

        // Assert
        assertEquals(testClaim, claim);
    }
}
