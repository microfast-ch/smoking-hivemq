
/*
 * Copyright 2018 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.microfast.hivemq.smoker;

import ch.microfast.hivemq.smoker.di.SmokerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.interceptor.connect.ConnectInboundInterceptorProvider;
import com.hivemq.extension.sdk.api.parameter.*;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extension.sdk.api.services.auth.provider.AuthorizerProvider;
import com.hivemq.extension.sdk.api.services.auth.provider.EnhancedAuthenticatorProvider;
import com.hivemq.extension.sdk.api.services.intializer.ClientInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the MQTT SMOKER authentication and authorization extension.
 * Check out <a href="https://arxiv.org/pdf/1904.00389.pdf">this paper</a> for further technical details.
 */
public class SmokerExtension implements ExtensionMain {

    private static final @NotNull Logger log = LoggerFactory.getLogger(SmokerExtension.class);

    @Override
    public void extensionStart(final @NotNull ExtensionStartInput extensionStartInput, final @NotNull ExtensionStartOutput extensionStartOutput) {

        try {
            // Initialize depedency injection
            Injector injector = Guice.createInjector(new SmokerModule());

            // Register services
            Services.initializerRegistry().setClientInitializer(injector.getInstance(ClientInitializer.class));
            Services.securityRegistry().setEnhancedAuthenticatorProvider(injector.getInstance(EnhancedAuthenticatorProvider.class));
            Services.securityRegistry().setAuthorizerProvider(injector.getInstance(AuthorizerProvider.class));
            Services.interceptorRegistry().setConnectInboundInterceptorProvider(injector.getInstance(ConnectInboundInterceptorProvider.class));

            final ExtensionInformation extensionInformation = extensionStartInput.getExtensionInformation();
            log.info("Started " + extensionInformation.getName() + ":" + extensionInformation.getVersion());

        } catch (Exception e) {
            log.error("Exception thrown at extension start: ", e);
        }
    }

    @Override
    public void extensionStop(final @NotNull ExtensionStopInput extensionStopInput, final @NotNull ExtensionStopOutput extensionStopOutput) {
        final ExtensionInformation extensionInformation = extensionStopInput.getExtensionInformation();
        log.info("Stopped " + extensionInformation.getName() + ":" + extensionInformation.getVersion());
    }
}
