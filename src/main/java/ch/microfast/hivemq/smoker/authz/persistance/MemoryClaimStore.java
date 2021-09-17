package ch.microfast.hivemq.smoker.authz.persistance;

import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MemoryClaimStore implements IClaimStore {

    private static final Logger log = LoggerFactory.getLogger(MemoryClaimStore.class);

    private final List<Claim> store = Collections.synchronizedList(new ArrayList<>());

    private final IClaimValidator claimValidator;

    @Inject
    public MemoryClaimStore(IClaimValidator claimValidator) {
        this.claimValidator = claimValidator;
    }

    @Override
    public synchronized void upsert(Claim claim) throws InvalidClaimException {
        claimValidator.validateClaim(claim);

        log.debug("Upserting claim:=" + claim.toString());
        String owner = claim.getRestriction().getOwner();
        String topic = claim.getRestriction().getTopicName();

        // try to find existing claim to delete
        Claim existingClaim = store.stream()
                .filter(f -> f.getRestriction().getOwner().equals(owner) && f.getRestriction().getTopicName().equals(topic))
                .findFirst()
                .orElse(null);

        if (existingClaim != null) {
            log.debug("Existing claim found which will be overridden. existingClaim:=" + existingClaim);
            store.remove(existingClaim);
        }

        store.add(claim);
    }

    @Override
    public synchronized void delete(String owner, String topicName) {
        log.debug("Deleting claim by owner:=" + owner + " and topicName:=" + topicName);
        // try to find existing claim to delete
        Claim existingClaim = store.stream()
                .filter(f -> f.getRestriction().getOwner().equals(owner) && f.getRestriction().getTopicName().equals(topicName))
                .findFirst()
                .orElse(null);

        store.remove(existingClaim);
    }

    @Override
    public Collection<Claim> find(Predicate<Claim> filterPredicate) {
        return store.stream().filter(filterPredicate).collect(Collectors.toList());
    }

    @Override
    public Collection<Claim> getAll() {
        return Collections.unmodifiableList(store);
    }
}
