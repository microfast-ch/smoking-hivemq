package ch.microfast.hivemq.smoker.authz.validation;

import ch.microfast.hivemq.smoker.authz.domain.Claim;

/**
 * Validator to validate an incoming claim. Could also contain complex business validations.
 */
public interface IClaimValidator {
    /**
     * Validates a if the claim is valid. Performs basic property validation as well as signature checks.
     *
     * @param claim the claim to be validated
     * @throws InvalidClaimException thrown if @link{@link Claim} is invalid. This exception also holds all error messages
     */
    void validateClaim(Claim claim) throws InvalidClaimException;
}
