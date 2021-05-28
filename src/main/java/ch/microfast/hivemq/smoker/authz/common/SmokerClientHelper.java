package ch.microfast.hivemq.smoker.authz.common;

import ch.microfast.hivemq.smoker.authn.common.AuthenticationConsts;
import com.hivemq.extension.sdk.api.client.parameter.ConnectionAttributeStore;

public final class SmokerClientHelper {
    /**
     * Checks if the given {@link ConnectionAttributeStore} contains the SMOKER authn flag
     * @param connectionAttributeStore the attribute store of a client connection
     * @return if the client is SMOKER authenticated or not
     */
    public static boolean isAuthenticated(ConnectionAttributeStore connectionAttributeStore) {
        if (connectionAttributeStore == null) {
            throw new IllegalArgumentException("Param 'connectionAttributeStore' must not be null");
        }

        String smokerConnAttr = connectionAttributeStore.getAsString(AuthenticationConsts.IS_SMOKER_AUTH_ATTRIBUTE_KEY).orElse(null);
        return smokerConnAttr != null && Boolean.TRUE.toString().equalsIgnoreCase(smokerConnAttr);
    }
}
