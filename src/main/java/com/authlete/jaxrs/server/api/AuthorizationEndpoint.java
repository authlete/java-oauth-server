/*
 * Copyright (C) 2016 Authlete, Inc.
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
package com.authlete.jaxrs.server.api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseAuthorizationEndpoint;


/**
 * An implementation of OAuth 2.0 authorization endpoint with OpenID Connect support.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
 *      >RFC 6749, 3.1. Authorization Endpoint</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.1.2. Authorization Endpoint (Authorization Code Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#ImplicitAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.2.2. Authorization Endpoint (Implicit Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#HybridAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.3.2. Authorization Endpoint (Hybrid Flow)</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/authorization")
public class AuthorizationEndpoint extends BaseAuthorizationEndpoint
{
    /**
     * The authorization endpoint for {@code GET} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MUST support {@code GET} method.
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
     *      >RFC 6749, 3.1 Authorization Endpoint</a>
     */
    @GET
    public Response get(
            @Context HttpServletRequest request,
            @Context UriInfo uriInfo)
    {
        // Handle the authorization request.
        return handle(request, uriInfo.getQueryParameters());
    }


    /**
     * The authorization endpoint for {@code POST} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MAY support {@code POST} method.
     * </p>
     *
     * <p>
     * In addition, <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest"
     * >OpenID Connect Core 1.0, 3.1.2.1. Authentication Request</a> says
     * that the authorization endpoint MUST support {@code POST} method.
     * </p>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Handle the authorization request.
        return handle(request, parameters);
    }


    /**
     * Handle the authorization request.
     */
    private Response handle(HttpServletRequest request, MultivaluedMap<String, String> parameters)
    {
        return handle(AuthleteApiFactory.getDefaultApi(),
                new AuthorizationRequestHandlerSpiImpl(request), parameters);
    }
}
