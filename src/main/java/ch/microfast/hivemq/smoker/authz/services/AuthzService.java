package ch.microfast.hivemq.smoker.authz.services;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;
import ch.microfast.hivemq.smoker.authz.common.TopicHelper;
import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.domain.ClientClaimsDto;
import ch.microfast.hivemq.smoker.authz.persistance.IClaimStore;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AuthzService implements IAuthzService {

    private static final Logger log = LoggerFactory.getLogger(AuthzService.class);

    private final IClaimStore claimStore;

    @Inject
    public AuthzService(IClaimStore claimStore) {
        this.claimStore = claimStore;
    }

    @Override
    public void claim(Claim claim) throws InvalidClaimException {
        log.info("Received claim:=" + claim.toString());

        // Persist claim
        claimStore.upsert(claim);
    }

    @Override
    public void unclaim(String owner, String topicName) {
        log.info("Received unclaim for owner:=" + owner + "and topic:=" + topicName);

        // Delete claim
        claimStore.delete(owner, topicName);
    }

    @Override
    public ClientClaimsDto getClaimsForClient(String clientId) {
        ClientClaimsDto result = new ClientClaimsDto(clientId);

        // Owned Claims
        Collection<Claim> ownedClaims = claimStore.find(c -> c.getRestriction().getOwner().equals(clientId));
        result.addOwned(ownedClaims);

        // Involved Claims
        Predicate<Claim> filterInvolved = c -> !c.getRestriction().getOwner().equals(clientId)
                && (c.getRestriction().getPermissions().stream().anyMatch(p -> p.getClientId().equals(clientId))
                || c.getRestriction().getPermissions().stream().anyMatch(p -> p.getClientId().equals(AuthorizationConsts.ANY_CLIENT_IDENTIFIER)));
        Collection<Claim> involvedClaims = claimStore.find(filterInvolved);
        result.addInvolved(involvedClaims);

        return result;
    }

    @Override
    public boolean checkAccess(String clientId, String topic, TopicPermission.MqttActivity activity, boolean isAuthenticated) {
        log.debug("Checking access for clientId:={}, topic:={}, activity:={}, isAuthenticated:={}", clientId, topic, activity, isAuthenticated);

        if (topic.startsWith(AuthorizationConsts.REQUEST_CLAIMS_TOPIC_PREFIX) && !TopicHelper.TopicSegmentIsEqualTo(topic, 2, clientId)) {
            log.debug("Acting on claim request topics is only allowed by the client itself - access denied");
            return false;
        }

        // Only allow to publish reserved topics if client is authenticated
        if (activity == TopicPermission.MqttActivity.PUBLISH && TopicHelper.IsReservedTopic(topic) && !isAuthenticated) {
            log.debug("Reserved topics can only be published if the client is authenticated - access denied");
            return false;
        }

        // Dont allow subscriptions of reserved topics
        if (activity == TopicPermission.MqttActivity.SUBSCRIBE && TopicHelper.IsReservedTopic(topic)) {
            log.debug("Reserved topics cannot be subscribed - access denied");
            return false;
        }

        // Topic is not in restricted area - access allowed for everyone
        if (!TopicHelper.IsTopicInRestrictedArea(topic)) {
            log.debug("Topic is outside of the restricted area - access allowed");
            return true;
        }

        // Topic seems to be in restricted area but client is not authenticated -> access denied
        if (!isAuthenticated) {
            log.debug("Client must be authenticated with SMOKER auth method to get access to restricted resources - access denied");
            return false;
        }

        Claim claim = claimStore.find(c -> c.getRestriction().getTopicName().equals(topic)).stream().findFirst().orElse(null);

        // topic not claimed -> access denied
        if (claim == null) {
            log.debug("Topic:=" + topic + " is not claimed by any client - access denied");
            return false;
        }

        // topic is owned by caller -> implicitly allowed
        if (claim.getRestriction().getOwner().equals(clientId)) {
            log.debug("Topic:=" + topic + " is owned by caller itself with clientId:={clientId} - access allowed");
            return true;
        }

        // Check if specific permissions are granted
        var involvedPermissions = claim.getRestriction().getPermissions().stream().filter(p -> p.getClientId().equals(clientId) || p.getClientId().equals(AuthorizationConsts.ANY_CLIENT_IDENTIFIER)).collect(Collectors.toList());
        var permissionMatch = involvedPermissions.stream().anyMatch(p -> p.getActivity() == activity || p.getActivity() == TopicPermission.MqttActivity.ALL);

        switch (claim.getRestriction().getRestrictionType()) {
            case WHITELIST:
                log.debug("Access to topic:=" + topic + " allowed:=" + permissionMatch + " for clientId:=" + clientId);
                return permissionMatch;
            case BLACKLIST:
                log.debug("Access to topic:=" + topic + " allowed:=" + !permissionMatch + " for clientId:=" + clientId);
                return !permissionMatch;
            default:
                return false;
        }
    }
}
