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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;


/**
 * A small, thread-safe, in-memory TTL cache for responses of idempotent
 * Authlete API calls.
 *
 * <p>
 * Each entry has two lifetimes:
 * </p>
 * <ul>
 *   <li><b>fresh</b> &mdash; until its TTL elapses; {@link #getFresh(String)}
 *       returns it and the value is served directly without calling Authlete.</li>
 *   <li><b>stale</b> &mdash; for an additional {@code staleMillis} after the TTL;
 *       {@link #getStale(String)} returns it. Stale values are used only as a
 *       fast-fail fallback while the circuit breaker is open, so that a degraded
 *       but functional response can be served during an outage.</li>
 * </ul>
 *
 * <p>
 * The cache is intentionally dependency-free (a plain {@link ConcurrentHashMap})
 * so it is easy to read and copy. For cross-instance caching, replace the map
 * with a shared store such as Redis.
 * </p>
 *
 * <p>
 * A single instance is shared across all cached methods; cache keys are
 * namespaced by method (see {@link AuthleteCacheableMethods}) so there is no risk of
 * collision between, say, a client id and a service-configuration request.
 * </p>
 */
class AuthleteResponseCache
{
    private static final class Entry
    {
        final Object value;
        final long   freshUntil;
        final long   staleUntil;

        Entry(Object value, long freshUntil, long staleUntil)
        {
            this.value      = value;
            this.freshUntil = freshUntil;
            this.staleUntil = staleUntil;
        }
    }


    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<String, Entry>();
    private final long         staleMillis;
    private final int          maxEntries;
    private final LongSupplier clock;


    AuthleteResponseCache(long staleMillis, int maxEntries)
    {
        this(staleMillis, maxEntries, System::currentTimeMillis);
    }


    /**
     * Package-private constructor that allows an injected clock for testing.
     */
    AuthleteResponseCache(long staleMillis, int maxEntries, LongSupplier clock)
    {
        this.staleMillis = staleMillis;
        this.maxEntries  = maxEntries;
        this.clock       = clock;
    }


    /**
     * Return the cached value for the key only if it is still fresh, otherwise
     * {@code null}.
     */
    Object getFresh(String key)
    {
        Entry e = map.get(key);

        if (e == null)
        {
            return null;
        }

        long now = clock.getAsLong();

        if (now < e.freshUntil)
        {
            return e.value;
        }

        // Expired beyond the stale window: drop it eagerly.
        if (now >= e.staleUntil)
        {
            map.remove(key, e);
        }

        return null;
    }


    /**
     * Return the cached value for the key if it still exists within the stale
     * window (whether fresh or only stale), otherwise {@code null}.
     */
    Object getStale(String key)
    {
        Entry e = map.get(key);

        if (e == null)
        {
            return null;
        }

        long now = clock.getAsLong();

        if (now < e.staleUntil)
        {
            return e.value;
        }

        map.remove(key, e);

        return null;
    }


    /**
     * Store a value under the key with the given TTL (milliseconds). The stale
     * window is added on top of the TTL.
     */
    void put(String key, Object value, long ttlMillis)
    {
        if (value == null || ttlMillis <= 0)
        {
            return;
        }

        long now = clock.getAsLong();

        // Bound memory: evict expired entries first, then refuse new keys if
        // still over the limit (existing keys are always allowed to refresh).
        if (map.size() >= maxEntries && !map.containsKey(key))
        {
            purgeExpired(now);

            if (map.size() >= maxEntries)
            {
                return;
            }
        }

        map.put(key, new Entry(value, now + ttlMillis, now + ttlMillis + staleMillis));
    }


    private void purgeExpired(long now)
    {
        for (Map.Entry<String, Entry> e : map.entrySet())
        {
            if (now >= e.getValue().staleUntil)
            {
                map.remove(e.getKey(), e.getValue());
            }
        }
    }


    /**
     * Remove all entries. Exposed for completeness / testing.
     */
    void clear()
    {
        map.clear();
    }


    int size()
    {
        return map.size();
    }
}