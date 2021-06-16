/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.obb;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.BaseClientRegistrationEndpoint;


/**
 * A sample implementation of Client Registration Endpoint that
 * conforms to requirements of Open Banking Brasil.
 *
 * <p>
 * NOTE: It is not assured that this implementation is perfect.
 * There are no warranties even if you have troubles by using
 * and/or referencing this implementation.
 * </p>
 *
 * @see <a href="https://openbanking-brasil.github.io/specs-seguranca/open-banking-brasil-dynamic-client-registration-1_ID1.html"
 *      >Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0 Implementers Draft 1</a>
 */
@Path("/api/obb/register")
public class ClientRegistrationEndpoint extends BaseClientRegistrationEndpoint
{
    /**
     * Client Registration Endpoint.
     *
     * <p>
     * According to "3.1. Client Registration Request" of RFC 7591 (OAuth 2.0
     * Dynamic Client Registration Protocol), the Client Registration Endpoint
     * accepts POST requests whose Content-Type is application/json.
     * </p>
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc7591.html"
     *      >RFC 7591 OAuth 2.0 Dynamic Client Registration Protocol</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @Context HttpServletRequest request,
            String body)
    {
        // Perform validation specific to Open Banking Brasil.
        // The resultant map holds client metadata.
        Map<String, Object> metadata = new ClientRegisterProcessor().process(request, body);

        // Register the client metadata by calling Authlete's /api/client/registration API.
        return handleRegister(
                AuthleteApiFactory.getDefaultApi(), Utils.toJson(metadata), authorization);
    }


    @Override
    protected void onError(WebApplicationException exception)
    {
        exception.printStackTrace();
    }
}
