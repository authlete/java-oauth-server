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
import static org.junit.Assert.assertTrue;
import java.util.function.LongSupplier;
import org.junit.Test;
import com.authlete.jaxrs.server.resilience.AuthleteCircuitBreaker.State;


public class AuthleteCircuitBreakerTest
{
    /** A clock whose value the test advances manually. */
    private final long[] now = { 0L };
    private final LongSupplier clock = () -> now[0];


    private AuthleteCircuitBreaker newBreaker()
    {
        // threshold=3 failures within a 30s window, open for 60s, 1 half-open trial.
        return new AuthleteCircuitBreaker(3, 30_000, 60_000, 1, clock);
    }


    @Test
    public void opensAfterThresholdFailures()
    {
        AuthleteCircuitBreaker cb = newBreaker();

        assertTrue(cb.allowRequest());
        cb.recordFailure();
        cb.recordFailure();
        assertEquals(State.CLOSED, cb.getState());

        cb.recordFailure(); // 3rd failure trips it open
        assertEquals(State.OPEN, cb.getState());
        assertFalse("open breaker fails fast", cb.allowRequest());
    }


    @Test
    public void successResetsFailureCount()
    {
        AuthleteCircuitBreaker cb = newBreaker();

        cb.recordFailure();
        cb.recordFailure();
        cb.recordSuccess(); // resets the count
        cb.recordFailure();
        cb.recordFailure();

        assertEquals("still closed after reset", State.CLOSED, cb.getState());
    }


    @Test
    public void failuresOutsideWindowDoNotAccumulate()
    {
        AuthleteCircuitBreaker cb = newBreaker();

        cb.recordFailure();
        cb.recordFailure();

        // Advance beyond the 30s rolling window: the count restarts.
        now[0] += 31_000;
        cb.recordFailure();
        cb.recordFailure();

        assertEquals(State.CLOSED, cb.getState());
    }


    @Test
    public void halfOpenSuccessClosesBreaker()
    {
        AuthleteCircuitBreaker cb = newBreaker();
        trip(cb);

        // Before the open timeout, requests are rejected.
        now[0] += 30_000;
        assertFalse(cb.allowRequest());

        // After the open timeout, a single trial is allowed (half-open).
        now[0] += 31_000;
        assertTrue("half-open trial allowed", cb.allowRequest());
        assertEquals(State.HALF_OPEN, cb.getState());
        assertFalse("only one trial permitted", cb.allowRequest());

        cb.recordSuccess();
        assertEquals("success closes the breaker", State.CLOSED, cb.getState());
    }


    @Test
    public void halfOpenFailureReopensBreaker()
    {
        AuthleteCircuitBreaker cb = newBreaker();
        trip(cb);

        now[0] += 61_000;
        assertTrue(cb.allowRequest()); // half-open trial
        cb.recordFailure();

        assertEquals("failed trial reopens", State.OPEN, cb.getState());
        assertFalse(cb.allowRequest());
    }


    @Test
    public void releaseTrialFreesHalfOpenSlot()
    {
        AuthleteCircuitBreaker cb = newBreaker();
        trip(cb);

        now[0] += 61_000;
        assertTrue(cb.allowRequest()); // half-open trial reserved
        assertFalse("slot taken", cb.allowRequest());

        // The trial ended without a verdict on backend health (e.g. an
        // unexpected local error): the slot must become available again.
        cb.releaseTrial();
        assertEquals("still half-open", State.HALF_OPEN, cb.getState());
        assertTrue("slot available again", cb.allowRequest());
    }


    private void trip(AuthleteCircuitBreaker cb)
    {
        cb.recordFailure();
        cb.recordFailure();
        cb.recordFailure();
        assertEquals(State.OPEN, cb.getState());
    }
}
