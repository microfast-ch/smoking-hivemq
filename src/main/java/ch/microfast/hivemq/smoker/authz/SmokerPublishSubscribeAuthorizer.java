package ch.microfast.hivemq.smoker.authz;

import ch.microfast.hivemq.smoker.authz.common.SmokerClientHelper;
import ch.microfast.hivemq.smoker.authz.services.IAuthzService;
import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.PublishAuthorizer;
import com.hivemq.extension.sdk.api.auth.SubscriptionAuthorizer;
import com.hivemq.extension.sdk.api.auth.parameter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publish and subscribe authorizer for all mqtt activity.
 * NOTE: These authorization functions are checked before default topic permissions defined in {@link SmokerClientInitializer}.
 */
public class SmokerPublishSubscribeAuthorizer implements PublishAuthorizer, SubscriptionAuthorizer {

    private static final Logger log = LoggerFactory.getLogger(SmokerPublishSubscribeAuthorizer.class);

    private final IAuthzService authzService;

    @Inject
    public SmokerPublishSubscribeAuthorizer(IAuthzService authzService) {
        this.authzService = authzService;
    }

    @Override
    public void authorizePublish(@NotNull PublishAuthorizerInput publishAuthorizerInput, @NotNull PublishAuthorizerOutput publishAuthorizerOutput) {
        try {
            boolean isAuthenticated = SmokerClientHelper.isAuthenticated(publishAuthorizerInput.getConnectionInformation().getConnectionAttributeStore());
            String topic = publishAuthorizerInput.getPublishPacket().getTopic();
            String clientId = publishAuthorizerInput.getClientInformation().getClientId();

            boolean isAuthorized = authzService.checkAccess(clientId, topic, TopicPermission.MqttActivity.PUBLISH, isAuthenticated);
            if (isAuthorized) {
                publishAuthorizerOutput.authorizeSuccessfully();
                return;
            }

            publishAuthorizerOutput.nextExtensionOrDefault();
        } catch(Exception ex) {
            log.error("Unexpected exception", ex);
            throw ex;
        }
    }

    @Override
    public void authorizeSubscribe(@NotNull SubscriptionAuthorizerInput subscriptionAuthorizerInput, @NotNull SubscriptionAuthorizerOutput subscriptionAuthorizerOutput) {
        try {
            boolean isAuthenticated = SmokerClientHelper.isAuthenticated(subscriptionAuthorizerInput.getConnectionInformation().getConnectionAttributeStore());
            String topic = subscriptionAuthorizerInput.getSubscription().getTopicFilter();
            String clientId = subscriptionAuthorizerInput.getClientInformation().getClientId();

            boolean isAuthorized = authzService.checkAccess(clientId, topic, TopicPermission.MqttActivity.SUBSCRIBE, isAuthenticated);
            if (isAuthorized) {
                subscriptionAuthorizerOutput.authorizeSuccessfully();
                return;
            }

            subscriptionAuthorizerOutput.nextExtensionOrDefault();
        } catch(Exception ex) {
            log.error("Unexpected exception", ex);
            throw ex;
        }
    }
}
