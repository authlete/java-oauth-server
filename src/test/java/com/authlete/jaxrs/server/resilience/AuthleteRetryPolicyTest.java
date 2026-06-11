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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;


public class AuthleteRetryPolicyTest
{
    private final AuthleteRetryPolicy policy = new AuthleteRetryPolicy();


    @Test
    public void transientStatuses()
    {
        assertTrue("no response is transient",        policy.isTransient(0));
        assertTrue("429 is transient",                policy.isTransient(429));
        assertTrue("500 is transient",                policy.isTransient(500));
        assertTrue("502 is transient",                policy.isTransient(502));
        assertTrue("503 is transient",                policy.isTransient(503));
        assertTrue("599 is transient",                policy.isTransient(599));
    }


    @Test
    public void permanentStatuses()
    {
        assertFalse("400 is permanent", policy.isTransient(400));
        assertFalse("401 is permanent", policy.isTransient(401));
        assertFalse("403 is permanent", policy.isTransient(403));
        assertFalse("404 is permanent", policy.isTransient(404));
        assertFalse("200 is not retried", policy.isTransient(200));
    }


    @Test
    public void rateLimitResetParsedFromSeconds()
    {
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("RateLimit-Reset", Collections.singletonList("3"));

        assertEquals(Long.valueOf(3000L), policy.rateLimitResetMillis(headers));
    }


    @Test
    public void rateLimitResetHeaderIsCaseInsensitive()
    {
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("ratelimit-reset", Arrays.asList("2"));

        assertEquals(Long.valueOf(2000L), policy.rateLimitResetMillis(headers));
    }


    @Test
    public void rateLimitResetAbsentOrInvalid()
    {
        assertNull(policy.rateLimitResetMillis(null));
        assertNull(policy.rateLimitResetMillis(new HashMap<>()));

        Map<String, java.util.List<String>> bad = new HashMap<>();
        bad.put("RateLimit-Reset", Collections.singletonList("not-a-number"));
        assertNull(policy.rateLimitResetMillis(bad));

        Map<String, java.util.List<String>> zero = new HashMap<>();
        zero.put("RateLimit-Reset", Collections.singletonList("0"));
        assertNull("non-positive reset is ignored", policy.rateLimitResetMillis(zero));
    }
}
