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


import java.util.concurrent.ConcurrentHashMap;


/**
 * Lazily creates and holds one {@link AuthleteCircuitBreaker} per Authlete API method.
 *
 * <p>
 * Keeping breakers per method isolates failures: a breaker that has tripped for
 * one endpoint (e.g. client management) does not affect another, higher-priority
 * endpoint (e.g. introspection), as recommended by the "Isolate Resources"
 * advice in Authlete's "Rate Limit Best Practices" guide.
 * </p>
 */
class AuthleteCircuitBreakerRegistry
{
    private final ConcurrentHashMap<String, AuthleteCircuitBreaker> breakers =
            new ConcurrentHashMap<String, AuthleteCircuitBreaker>();

    private final int  failureThreshold;
    private final long windowMillis;
    private final long openMillis;
    private final int  halfOpenTrials;


    AuthleteCircuitBreakerRegistry(ResilienceConfig config)
    {
        this.failureThreshold = config.getBreakerFailureThreshold();
        this.windowMillis     = config.getBreakerWindowMillis();
        this.openMillis       = config.getBreakerOpenMillis();
        this.halfOpenTrials   = config.getBreakerHalfOpenTrials();
    }


    /**
     * Get the breaker for the given method name, creating it on first use.
     */
    AuthleteCircuitBreaker forMethod(String methodName)
    {
        AuthleteCircuitBreaker existing = breakers.get(methodName);

        if (existing != null)
        {
            return existing;
        }

        AuthleteCircuitBreaker created =
                new AuthleteCircuitBreaker(failureThreshold, windowMillis, openMillis, halfOpenTrials);

        AuthleteCircuitBreaker previous = breakers.putIfAbsent(methodName, created);

        return (previous != null) ? previous : created;
    }
}