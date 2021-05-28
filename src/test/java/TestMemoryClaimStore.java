import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.domain.Restriction;
import ch.microfast.hivemq.smoker.authz.domain.RestrictionType;
import ch.microfast.hivemq.smoker.authz.persistance.MemoryClaimStore;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;

public class TestMemoryClaimStore extends TestBase {
    private MemoryClaimStore memoryClaimStore;

    @Mock
    private IClaimValidator claimValidator;

    @Before
    public void SetUp() {
        memoryClaimStore = new MemoryClaimStore(claimValidator);
    }

    @Test
    public void testInsert() throws InvalidClaimException {
        // Arrange
        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);

        Claim claim = new Claim();
        claim.setSignature("ABCDEFGH");
        claim.setRestriction(restriction);

        // Act
        memoryClaimStore.upsert(claim);

        // Assert
        assertEquals(1, memoryClaimStore.getAll().size());
    }

    @Test
    public void testUpsert() throws InvalidClaimException {
        // Arrange
        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);

        Claim claim = new Claim();
        claim.setSignature("ABCDEFGH");
        claim.setRestriction(restriction);

        // Act
        memoryClaimStore.upsert(claim);
        memoryClaimStore.upsert(claim);

        // Assert
        assertEquals(1, memoryClaimStore.getAll().size());
    }

    @Test
    public void testDelete() throws InvalidClaimException {
        // Arrange
        Restriction restriction = new Restriction();
        restriction.setTopicName("restricted/OWNER_CLIENT_ID/test/topic");
        restriction.setRestrictionType(RestrictionType.BLACKLIST);

        Claim claim = new Claim();
        claim.setSignature("ABCDEFGH");
        claim.setRestriction(restriction);

        // 1. Act & Assert
        memoryClaimStore.upsert(claim);
        assertEquals(1, memoryClaimStore.getAll().size());

        // 2. Act & Assert
        memoryClaimStore.delete(claim.getRestriction().getOwner(), claim.getRestriction().getTopicName());
        assertEquals(0, memoryClaimStore.getAll().size());
    }
}
