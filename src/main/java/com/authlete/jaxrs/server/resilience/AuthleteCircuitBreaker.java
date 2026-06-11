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


import java.util.function.LongSupplier;


/**
 * A classic three-state circuit breaker (Closed / Open / Half-Open) guarding a
 * single Authlete API method.
 *
 * <ul>
 *   <li><b>Closed</b> &mdash; normal operation; transient failures are counted
 *       within a rolling window. Reaching the failure threshold trips the
 *       breaker <b>Open</b>.</li>
 *   <li><b>Open</b> &mdash; calls fail fast without touching the backend. After
 *       the open timeout elapses, the breaker moves to <b>Half-Open</b>.</li>
 *   <li><b>Half-Open</b> &mdash; a limited number of trial calls are allowed
 *       through. A success closes the breaker; a failure reopens it.</li>
 * </ul>
 *
 * <p>
 * One breaker is kept per API method (see {@link AuthleteCircuitBreakerRegistry}) so
 * that failures in one endpoint (e.g. client management) do not block an
 * unrelated, higher-priority endpoint (e.g. introspection).
 * </p>
 *
 * <p>
 * All state transitions are guarded by intrinsic locking; the breaker is safe
 * for concurrent use.
 * </p>
 */
class AuthleteCircuitBreaker
{
    enum State
    {
        CLOSED,
        OPEN,
        HALF_OPEN
    }


    private final int          failureThreshold;
    private final long         windowMillis;
    private final long         openMillis;
    private final int          halfOpenTrials;
    private final LongSupplier clock;

    private State   state            = State.CLOSED;
    private int     failureCount     = 0;
    private boolean windowOpen       = false;
    private long    windowStart      = 0L;
    private long    openedAt         = 0L;
    private int     halfOpenInFlight = 0;


    AuthleteCircuitBreaker(int failureThreshold, long windowMillis, long openMillis, int halfOpenTrials)
    {
        this(failureThreshold, windowMillis, openMillis, halfOpenTrials, System::currentTimeMillis);
    }


    /**
     * Package-private constructor that allows an injected clock for testing.
     */
    AuthleteCircuitBreaker(int failureThreshold, long windowMillis, long openMillis,
                   int halfOpenTrials, LongSupplier clock)
    {
        this.failureThreshold = failureThreshold;
        this.windowMillis     = windowMillis;
        this.openMillis       = openMillis;
        this.halfOpenTrials   = Math.max(1, halfOpenTrials);
        this.clock            = clock;
    }


    /**
     * Decide whether a request may proceed right now. When this returns
     * {@code true} in the half-open state, a trial slot is reserved and must be
     * released via {@link #recordSuccess()} or {@link #recordFailure()}.
     */
    synchronized boolean allowRequest()
    {
        long now = clock.getAsLong();

        switch (state)
        {
            case CLOSED:
                return true;

            case OPEN:
                if (now - openedAt >= openMillis)
                {
                    // Time to probe whether the backend has recovered.
                    state            = State.HALF_OPEN;
                    halfOpenInFlight = 1;
                    return true;
                }
                return false;

            case HALF_OPEN:
            default:
                if (halfOpenInFlight < halfOpenTrials)
                {
                    halfOpenInFlight++;
                    return true;
                }
                return false;
        }
    }


    /**
     * Record a successful call.
     */
    synchronized void recordSuccess()
    {
        // Any success (in either Closed or Half-Open) restores normal operation.
        reset();
    }


    /**
     * Release a half-open trial slot without judging backend health. Used when
     * a call granted by {@link #allowRequest()} ends in a way that says nothing
     * about whether the backend has recovered (e.g. an unexpected local
     * error), so the slot becomes available for the next probe.
     */
    synchronized void releaseTrial()
    {
        if (state == State.HALF_OPEN && halfOpenInFlight > 0)
        {
            halfOpenInFlight--;
        }
    }


    /**
     * Record a (transient) failure.
     */
    synchronized void recordFailure()
    {
        long now = clock.getAsLong();

        if (state == State.HALF_OPEN)
        {
            // The probe failed: reopen immediately.
            trip(now);
            return;
        }

        // CLOSED: count failures within the rolling window.
        if (!windowOpen || now - windowStart > windowMillis)
        {
            // Start a fresh window.
            windowOpen   = true;
            windowStart  = now;
            failureCount = 0;
        }

        failureCount++;

        if (failureCount >= failureThreshold)
        {
            trip(now);
        }
    }


    private void trip(long now)
    {
        state            = State.OPEN;
        openedAt         = now;
        failureCount     = 0;
        windowOpen       = false;
        halfOpenInFlight = 0;
    }


    private void reset()
    {
        state            = State.CLOSED;
        failureCount     = 0;
        windowOpen       = false;
        halfOpenInFlight = 0;
    }


    synchronized State getState()
    {
        return state;
    }
}