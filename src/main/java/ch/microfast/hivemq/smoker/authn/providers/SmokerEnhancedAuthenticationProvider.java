package ch.microfast.hivemq.smoker.authn.providers;

import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.auth.EnhancedAuthenticator;
import com.hivemq.extension.sdk.api.auth.parameter.AuthenticatorProviderInput;
import com.hivemq.extension.sdk.api.services.auth.provider.EnhancedAuthenticatorProvider;

/**
 * Enhanced authentication provider class for the hivemq extensions ecosystem.
 */
public class SmokerEnhancedAuthenticationProvider implements EnhancedAuthenticatorProvider {

    private final EnhancedAuthenticator enhancedAuthenticator;

    @Inject
    public SmokerEnhancedAuthenticationProvider(EnhancedAuthenticator enhancedAuthenticator) {
        this.enhancedAuthenticator = enhancedAuthenticator;
    }

    @Override
    public @Nullable EnhancedAuthenticator getEnhancedAuthenticator(@NotNull AuthenticatorProviderInput authenticatorProviderInput) {
        return enhancedAuthenticator;
    }
}
