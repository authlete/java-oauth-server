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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiException;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.dto.RevocationRequest;


/**
 * End-to-end tests that exercise the whole resilience layer through the real
 * dynamic proxy produced by {@link ResilientAuthleteApiFactory#wrap}, driving a
 * programmable fake {@link AuthleteApi} backend so caching, retry, permanent
 * error handling, circuit breaking and stale fallback can all be asserted
 * without a network or a real Authlete server.
 */
public class ResilientAuthleteApiE2ETest
{
    /** Every resilience knob this test touches, reset between cases. */
    private static final String[] KEYS = {
        "resilience.enabled",
        "resilience.retry.enabled",
        "resilience.retry.maxAttempts",
        "resilience.retry.baseDelayMillis",
        "resilience.retry.jitterMillis",
        "resilience.retry.maxTotalMillis",
        "resilience.breaker.enabled",
        "resilience.breaker.failureThreshold",
        "resilience.breaker.windowSeconds",
        "resilience.breaker.openSeconds",
        "resilience.cache.enabled",
        "resilience.cache.ttl.introspection",
        "resilience.cache.staleSeconds",
    };


    /**
     * Programmable {@link AuthleteApi} backend. Only {@code introspection} is
     * meaningful; every other method returns {@code null}. The next queued
     * status (or {@link #always}, when set) decides whether a call throws an
     * {@link AuthleteApiException} or returns {@link #response}.
     */
    private static final class Backend implements InvocationHandler
    {
        final AtomicInteger calls = new AtomicInteger();
        final ConcurrentLinkedQueue<Integer> statuses = new ConcurrentLinkedQueue<>();
        volatile Integer always = null;
        final IntrospectionResponse response;

        Backend(IntrospectionResponse response)
        {
            this.response = response;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
        {
            if (method.getDeclaringClass() == Object.class)
            {
                switch (method.getName())
                {
                    case "equals":   return proxy == args[0];
                    case "hashCode": return System.identityHashCode(proxy);
                    default:         return "Backend";
                }
            }

            if (!"introspection".equals(method.getName()))
            {
                return null;
            }

            calls.incrementAndGet();

            Integer status = (always != null) ? always : statuses.poll();

            if (status != null && status.intValue() != 0)
            {
                throw new AuthleteApiException(
                        "simulated " + status, status.intValue(), "error", null);
            }

            return response;
        }
    }


    private final IntrospectionResponse canned = new IntrospectionResponse();
    private Backend     backend;
    private AuthleteApi api;


    @Before
    public void setUp()
    {
        // A baseline that is friendly to the cache/retry cases; individual
        // tests override the few knobs they care about before building the API.
        set("resilience.enabled", "true");
        set("resilience.retry.enabled", "true");
        set("resilience.retry.maxAttempts", "4");
        set("resilience.retry.baseDelayMillis", "5");
        set("resilience.retry.jitterMillis", "0");
        set("resilience.retry.maxTotalMillis", "60000");
        set("resilience.breaker.enabled", "true");
        set("resilience.breaker.failureThreshold", "100");
        set("resilience.breaker.windowSeconds", "30");
        set("resilience.breaker.openSeconds", "60");
        set("resilience.cache.enabled", "true");
        set("resilience.cache.ttl.introspection", "30");
        set("resilience.cache.staleSeconds", "1800");

        backend = new Backend(canned);
    }


    @After
    public void tearDown()
    {
        for (String key : KEYS)
        {
            System.clearProperty(key);
        }
    }


    /** Build the resilient proxy from the current system-property snapshot. */
    private AuthleteApi buildApi()
    {
        AuthleteApi delegate = (AuthleteApi) Proxy.newProxyInstance(
                AuthleteApi.class.getClassLoader(),
                new Class<?>[] { AuthleteApi.class },
                backend);

        return ResilientAuthleteApiFactory.wrap(delegate);
    }


    private static IntrospectionRequest request()
    {
        return new IntrospectionRequest().setToken("token-123");
    }


    @Test
    public void freshHitIsServedFromCacheWithoutCallingBackend() throws Exception
    {
        api = buildApi();

        IntrospectionResponse first  = api.introspection(request());
        IntrospectionResponse second = api.introspection(request());

        assertSame("same cached instance returned", first, second);
        assertSame(canned, first);
        assertEquals("backend invoked only once", 1, backend.calls.get());
    }


    @Test
    public void transientFailuresAreRetriedThenSucceed() throws Exception
    {
        backend.statuses.add(503);
        backend.statuses.add(503);

        api = buildApi();

        IntrospectionResponse result = api.introspection(request());

        assertSame("succeeds after the backend recovers", canned, result);
        assertEquals("two failures + one success", 3, backend.calls.get());
    }


    @Test
    public void permanentErrorIsNotRetried() throws Exception
    {
        backend.statuses.add(400);

        api = buildApi();

        try
        {
            api.introspection(request());
            fail("expected the 400 to propagate");
        }
        catch (AuthleteApiException e)
        {
            assertEquals(400, e.getStatusCode());
        }

        assertEquals("no retry on a permanent error", 1, backend.calls.get());
    }


    @Test
    public void breakerOpensAndFailsFastWithoutCallingBackend() throws Exception
    {
        set("resilience.retry.enabled", "false");
        set("resilience.breaker.failureThreshold", "3");
        backend.always = 503;

        api = buildApi();

        for (int i = 0; i < 6; i++)
        {
            try
            {
                api.introspection(request());
                fail("every call should fail while the backend is down");
            }
            catch (AuthleteApiException expected)
            {
                // expected
            }
        }

        // Only the first three calls reach the backend; after the threshold the
        // breaker is open and subsequent calls fail fast.
        assertEquals("backend shielded once the breaker opens", 3, backend.calls.get());
    }


    @Test
    public void revocationEvictsCachedIntrospectionForTheToken() throws Exception
    {
        api = buildApi();

        // Prime the cache; the second call is served from it.
        assertSame(canned, api.introspection(request()));
        api.introspection(request());
        assertEquals("backend hit once while cached", 1, backend.calls.get());

        // Revoke the same token through the proxy (URL-encoded form parameters).
        api.revocation(new RevocationRequest()
                .setParameters("token=token-123&token_type_hint=access_token"));

        // The cached entry must be gone: the next introspection reaches the
        // backend again instead of reporting the revoked token as active.
        api.introspection(request());
        assertEquals("cache evicted on revocation", 2, backend.calls.get());
    }


    @Test
    public void revocationOfAnotherTokenKeepsUnrelatedCacheEntries() throws Exception
    {
        api = buildApi();

        assertSame(canned, api.introspection(request()));
        assertEquals(1, backend.calls.get());

        // Revoking a different token must not evict this token's entry.
        api.revocation(new RevocationRequest()
                .setParameters("token=other-token&token_type_hint=access_token"));

        api.introspection(request());
        assertEquals("unrelated entry still served from cache", 1, backend.calls.get());
    }


    @Test
    public void staleEntryIsServedAsFallbackOnFailure() throws Exception
    {
        set("resilience.retry.enabled", "false");
        set("resilience.cache.ttl.introspection", "1"); // 1s fresh, then stale

        api = buildApi();

        // Prime the cache with a successful response.
        assertSame(canned, api.introspection(request()));
        assertEquals(1, backend.calls.get());

        // Let the fresh TTL lapse so the entry is only "stale".
        Thread.sleep(1200);

        // Backend now fails; the layer should serve the stale value instead.
        backend.always = 503;
        IntrospectionResponse result = api.introspection(request());

        assertSame("stale cached value served as fallback", canned, result);
        assertEquals("backend was attempted once more before falling back",
                2, backend.calls.get());
    }


    private static void set(String key, String value)
    {
        System.setProperty(key, value);
    }
}
