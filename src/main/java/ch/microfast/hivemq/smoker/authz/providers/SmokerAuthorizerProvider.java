package ch.microfast.hivemq.smoker.authz.providers;

import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.auth.Authorizer;
import com.hivemq.extension.sdk.api.auth.parameter.AuthorizerProviderInput;
import com.hivemq.extension.sdk.api.services.auth.provider.AuthorizerProvider;

public class SmokerAuthorizerProvider implements AuthorizerProvider {
    private final Authorizer authorizer;

    @Inject
    public SmokerAuthorizerProvider(Authorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    public @Nullable Authorizer getAuthorizer(@NotNull AuthorizerProviderInput authorizerProviderInput) {
        return this.authorizer;
    }
}
