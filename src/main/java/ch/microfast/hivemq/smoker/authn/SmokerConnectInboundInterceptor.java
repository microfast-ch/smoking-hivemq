package ch.microfast.hivemq.smoker.authn;

import ch.microfast.hivemq.smoker.authn.common.AuthenticationConsts;
import ch.microfast.hivemq.smoker.authz.services.AuthzService;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.connect.parameter.ConnectInboundInput;
import com.hivemq.extension.sdk.api.interceptor.connect.parameter.ConnectInboundOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * Connection interceptor to prevent collisions of clientId's of authenticated and unauthenticated clients
 */
public class SmokerConnectInboundInterceptor implements ConnectInboundInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SmokerConnectInboundInterceptor.class);


    @Override
    public void onConnect(@NotNull ConnectInboundInput connectInboundInput, @NotNull ConnectInboundOutput connectInboundOutput) {
        @NotNull Optional<String> authenticationMethod = connectInboundInput.getConnectPacket().getAuthenticationMethod();

        log.info("MQTT client connecting with id:={} and authenticationMethod:={}", connectInboundInput.getClientInformation().getClientId(), authenticationMethod);

        // If the client does not want to connect with SMOKER - the broker must assign a random client ID to prevent collisions with authenticated clients
        if (authenticationMethod.isEmpty() || !AuthenticationConsts.SMOKER_AUTH_METHOD.equalsIgnoreCase(authenticationMethod.get())) {
            connectInboundOutput.getConnectPacket().setClientId(UUID.randomUUID().toString());
        }
    }
}
