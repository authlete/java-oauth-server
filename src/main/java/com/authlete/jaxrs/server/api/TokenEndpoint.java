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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseTokenEndpoint;


/**
 * An implementation of OAuth 2.0 token endpoint with OpenID Connect support.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.2"
 *      >RFC 6749, 3.2. Token Endpoint</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#HybridTokenEndpoint"
 *      >OpenID Connect Core 1.0, 3.3.3. Token Endpoint</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/token")
public class TokenEndpoint extends BaseTokenEndpoint
{
    /**
     * The token endpoint for {@code POST} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.2">RFC 6749,
     * 3.2. Token Endpoint</a> says:
     * </p>
     *
     * <blockquote>
     * <i>The client MUST use the HTTP "POST" method when making access
     * token requests.</i>
     * </blockquote>
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-2.3">RFC 6749,
     * 2.3. Client Authentication</a> mentions (1) HTTP Basic Authentication
     * and (2) {@code client_id} &amp; {@code client_secret} parameters in
     * the request body as the means of client authentication. This
     * implementation supports the both means.
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.2"
     *      >RFC 6749, 3.2. Token Endpoint</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            MultivaluedMap<String, String> parameters,
            @Context HttpServletRequest request)
    {
        String[] clientCertificates = extractClientCertificateChain(request);

        // Handle the token request.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new TokenRequestHandlerSpiImpl(), parameters, authorization, clientCertificates);
    }

}
