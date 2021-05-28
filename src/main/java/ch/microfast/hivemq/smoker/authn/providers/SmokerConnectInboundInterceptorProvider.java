package ch.microfast.hivemq.smoker.authn.providers;

import com.google.inject.Inject;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptor;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptorProvider;
import com.hivemq.extension.sdk.api.interceptor.connect.parameter.ConnectInboundProviderInput;

public class SmokerConnectInboundInterceptorProvider implements ConnectInboundInterceptorProvider {

    private final ConnectInboundInterceptor connectInboundInterceptor;

    @Inject
    public SmokerConnectInboundInterceptorProvider(ConnectInboundInterceptor connectInboundInterceptor) {
        this.connectInboundInterceptor = connectInboundInterceptor;
    }

    @Override
    public @Nullable ConnectInboundInterceptor getConnectInboundInterceptor(@NotNull ConnectInboundProviderInput connectInboundProviderInput) {
        return connectInboundInterceptor;
    }
}
