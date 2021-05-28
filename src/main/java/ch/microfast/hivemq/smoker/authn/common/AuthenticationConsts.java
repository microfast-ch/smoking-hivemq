package ch.microfast.hivemq.smoker.authn.common;

public class AuthenticationConsts {

    /**
     * Key which is used to hold auth state in {@link com.hivemq.extension.sdk.api.client.parameter.ConnectionAttributeStore} for each connection
     */
    public static final String IS_SMOKER_AUTH_ATTRIBUTE_KEY = "IS_SMOKER_AUTH";

    /**
     * Value which must be provided by clients as authentication method to trigger SMOKER auth which is implemented in {@link ch.microfast.hivemq.smoker.authn.SmokerEnhancedAuthenticator}
     */
    public static final String SMOKER_AUTH_METHOD = "SMOKER";
}
