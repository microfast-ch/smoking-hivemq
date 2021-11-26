package ch.microfast.hivemq.smoker.authz;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;
import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import com.hivemq.extension.sdk.api.client.ClientContext;
import com.hivemq.extension.sdk.api.client.parameter.InitializerInput;
import com.hivemq.extension.sdk.api.packets.auth.DefaultAuthorizationBehaviour;
import com.hivemq.extension.sdk.api.packets.auth.ModifiableDefaultPermissions;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;

/**
 * A {@link ClientInitializer} used to register the interceptors used in this extension
 */
public class SmokerClientInitializer implements ClientInitializer {

    private final SmokerPublishInboundInterceptor publishInboundInterceptor;

    @Inject
    public SmokerClientInitializer(SmokerPublishInboundInterceptor publishInboundInterceptor) {
        this.publishInboundInterceptor = publishInboundInterceptor;
    }

    @Override
    public void initialize(@NotNull InitializerInput initializerInput, @NotNull ClientContext clientContext) {
        clientContext.addPublishInboundInterceptor(publishInboundInterceptor);

        // Set the permissions according to the claims by clients
        // NOTE: The Priority of the permissions is set by the chronological order of the permissions.
        final ModifiableDefaultPermissions defaultPermissions = clientContext.getDefaultPermissions();

        // Default Permissions
        var denyClaimTopicSubscriptions = Builders.topicPermission()
                .activity(TopicPermission.MqttActivity.SUBSCRIBE)
                .topicFilter(AuthorizationConsts.CLAIM_TOPIC)
                .type(TopicPermission.PermissionType.DENY)
                .build();

        var denyUnclaimTopicSubscriptions = Builders.topicPermission()
                .activity(TopicPermission.MqttActivity.SUBSCRIBE)
                .topicFilter(AuthorizationConsts.UNCLAIM_TOPIC)
                .type(TopicPermission.PermissionType.DENY)
                .build();

        var denyRestrictedAreaActivity = Builders.topicPermission()
                .activity(TopicPermission.MqttActivity.ALL)
                .topicFilter(AuthorizationConsts.RESTRICTED_AREA_PREFIX + "/#")
                .type(TopicPermission.PermissionType.DENY)
                .build();

        defaultPermissions.add(denyClaimTopicSubscriptions);
        defaultPermissions.add(denyUnclaimTopicSubscriptions);
        defaultPermissions.add(denyRestrictedAreaActivity);

        //change the default behaviour to ALLOW, so all other topics are allowed
        defaultPermissions.setDefaultBehaviour(DefaultAuthorizationBehaviour.ALLOW);
    }
}
