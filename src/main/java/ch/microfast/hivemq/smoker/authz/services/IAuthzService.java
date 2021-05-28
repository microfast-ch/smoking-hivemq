package ch.microfast.hivemq.smoker.authz.services;

import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;

import java.util.Collection;

/**
 * Authorization services to provide claim functionality as well as checking access for client activity
 */
public interface IAuthzService {
    void claim(Claim claim) throws InvalidClaimException;

    void unclaim(String owner, String topicName);

    Collection<Claim> getClaimsForClient(String clientId);

    boolean checkAccess(String clientId, String topic, TopicPermission.MqttActivity activity, boolean isAuthenticated);
}
