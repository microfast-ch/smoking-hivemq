package ch.microfast.hivemq.smoker.authz;

import ch.microfast.hivemq.smoker.authz.common.AuthorizationConsts;
import ch.microfast.hivemq.smoker.authz.common.SmokerClientHelper;
import ch.microfast.hivemq.smoker.authz.domain.Claim;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.authz.services.IAuthzService;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.InvalidClaimException;
import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.parameter.TopicPermission;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundInput;
import com.hivemq.extension.sdk.api.interceptor.publish.parameter.PublishInboundOutput;
import com.hivemq.extension.sdk.api.packets.publish.AckReasonCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The inbound publish interceptor listens for claims/unclaims of authenticated clients and stores them into the persistent storage.
 * NOTE: This interceptor is called before any other authorizers such as {@link SmokerPublishSubscribeAuthorizer} which handles every publish message.
 * The {@link SmokerPublishSubscribeAuthorizer} is not called if this interceptor already prevents the publish.
 */
public class SmokerPublishInboundInterceptor implements PublishInboundInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SmokerPublishInboundInterceptor.class);

    private final IAuthzService authzService;

    private final SmokerJsonSerializer smokerJsonSerializer;

    private final IClaimValidator claimValidator;

    @Inject
    public SmokerPublishInboundInterceptor(IAuthzService authzService, SmokerJsonSerializer smokerJsonSerializer, IClaimValidator claimValidator) {
        this.authzService = authzService;
        this.smokerJsonSerializer = smokerJsonSerializer;
        this.claimValidator = claimValidator;
    }

    @Override
    public void onInboundPublish(@NotNull PublishInboundInput publishInboundInput, @NotNull PublishInboundOutput publishInboundOutput) {
        try {
            String clientId = publishInboundInput.getClientInformation().getClientId();
            String publishTopic = publishInboundInput.getPublishPacket().getTopic();
            boolean isAuthenticated = SmokerClientHelper.isAuthenticated(publishInboundInput.getConnectionInformation().getConnectionAttributeStore());

            // prevent publish if client is not allowed to
            if (AuthorizationConsts.CLAIM_TOPIC.equals(publishTopic) || AuthorizationConsts.UNCLAIM_TOPIC.equals(publishTopic)) {
                boolean isAllowed = authzService.checkAccess(clientId, publishTopic, TopicPermission.MqttActivity.PUBLISH, isAuthenticated);
                if (!isAllowed) {
                    log.debug("Prevent claim/unclaim because the client is not allowed to");
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.NOT_AUTHORIZED);
                    return;
                }
            }

            // handle claims
            if (AuthorizationConsts.CLAIM_TOPIC.equals(publishTopic)) {
                log.debug("Incoming claim from clientId:={}", clientId);
                @NotNull Optional<ByteBuffer> payload = publishInboundInput.getPublishPacket().getPayload();

                // validate if payload is not empty
                if (payload.isEmpty()) {
                    log.debug("Prevent claim because payload was empty");
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.PAYLOAD_FORMAT_INVALID, "The claim payload is empty");
                    return;
                }

                // deserialize the payload and process the claim
                String claimJson = StandardCharsets.UTF_8.decode(payload.get()).toString();
                try {
                    Claim claim = smokerJsonSerializer.readValue(claimJson, Claim.class);
                    claimValidator.validateClaim(claim);
                    authzService.claim(claim);
                } catch (IOException ex) {
                    log.debug("Prevent claim because deserialization failed", ex);
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.PAYLOAD_FORMAT_INVALID, "The claim payload is not in the expected format and cannot be deserialized");
                    return;
                } catch (InvalidClaimException ex) {
                    log.debug("Prevent claim because validation failed", ex);
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.PAYLOAD_FORMAT_INVALID, String.format("Claim is not valid. ErrorMessages:={}", ex.getErrorMessages()));
                } catch (Exception ex) {
                    log.error("Prevent claim because of a unexpected exception", ex);
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.UNSPECIFIED_ERROR, String.format("Unexpected exception while claiming"));
                }
            }

            // handle unclaims
            else if (AuthorizationConsts.UNCLAIM_TOPIC.equals(publishTopic)) {
                log.debug("Incoming unclaim from clientId:={}", clientId);
                @NotNull Optional<ByteBuffer> payload = publishInboundInput.getPublishPacket().getPayload();

                // validate if payload is not empty
                if (payload.isEmpty()) {
                    log.debug("Prevent unclaim because payload was empty");
                    publishInboundOutput.preventPublishDelivery(AckReasonCode.PAYLOAD_FORMAT_INVALID, "The unclaim payload is empty");
                    return;
                }

                String unclaimTopicName = StandardCharsets.UTF_8.decode(payload.get()).toString();
                authzService.unclaim(clientId, unclaimTopicName);
            }
        } catch(Exception ex) {
            log.error("Unexpected exception", ex);
            throw ex;
        }
    }
}
