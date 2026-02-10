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
package com.authlete.jaxrs.server.decorator;


import java.io.IOException;
import java.util.UUID;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import com.authlete.jaxrs.server.http.CustomHttpHeaders;
import com.authlete.jaxrs.server.http.RequestUtility;


/**
 * A filter to add the {@code x-fapi-interaction-id} HTTP header to the HTTP
 * response.
 *
 * <p>
 * When the HTTP request contains the {@code x-fapi-interaction-id} HTTP header,
 * the same value is used. Otherwise, a random UUID is generated and used as the
 * value of the {@code x-fapi-interaction-id} HTTP header of the HTTP response.
 * </p>
 *
 * @see <a href="https://bitbucket.org/openid/fapi/src/master/FAPI_2_0_Implementation_Advice.md">
 *      FAPI 2.0 Implementation Advice</a>
 */
@Provider
@Priority(DecoratorPriorities.FAPI_INTERACTION_ID_RESPONSE_FILTER)
public class FapiInteractionIdResponseFilter implements ContainerResponseFilter
{
    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException
    {
        // If the response already contains the x-fapi-interaction-id HTTP header
        // (e.g., set by an endpoint such as the OBB endpoints), do nothing.
        if (responseContext.getHeaders().containsKey(CustomHttpHeaders.X_FAPI_INTERACTION_ID))
        {
            return;
        }

        // The value of the x-fapi-interaction-id HTTP header in the HTTP request.
        String interactionId = RequestUtility.extractFapiInteractionId(requestContext);

        // If the request does not contain the x-fapi-interaction-id HTTP header.
        if (interactionId == null)
        {
            // Generate a random x-fapi-interaction-id.
            interactionId = generateInteractionId();
        }

        // Add the x-fapi-interaction-id HTTP header to the HTTP response.
        //
        // Note that even if the value of the x-fapi-interaction-id HTTP header
        // in the HTTP request is malformed, the malformed value is used as is.
        responseContext.getHeaders().add(
                CustomHttpHeaders.X_FAPI_INTERACTION_ID, interactionId);
    }


    private static final String generateInteractionId()
    {
        return UUID.randomUUID().toString();
    }
}
