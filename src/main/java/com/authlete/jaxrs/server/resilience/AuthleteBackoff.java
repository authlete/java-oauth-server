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


import java.util.Random;


/**
 * Computes the delay before a retry using exponential backoff with random
 * jitter, as recommended in Authlete's "Rate Limit Best Practices" guide
 * (first retry after ~500&nbsp;ms, then ~1&nbsp;s, ~2&nbsp;s, ...).
 *
 * <p>
 * Jitter spreads retries from many clients across time so they do not all
 * fire simultaneously and create a renewed traffic spike. When the server
 * tells us exactly how long to wait (via a {@code RateLimit-Reset} header on a
 * {@code 429}), that value is honoured instead of the computed delay.
 * </p>
 */
class AuthleteBackoff
{
    private final long   baseDelayMillis;
    private final long   maxDelayMillis;
    private final long   jitterMillis;
    private final Random random;


    AuthleteBackoff(long baseDelayMillis, long maxDelayMillis, long jitterMillis)
    {
        this(baseDelayMillis, maxDelayMillis, jitterMillis, new Random());
    }


    /**
     * Package-private constructor that allows an injected {@link Random} for
     * deterministic testing.
     */
    AuthleteBackoff(long baseDelayMillis, long maxDelayMillis, long jitterMillis, Random random)
    {
        this.baseDelayMillis = baseDelayMillis;
        this.maxDelayMillis  = maxDelayMillis;
        this.jitterMillis    = jitterMillis;
        this.random          = random;
    }


    /**
     * Compute the delay (milliseconds) before the given retry attempt.
     *
     * @param attempt
     *         The 1-based retry number (1 = first retry, 2 = second retry, ...).
     *
     * @param explicitDelayMillis
     *         An explicit delay requested by the server (e.g. from a
     *         {@code RateLimit-Reset} header), or {@code null} if none. When
     *         present, it forms the base delay instead of the exponential value.
     *
     * @return
     *         The delay to sleep, clamped to {@code [0, maxDelayMillis]}.
     */
    long delayMillis(int attempt, Long explicitDelayMillis)
    {
        long base;

        if (explicitDelayMillis != null)
        {
            // Honour the server's instruction.
            base = explicitDelayMillis;
        }
        else
        {
            // baseDelay * 2^(attempt-1), guarding against overflow.
            int shift = Math.max(0, attempt - 1);

            if (shift >= 62)
            {
                base = maxDelayMillis;
            }
            else
            {
                base = baseDelayMillis << shift;
            }
        }

        long jitter = (jitterMillis > 0) ? (long) (random.nextDouble() * jitterMillis) : 0L;

        long delay = base + jitter;

        if (delay < 0 || delay > maxDelayMillis)
        {
            delay = maxDelayMillis;
        }

        return delay;
    }
}