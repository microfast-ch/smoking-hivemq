package ch.microfast.hivemq.smoker.authz.domain;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * DTO to hold a clients owned and involved claims
 */
public class ClientClaimsDto {
    private String clientId;
    private List<Claim> ownedClaims;
    private List<Claim> involvedClaims;

    /**
     * @param clientId the client ID to hold the owned and involved Claims from
     */
    public ClientClaimsDto(String clientId) {
        this.clientId = clientId;
        this.ownedClaims = new ArrayList<>();
        this.involvedClaims = new ArrayList<>();
    }

    /**
     * Add a Claim to the list of owned claims for this client
     */
    public void addOwned(Claim claim) {
        validateOwnerClaim(claim);
        this.ownedClaims.add(claim);
    }

    /**
     * Add a list of claims to the list of owned claims for this client
     */
    public void addOwned(Collection<Claim> claims) {
        if (claims == null || claims.isEmpty()) {
            return;
        }

        for (Claim claim : claims) { addOwned(claim); }
    }

    /**
     * Add a Claim to the list of involved claims for this client
     */
    public void addInvolved(Claim claim) {
        validateInvolvedClaim(claim);
        this.involvedClaims.add(claim);
    }

    /**
     * Add a list of claims to the list of involved claims for this client
     */
    public void addInvolved(Collection<Claim> claims) {
        if (claims == null || claims.isEmpty()) {
            return;
        }

        for (Claim claim : claims) { addInvolved(claim); }
    }

    /**
     * Return the owned and involved Claims in a combined list.
     * @return a read-only claim list
     */
    public List<Claim> getAllClaims() {
        List<Claim> allClaims = new ArrayList<>();
        allClaims.addAll(ownedClaims);
        allClaims.addAll(involvedClaims);

        return Collections.unmodifiableList(allClaims);
    }

    private void validateOwnerClaim(Claim claim) {
        if (claim == null) {
            throw new IllegalArgumentException("Claim must not be null");
        }

        if (!claim.getRestriction().getOwner().equals(clientId)) {
            throw new IllegalArgumentException("Claim must be owned by clientId:=" + clientId);
        }
    }

    private void validateInvolvedClaim(Claim claim) {
        if (claim == null) {
            throw new IllegalArgumentException("Claim must not be null");
        }

        if (claim.getRestriction().getOwner().equals(clientId)) {
            throw new IllegalArgumentException("Claim must not be owned by clientId:=" + clientId);
        }

        Predicate<Permission> filterInvolved = (p)  -> p.getClientId().equals(clientId)
                || p.getClientId().equals(AuthorizationConsts.ANY_CLIENT_IDENTIFIER);

        if (claim.getRestriction().getPermissions().stream().noneMatch(filterInvolved)) {
            throw new IllegalArgumentException("Claim is not involved with clientId:=" + clientId);
        }
    }
}
