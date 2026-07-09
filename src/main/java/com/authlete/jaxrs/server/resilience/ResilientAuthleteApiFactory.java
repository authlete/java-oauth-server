/*
 * Copyright (C) 2026 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.resilience;


import java.lang.reflect.Proxy;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;


/**
 * Drop-in replacement for {@link AuthleteApiFactory#getDefaultApi()} that
 * returns an {@link AuthleteApi} wrapped with the resilience layer (caching,
 * conditional retry, exponential backoff with jitter, and a circuit breaker).
 *
 * <p>
 * Endpoint classes obtain their API client through this factory instead of
 * {@code AuthleteApiFactory} so that every call to Authlete &mdash; including
 * nested calls made by handler SPIs that receive the same instance &mdash; is
 * protected, with no change to the endpoint logic itself.
 * </p>
 *
 * <p>
 * The wrapped instance is built once and shared. Tuning lives in
 * {@code resilience.properties} (see {@link ResilienceConfig}); when resilience
 * is disabled there, the underlying default {@code AuthleteApi} is returned
 * unwrapped, restoring the original behaviour.
 * </p>
 */
public final class ResilientAuthleteApiFactory
{
    private static volatile AuthleteApi cachedApi;


    private ResilientAuthleteApiFactory()
    {
    }


    /**
     * Get the resilient default {@link AuthleteApi} instance.
     *
     * @return
     *         The default {@code AuthleteApi} wrapped with the resilience layer,
     *         or the unwrapped default instance when resilience is disabled.
     */
    public static AuthleteApi getDefaultApi()
    {
        AuthleteApi api = cachedApi;

        if (api != null)
        {
            return api;
        }

        return initDefaultApi();
    }


    private static synchronized AuthleteApi initDefaultApi()
    {
        if (cachedApi != null)
        {
            return cachedApi;
        }

        AuthleteApi delegate = AuthleteApiFactory.getDefaultApi();

        cachedApi = wrap(delegate);

        return cachedApi;
    }


    /**
     * Wrap the given {@link AuthleteApi} with the resilience layer. Returns the
     * delegate unchanged when resilience is disabled in the configuration.
     */
    public static AuthleteApi wrap(AuthleteApi delegate)
    {
        ResilienceConfig config = new ResilienceConfig();

        if (!config.isEnabled())
        {
            return delegate;
        }

        return (AuthleteApi) Proxy.newProxyInstance(
                AuthleteApi.class.getClassLoader(),
                new Class<?>[] { AuthleteApi.class },
                new ResilientAuthleteApiInvocationHandler(delegate, config));
    }
}
