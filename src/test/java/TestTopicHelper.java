import ch.microfast.hivemq.smoker.authz.common.TopicHelper;
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
import static org.junit.Assert.assertTrue;

public class TestTopicHelper {

    private SmokerJsonSerializer mapper;

    private Claim testClaim;

    @Before
    public void SetUp() {

    }

    @Test
    public void testSerializeDeserializeClaim() throws IOException {
        // Arrange
        String clientId = "FE5AAMRMNQZYEAJYUCI7WUGZWH7ITCYB7LX3PVFD32NOZQ56KUIQ====";
        String topic = "restricted/" + clientId + "/claims";

        // Act
        boolean isOwned = TopicHelper.IsTopicOwnedByOwner(topic, clientId);
        boolean isEqual = TopicHelper.TopicSegmentIsEqualTo(topic, 2, "claims");

        // Assert
        assertTrue(isEqual);
        assertTrue(isOwned);
    }
}
