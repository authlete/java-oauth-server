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


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiException;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.resilience.AuthleteCacheableMethods.CachePolicy;


/**
 * The {@link InvocationHandler} behind the resilient {@link AuthleteApi} proxy.
 *
 * <p>
 * Every call to the wrapped {@code AuthleteApi} flows through {@link #invoke}
 * which applies, in order, the four practices from Authlete's "Rate Limit Best
 * Practices" guide:
 * </p>
 * <ol>
 *   <li><b>Caching</b> &mdash; a fresh cached response for an idempotent read is
 *       returned without calling Authlete.</li>
 *   <li><b>Circuit breaking</b> &mdash; when the per-method breaker is open, the
 *       call fails fast, serving stale cached data when available.</li>
 *   <li><b>Conditional retry</b> &mdash; only transient failures (429/5xx/no
 *       response) are retried; permanent 4xx errors propagate immediately.</li>
 *   <li><b>Exponential backoff with jitter</b> &mdash; the wait before each
 *       retry grows exponentially (honouring {@code RateLimit-Reset} on 429),
 *       bounded by a total retry budget.</li>
 * </ol>
 */
class ResilientAuthleteApiInvocationHandler implements InvocationHandler
{
    private static final Logger logger =
            LoggerFactory.getLogger(ResilientAuthleteApiInvocationHandler.class);

    private final AuthleteApi            delegate;
    private final AuthleteCacheableMethods       cacheable;
    private final AuthleteResponseCache          cache;
    private final AuthleteRetryPolicy            retry;
    private final AuthleteBackoff                backoff;
    private final AuthleteCircuitBreakerRegistry breakers;

    private final boolean cacheEnabled;
    private final boolean retryEnabled;
    private final boolean breakerEnabled;
    private final int     maxAttempts;
    private final long    maxTotalMillis;


    ResilientAuthleteApiInvocationHandler(AuthleteApi delegate, ResilienceConfig config)
    {
        this.delegate  = delegate;
        this.cacheable = new AuthleteCacheableMethods(config);
        this.cache     = new AuthleteResponseCache(config.getCacheStaleMillis(), config.getCacheMaxEntries());
        this.retry     = new AuthleteRetryPolicy();
        this.backoff   = new AuthleteBackoff(
                config.getRetryBaseDelayMillis(),
                config.getRetryMaxTotalMillis(),
                config.getRetryJitterMillis());
        this.breakers  = new AuthleteCircuitBreakerRegistry(config);

        this.cacheEnabled   = config.isCacheEnabled();
        this.retryEnabled   = config.isRetryEnabled();
        this.breakerEnabled = config.isBreakerEnabled();
        this.maxAttempts    = Math.max(1, config.getRetryMaxAttempts());
        this.maxTotalMillis = config.getRetryMaxTotalMillis();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        // Methods inherited from Object (equals/hashCode/toString) are handled
        // locally and never forwarded to Authlete.
        if (method.getDeclaringClass() == Object.class)
        {
            return invokeObjectMethod(proxy, method, args);
        }

        CachePolicy policy = cacheEnabled ? cacheable.policyFor(method, args) : null;

        // (1) Serve a fresh cached response without touching the network.
        if (policy != null)
        {
            Object fresh = cache.getFresh(policy.key);

            if (fresh != null)
            {
                return fresh;
            }
        }

        AuthleteCircuitBreaker breaker = breakerEnabled ? breakers.forMethod(method.getName()) : null;

        long start   = System.currentTimeMillis();
        int  attempt = 0;
        AuthleteApiException lastError = null;

        while (true)
        {
            attempt++;

            // (2) Circuit breaker gate: fail fast when open.
            if (breaker != null && !breaker.allowRequest())
            {
                Object stale = serveStale(policy, method, "circuit open");

                if (stale != null)
                {
                    return stale;
                }

                throw (lastError != null) ? lastError : circuitOpenException(method);
            }

            try
            {
                Object result = method.invoke(delegate, args);

                if (breaker != null)
                {
                    breaker.recordSuccess();
                }

                if (policy != null)
                {
                    cache.put(policy.key, result, effectiveTtl(policy, result));
                }

                return result;
            }
            catch (InvocationTargetException ite)
            {
                Throwable cause = ite.getCause();

                // Only AuthleteApiException participates in retry/breaker logic;
                // anything else is an unexpected error and is propagated as-is,
                // after releasing any half-open trial slot reserved by
                // allowRequest() (the error says nothing about backend health).
                if (!(cause instanceof AuthleteApiException))
                {
                    if (breaker != null)
                    {
                        breaker.releaseTrial();
                    }

                    throw (cause != null) ? cause : ite;
                }

                AuthleteApiException ae = (AuthleteApiException) cause;
                lastError = ae;

                int     status      = ae.getStatusCode();
                boolean isTransient = retry.isTransient(status);

                // (3) Only transient failures count toward the breaker. A
                // permanent 4xx proves the backend is up and answering, so it
                // counts as a success (closing a half-open breaker and
                // releasing the trial slot).
                if (breaker != null)
                {
                    if (isTransient)
                    {
                        breaker.recordFailure();
                    }
                    else
                    {
                        breaker.recordSuccess();
                    }
                }

                // (4) Retry transient failures with exponential backoff, within budget.
                if (retryEnabled && isTransient && attempt < maxAttempts)
                {
                    Long reset = (status == 429)
                            ? retry.rateLimitResetMillis(ae.getResponseHeaders()) : null;

                    long delay   = backoff.delayMillis(attempt, reset);
                    long elapsed = System.currentTimeMillis() - start;

                    if (elapsed + delay <= maxTotalMillis)
                    {
                        logger.debug("Authlete API {} failed (status={}, attempt={}); retrying in {} ms.",
                                method.getName(), status, attempt, delay);

                        if (sleep(delay))
                        {
                            continue;
                        }
                    }
                }

                // Exhausted retries (or permanent error): try a stale fallback for
                // transient failures, otherwise surface the original exception.
                if (isTransient)
                {
                    Object stale = serveStale(policy, method, "transient failure, retries exhausted");

                    if (stale != null)
                    {
                        return stale;
                    }
                }

                throw ae;
            }
        }
    }


    /**
     * Compute the TTL to store a freshly fetched value under, capping
     * introspection results so a cached entry never reports a token as active
     * past its own expiry.
     */
    private long effectiveTtl(CachePolicy policy, Object result)
    {
        long ttl = policy.ttlMillis;

        if (policy.capByTokenExpiry && result instanceof IntrospectionResponse)
        {
            long expiresAt = ((IntrospectionResponse) result).getExpiresAt();

            if (expiresAt > 0)
            {
                long untilExpiry = expiresAt - System.currentTimeMillis();

                // Already expired: do not cache at all.
                if (untilExpiry <= 0)
                {
                    return 0;
                }

                ttl = Math.min(ttl, untilExpiry);
            }
        }

        return ttl;
    }


    private Object serveStale(CachePolicy policy, Method method, String reason)
    {
        if (policy == null)
        {
            return null;
        }

        Object stale = cache.getStale(policy.key);

        if (stale != null)
        {
            logger.warn("Serving stale cached response for Authlete API {} ({}).",
                    method.getName(), reason);
        }

        return stale;
    }


    /**
     * Sleep for the given duration. Returns {@code false} if interrupted, in
     * which case the caller should stop retrying.
     */
    private boolean sleep(long millis)
    {
        if (millis <= 0)
        {
            return true;
        }

        try
        {
            Thread.sleep(millis);
            return true;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
    }


    private static AuthleteApiException circuitOpenException(Method method)
    {
        return new AuthleteApiException(
                "Circuit breaker is open for Authlete API '" + method.getName()
                        + "'; failing fast to protect the service.",
                503, "Service Unavailable", null);
    }


    private Object invokeObjectMethod(Object proxy, Method method, Object[] args)
    {
        switch (method.getName())
        {
            case "equals":
                return proxy == args[0];

            case "hashCode":
                return System.identityHashCode(proxy);

            case "toString":
            default:
                return "ResilientAuthleteApi[" + delegate + "]";
        }
    }
}