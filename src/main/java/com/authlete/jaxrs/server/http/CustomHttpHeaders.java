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
package com.authlete.jaxrs.server.http;


/**
 * Custom HTTP headers.
 */
public class CustomHttpHeaders
{
    /**
     * The FAPI Interaction ID ({@code x-fapi-interaction-id})
     *
     * @see <a href="https://openid.net/specs/openid-financial-api-part-1-1_0.html">
     *      FAPI 1.0 Baseline</a>
     *
     * @see <a href="https://bitbucket.org/openid/fapi/src/master/FAPI_2_0_Implementation_Advice.md">
     *      FAPI 2.0 Implementation Advice</a>
     */
    public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
}
