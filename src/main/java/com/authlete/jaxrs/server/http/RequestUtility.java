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


import javax.ws.rs.container.ContainerRequestContext;


public class RequestUtility
{
    /**
     * Extract the value of the {@code x-fapi-interaction-id} header from the
     * given {@link ContainerRequestContext} instance.
     */
    public static String extractFapiInteractionId(ContainerRequestContext ctx)
    {
        return ctx.getHeaderString(CustomHttpHeaders.X_FAPI_INTERACTION_ID);
    }
}
