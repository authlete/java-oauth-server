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
import static org.junit.Assert.assertNull;
import java.util.function.LongSupplier;
import org.junit.Test;


public class AuthleteResponseCacheTest
{
    private final long[] now = { 0L };
    private final LongSupplier clock = () -> now[0];


    @Test
    public void freshHitWithinTtl()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 100, clock);
        cache.put("k", "v", 500);

        now[0] = 499;
        assertEquals("v", cache.getFresh("k"));
    }


    @Test
    public void expiresAfterTtlButStaleRemains()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 100, clock);
        cache.put("k", "v", 500); // fresh until 500, stale until 1500

        now[0] = 600;
        assertNull("no longer fresh", cache.getFresh("k"));
        assertEquals("but available as stale", "v", cache.getStale("k"));
    }


    @Test
    public void staleEntryDroppedAfterStaleWindow()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 100, clock);
        cache.put("k", "v", 500); // stale until 1500

        now[0] = 1600;
        assertNull(cache.getStale("k"));
        assertNull(cache.getFresh("k"));
    }


    @Test
    public void nullValueAndNonPositiveTtlAreNotCached()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 100, clock);
        cache.put("a", null, 500);
        cache.put("b", "v", 0);

        assertNull(cache.getFresh("a"));
        assertNull(cache.getFresh("b"));
        assertEquals(0, cache.size());
    }


    @Test
    public void maxEntriesIsBounded()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 2, clock);
        cache.put("a", "1", 500);
        cache.put("b", "2", 500);
        cache.put("c", "3", 500); // exceeds the limit; rejected while others are live

        assertEquals(2, cache.size());
        assertNull("new key rejected at capacity", cache.getFresh("c"));

        // Updating an existing key is still allowed at capacity.
        cache.put("a", "1b", 500);
        assertEquals("1b", cache.getFresh("a"));
    }


    @Test
    public void removeIfDropsOnlyMatchingKeys()
    {
        AuthleteResponseCache cache = new AuthleteResponseCache(1000, 100, clock);
        cache.put("introspection::tokenA|scope1", "a1", 500);
        cache.put("introspection::tokenA|scope2", "a2", 500);
        cache.put("introspection::tokenB|scope1", "b1", 500);

        int removed = cache.removeIf(key -> key.startsWith("introspection::tokenA|"));

        assertEquals(2, removed);
        assertNull(cache.getFresh("introspection::tokenA|scope1"));
        assertNull(cache.getFresh("introspection::tokenA|scope2"));
        assertEquals("other tokens untouched", "b1",
                cache.getFresh("introspection::tokenB|scope1"));
    }
}
