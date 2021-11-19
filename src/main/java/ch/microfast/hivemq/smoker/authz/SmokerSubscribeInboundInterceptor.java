package ch.microfast.hivemq.smoker.authz;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;
import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.authz.services.IAuthzService;
import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.subscribe.SubscribeInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.subscribe.parameter.SubscribeInboundInput;
import com.hivemq.extension.sdk.api.interceptor.subscribe.parameter.SubscribeInboundOutput;
import com.hivemq.extension.sdk.api.packets.general.Qos;
import com.hivemq.extension.sdk.api.packets.subscribe.Subscription;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.builder.Builders;
import com.hivemq.extension.sdk.api.services.publish.Publish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class SmokerSubscribeInboundInterceptor implements SubscribeInboundInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SmokerSubscribeInboundInterceptor.class);

    private final IAuthzService authzService;
    private SmokerJsonSerializer smokerJsonSerializer;

    @Inject
    public SmokerSubscribeInboundInterceptor(IAuthzService authzService, SmokerJsonSerializer smokerJsonSerializer) {
        this.authzService = authzService;
        this.smokerJsonSerializer = smokerJsonSerializer;
    }


    @Override
    public void onInboundSubscribe(@NotNull SubscribeInboundInput subscribeInboundInput, @NotNull SubscribeInboundOutput subscribeInboundOutput) {
        String clientId = subscribeInboundInput.getClientInformation().getClientId();
        String getClaimsTopic = AuthorizationConsts.GET_CLAIMS_TOPIC_PREFIX + "/" + clientId;

        for (Subscription sub : subscribeInboundInput.getSubscribePacket().getSubscriptions()) {
            String topic = sub.getTopicFilter();
            if (getClaimsTopic.equals(topic)) {
                try {
                    Collection<Claim> claimsForClient = authzService.getClaimsForClient(clientId);
                    String clientClaimsJson = this.smokerJsonSerializer.writeValueAsString(claimsForClient);
                    Publish message = Builders.publish()
                            .topic(getClaimsTopic)
                            .retain(true)
                            .qos(Qos.AT_LEAST_ONCE)
                            .payload(ByteBuffer.wrap(clientClaimsJson.getBytes(StandardCharsets.UTF_8)))
                            .build();
                    Services.publishService().publish(message);

                } catch(Exception ex) {
                    log.error("Problem while subscribing to technical topic", ex);
                }
            }
        }
    }
}
