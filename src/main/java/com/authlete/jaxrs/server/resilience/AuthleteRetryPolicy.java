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


import java.util.List;
import java.util.Map;


/**
 * Decides whether a failed Authlete API call is <i>transient</i> (worth
 * retrying) or <i>permanent</i> (never retried), per Authlete's "Rate Limit
 * Best Practices" guide.
 *
 * <p>
 * Transient failures are:
 * </p>
 * <ul>
 *   <li>{@code 429 Too Many Requests} &mdash; retry only after the delay in the
 *       {@code RateLimit-Reset} response header (see
 *       {@link #rateLimitResetMillis(Map)}).</li>
 *   <li>{@code 502 Bad Gateway}, {@code 503 Service Unavailable}, and any other
 *       {@code 5xx}.</li>
 *   <li>Connection-level errors with no HTTP response (status {@code 0}).</li>
 * </ul>
 *
 * <p>
 * Permanent failures are the remaining {@code 4xx} codes (e.g. {@code 400},
 * {@code 401}, {@code 403}); these can never succeed without changing the
 * request, so they are surfaced immediately.
 * </p>
 */
class AuthleteRetryPolicy
{
    // Common spellings of the rate-limit reset header, matched case-insensitively.
    private static final String[] RESET_HEADERS = {
        "RateLimit-Reset", "Ratelimit-Reset", "X-RateLimit-Reset", "Retry-After"
    };


    /**
     * Tell whether the given HTTP status code denotes a transient failure that
     * may be retried.
     *
     * @param statusCode
     *         The HTTP status code from {@code AuthleteApiException.getStatusCode()}.
     *         A value of {@code 0} means no HTTP response was received (a
     *         connection-level error), which is treated as transient.
     */
    boolean isTransient(int statusCode)
    {
        // No HTTP response (connection refused, timeout, DNS failure, ...).
        if (statusCode == 0)
        {
            return true;
        }

        // Too Many Requests.
        if (statusCode == 429)
        {
            return true;
        }

        // Any server-side error.
        if (statusCode >= 500 && statusCode < 600)
        {
            return true;
        }

        // Everything else (notably 4xx) is permanent.
        return false;
    }


    /**
     * Extract the rate-limit reset delay (milliseconds) from the response
     * headers, or {@code null} when no usable value is present.
     *
     * <p>
     * The header value is interpreted as a number of seconds to wait before the
     * next attempt. Non-numeric or non-positive values are ignored.
     * </p>
     */
    Long rateLimitResetMillis(Map<String, List<String>> headers)
    {
        if (headers == null || headers.isEmpty())
        {
            return null;
        }

        for (String wanted : RESET_HEADERS)
        {
            String value = findHeader(headers, wanted);

            if (value == null)
            {
                continue;
            }

            try
            {
                long seconds = Long.parseLong(value.trim());

                if (seconds > 0)
                {
                    return seconds * 1000L;
                }
            }
            catch (NumberFormatException e)
            {
                // Try the next candidate header.
            }
        }

        return null;
    }


    private static String findHeader(Map<String, List<String>> headers, String name)
    {
        for (Map.Entry<String, List<String>> e : headers.entrySet())
        {
            String key = e.getKey();

            if (key != null && key.equalsIgnoreCase(name))
            {
                List<String> values = e.getValue();

                if (values != null && !values.isEmpty())
                {
                    return values.get(0);
                }
            }
        }

        return null;
    }
}