package ch.microfast.hivemq.smoker.di;

import ch.microfast.hivemq.smoker.SmokerExtension;
import ch.microfast.hivemq.smoker.authn.SmokerConnectInboundInterceptor;
import ch.microfast.hivemq.smoker.authn.providers.SmokerConnectInboundInterceptorProvider;
import ch.microfast.hivemq.smoker.authn.providers.SmokerEnhancedAuthenticationProvider;
import ch.microfast.hivemq.smoker.authn.SmokerEnhancedAuthenticator;
import ch.microfast.hivemq.smoker.authz.SmokerSubscribeInboundInterceptor;
import ch.microfast.hivemq.smoker.authz.persistance.ChronicleMapDiskClaimStore;
import ch.microfast.hivemq.smoker.authz.providers.SmokerAuthorizerProvider;
import ch.microfast.hivemq.smoker.authz.SmokerClientInitializer;
import ch.microfast.hivemq.smoker.authz.SmokerPublishInboundInterceptor;
import ch.microfast.hivemq.smoker.authz.SmokerPublishSubscribeAuthorizer;
import ch.microfast.hivemq.smoker.authz.serialization.SmokerJsonSerializer;
import ch.microfast.hivemq.smoker.authz.validation.ClaimValidator;
import ch.microfast.hivemq.smoker.authz.validation.IClaimValidator;
import ch.microfast.hivemq.smoker.crypto.EddsaCryptoProvider;
import ch.microfast.hivemq.smoker.crypto.ICryptoProvider;
import ch.microfast.hivemq.smoker.authz.persistance.IClaimStore;
import ch.microfast.hivemq.smoker.authz.services.AuthzService;
import ch.microfast.hivemq.smoker.authz.services.IAuthzService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.auth.Authorizer;
import com.hivemq.extension.sdk.api.auth.EnhancedAuthenticator;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptorProvider;
import com.hivemq.extension.sdk.api.interceptor.publish.PublishInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.subscribe.SubscribeInboundInterceptor;
import com.hivemq.extension.sdk.api.services.auth.provider.AuthorizerProvider;
import com.hivemq.extension.sdk.api.services.auth.provider.EnhancedAuthenticatorProvider;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * DI configuration for the SMOKER extension
 */
public class SmokerModule extends AbstractModule {
    private static final @NotNull Logger log = LoggerFactory.getLogger(SmokerExtension.class);

    @Override
    protected void configure() {
        bind(SmokerJsonSerializer.class).in(Scopes.SINGLETON);
        bind(IClaimStore.class).to(ChronicleMapDiskClaimStore.class).in(Scopes.SINGLETON);
        bind(IClaimValidator.class).to(ClaimValidator.class);
        bind(ICryptoProvider.class).to(EddsaCryptoProvider.class);
        bind(ClientInitializer.class).to(SmokerClientInitializer.class);
        bind(ConnectInboundInterceptor.class).to(SmokerConnectInboundInterceptor.class);
        bind(ConnectInboundInterceptorProvider.class).to(SmokerConnectInboundInterceptorProvider.class);
        bind(Authorizer.class).to(SmokerPublishSubscribeAuthorizer.class);
        bind(AuthorizerProvider.class).to(SmokerAuthorizerProvider.class);
        bind(EnhancedAuthenticator.class).to(SmokerEnhancedAuthenticator.class);
        bind(EnhancedAuthenticatorProvider.class).to(SmokerEnhancedAuthenticationProvider.class);
        bind(PublishInboundInterceptor.class).to(SmokerPublishInboundInterceptor.class);
        bind(SubscribeInboundInterceptor.class).to(SmokerSubscribeInboundInterceptor.class);
        bind(IAuthzService.class).to(AuthzService.class);

        bindConfig();
    }

    /**
     * Load config file and bind it to the module
     */
    private void bindConfig() {
        try {
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/config/smive.properties"));
            Names.bindProperties(binder(), props);
        } catch (IOException e) {
            log.error("Could not load config: ", e);
            System.exit(1);
        }
    }
}
