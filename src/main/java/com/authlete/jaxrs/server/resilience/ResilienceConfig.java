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


/**
 * Typed, defaulted access to the resilience configuration defined in
 * {@code resilience.properties} (overridable via JVM system properties).
 *
 * <p>
 * Values are read once at class-load time. The defaults below match the
 * recommendations in Authlete's "Rate Limit Best Practices" guide, so the
 * resilience layer behaves sensibly even when {@code resilience.properties}
 * is absent.
 * </p>
 */
public final class ResilienceConfig
{
    private static final ResilienceProperties PROPS = new ResilienceProperties();

    // Master switch.
    private final boolean enabled;

    // Cache.
    private final boolean cacheEnabled;
    private final long    cacheTtlServiceConfiguration;
    private final long    cacheTtlServiceJwks;
    private final long    cacheTtlClient;
    private final long    cacheTtlCredentialIssuerMetadata;
    private final long    cacheTtlCredentialIssuerJwks;
    private final long    cacheTtlIntrospection;
    private final long    cacheTtlStandardIntrospection;
    private final long    cacheStaleMillis;
    private final int     cacheMaxEntries;

    // Retry / backoff.
    private final boolean retryEnabled;
    private final int     retryMaxAttempts;
    private final long    retryBaseDelayMillis;
    private final long    retryMaxTotalMillis;
    private final long    retryJitterMillis;

    // Circuit breaker.
    private final boolean breakerEnabled;
    private final int     breakerFailureThreshold;
    private final long    breakerWindowMillis;
    private final long    breakerOpenMillis;
    private final int     breakerHalfOpenTrials;


    /**
     * Build a configuration snapshot from {@code resilience.properties} and
     * system properties.
     */
    public ResilienceConfig()
    {
        enabled = PROPS.getBoolean("resilience.enabled", true);

        cacheEnabled                     = PROPS.getBoolean("resilience.cache.enabled", true);
        cacheTtlServiceConfiguration     = seconds("resilience.cache.ttl.serviceConfiguration", 600);
        cacheTtlServiceJwks              = seconds("resilience.cache.ttl.serviceJwks", 600);
        cacheTtlClient                   = seconds("resilience.cache.ttl.client", 300);
        cacheTtlCredentialIssuerMetadata = seconds("resilience.cache.ttl.credentialIssuerMetadata", 600);
        cacheTtlCredentialIssuerJwks     = seconds("resilience.cache.ttl.credentialIssuerJwks", 600);
        cacheTtlIntrospection            = seconds("resilience.cache.ttl.introspection", 30);
        cacheTtlStandardIntrospection    = seconds("resilience.cache.ttl.standardIntrospection", 30);
        cacheStaleMillis                 = seconds("resilience.cache.staleSeconds", 1800);
        cacheMaxEntries                  = PROPS.getInt("resilience.cache.maxEntries", 10000);

        retryEnabled         = PROPS.getBoolean("resilience.retry.enabled", true);
        retryMaxAttempts     = PROPS.getInt("resilience.retry.maxAttempts", 4);
        retryBaseDelayMillis = PROPS.getLong("resilience.retry.baseDelayMillis", 500);
        retryMaxTotalMillis  = PROPS.getLong("resilience.retry.maxTotalMillis", 60000);
        retryJitterMillis    = PROPS.getLong("resilience.retry.jitterMillis", 200);

        breakerEnabled          = PROPS.getBoolean("resilience.breaker.enabled", true);
        breakerFailureThreshold = PROPS.getInt("resilience.breaker.failureThreshold", 5);
        breakerWindowMillis     = seconds("resilience.breaker.windowSeconds", 30);
        breakerOpenMillis       = seconds("resilience.breaker.openSeconds", 60);
        breakerHalfOpenTrials   = PROPS.getInt("resilience.breaker.halfOpenTrials", 1);
    }


    /**
     * Read a value expressed in seconds and return it in milliseconds.
     */
    private static long seconds(String key, long defaultSeconds)
    {
        return PROPS.getLong(key, defaultSeconds) * 1000L;
    }


    public boolean isEnabled()
    {
        return enabled;
    }


    public boolean isCacheEnabled()
    {
        return cacheEnabled;
    }


    public long getCacheTtlServiceConfiguration()
    {
        return cacheTtlServiceConfiguration;
    }


    public long getCacheTtlServiceJwks()
    {
        return cacheTtlServiceJwks;
    }


    public long getCacheTtlClient()
    {
        return cacheTtlClient;
    }


    public long getCacheTtlCredentialIssuerMetadata()
    {
        return cacheTtlCredentialIssuerMetadata;
    }


    public long getCacheTtlCredentialIssuerJwks()
    {
        return cacheTtlCredentialIssuerJwks;
    }


    public long getCacheTtlIntrospection()
    {
        return cacheTtlIntrospection;
    }


    public long getCacheTtlStandardIntrospection()
    {
        return cacheTtlStandardIntrospection;
    }


    public long getCacheStaleMillis()
    {
        return cacheStaleMillis;
    }


    public int getCacheMaxEntries()
    {
        return cacheMaxEntries;
    }


    public boolean isRetryEnabled()
    {
        return retryEnabled;
    }


    public int getRetryMaxAttempts()
    {
        return retryMaxAttempts;
    }


    public long getRetryBaseDelayMillis()
    {
        return retryBaseDelayMillis;
    }


    public long getRetryMaxTotalMillis()
    {
        return retryMaxTotalMillis;
    }


    public long getRetryJitterMillis()
    {
        return retryJitterMillis;
    }


    public boolean isBreakerEnabled()
    {
        return breakerEnabled;
    }


    public int getBreakerFailureThreshold()
    {
        return breakerFailureThreshold;
    }


    public long getBreakerWindowMillis()
    {
        return breakerWindowMillis;
    }


    public long getBreakerOpenMillis()
    {
        return breakerOpenMillis;
    }


    public int getBreakerHalfOpenTrials()
    {
        return breakerHalfOpenTrials;
    }
}