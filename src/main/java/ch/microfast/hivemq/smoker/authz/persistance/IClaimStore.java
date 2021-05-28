package ch.microfast.hivemq.smoker.authz.persistance;

import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Implementations can hold a set {@link Claim}s. Implementations should also make sure that claims are validated
 * properly when reading as well as writing as this is a source of truth for services.
 *
 * Productive implementation should be able to proceed reads very efficient (caching).
 */
public interface IClaimStore {

    void upsert(Claim claim) throws InvalidClaimException;

    void delete(String owner, String topicName);

    Collection<Claim> find(Predicate<Claim> filterPredicate);

    Collection<Claim> getAll();

}
