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
import static org.junit.Assert.assertTrue;
import java.util.Random;
import org.junit.Test;


public class AuthleteBackoffTest
{
    @Test
    public void exponentialScheduleWithoutJitter()
    {
        // base=500, max=60000, jitter=0
        AuthleteBackoff backoff = new AuthleteBackoff(500, 60000, 0, new Random(0));

        assertEquals(500,  backoff.delayMillis(1, null)); // 500 * 2^0
        assertEquals(1000, backoff.delayMillis(2, null)); // 500 * 2^1
        assertEquals(2000, backoff.delayMillis(3, null)); // 500 * 2^2
        assertEquals(4000, backoff.delayMillis(4, null)); // 500 * 2^3
    }


    @Test
    public void delayIsCappedAtMax()
    {
        AuthleteBackoff backoff = new AuthleteBackoff(500, 3000, 0, new Random(0));

        // 500 * 2^3 = 4000 would exceed the 3000 cap.
        assertEquals(3000, backoff.delayMillis(4, null));
        assertEquals(3000, backoff.delayMillis(20, null));
    }


    @Test
    public void explicitDelayIsHonoured()
    {
        AuthleteBackoff backoff = new AuthleteBackoff(500, 60000, 0, new Random(0));

        // A RateLimit-Reset of 5s overrides the exponential value.
        assertEquals(5000, backoff.delayMillis(1, 5000L));
    }


    @Test
    public void jitterStaysWithinBounds()
    {
        long base   = 500;
        long jitter = 200;
        AuthleteBackoff backoff = new AuthleteBackoff(base, 60000, jitter, new Random(42));

        for (int i = 0; i < 100; i++)
        {
            long delay = backoff.delayMillis(1, null);
            assertTrue("delay >= base", delay >= base);
            assertTrue("delay < base + jitter", delay < base + jitter);
        }
    }
}
