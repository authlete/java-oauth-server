/*
 * Copyright (C) 2016-2024 Authlete, Inc.
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


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.BaseTokenEndpoint;
import com.authlete.jaxrs.TokenRequestHandler.Params;


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
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Authlete API
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();

        // Process the token request in a standard way.
        Response response = processTokenRequest(authleteApi, request, parameters);

        // Do additional tasks as necessary.
        doTasks(authleteApi, request, parameters, response);

        return response;
    }


    private Response processTokenRequest(
            AuthleteApi authleteApi, HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Parameters for Authlete's /api/auth/token API.
        Params params = buildParams(request, parameters);

        // Handle the token request.
        return handle(authleteApi, new TokenRequestHandlerSpiImpl(authleteApi), params);
    }


    private Params buildParams(
            HttpServletRequest request, MultivaluedMap<String, String> parameters)
    {
        Params params = new Params();

        // RFC 6749
        // The OAuth 2.0 Authorization Framework
        params.setParameters(parameters)
              .setAuthorization(request.getHeader(HttpHeaders.AUTHORIZATION))
              ;

        // MTLS
        // RFC 8705 : OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens
        params.setClientCertificatePath(extractClientCertificateChain(request));

        // DPoP
        // OAuth 2.0 Demonstration of Proof-of-Possession at the Application Layer (DPoP)
        params.setDpop(request.getHeader("DPoP"))
              .setHtm("POST")
              //.setHtu(request.getRequestURL().toString())
              ;

        // We can reconstruct the URL of the token endpoint by calling
        // request.getRequestURL().toString() and set it to params by the
        // setHtu(String) method. However, the calculated URL may be invalid
        // behind proxies.
        //
        // If "htu" is not set here, the "tokenEndpoint" property of "Service"
        // (which can be configured by using Authlete's Service Owner Console)
        // is referred to as the default value. Therefore, we don't call the
        // setHtu(String) method here intentionally. Note that this means you
        // have to set "tokenEndpoint" properly to support DPoP.

        // Even the call of the setHtm(String) method can be omitted, too.
        // When "htm" is not set, "POST" is used as the default value.

        // OAuth 2.0 Attestation-Based Client Authentication
        params.setClientAttestation(   request.getHeader("OAuth-Client-Attestation"))
              .setClientAttestationPop(request.getHeader("OAuth-Client-Attestation-PoP"))
              ;

        return params;
    }


    @SuppressWarnings("unchecked")
    private void doTasks(
            AuthleteApi authleteApi, HttpServletRequest request,
            MultivaluedMap<String, String> requestParams, Response response)
    {
        // The entity conforms to the token response defined in RFC 6749.
        Map<String, Object> responseParams =
                Utils.fromJson((String)response.getEntity(), Map.class);

        // A task specific to Open Banking Brasil.
        new OBBTokenTask().process(
                authleteApi, request, requestParams, response, responseParams);
    }
}
