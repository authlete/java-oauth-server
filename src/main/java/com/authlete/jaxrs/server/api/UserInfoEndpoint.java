/*
 * Copyright (C) 2016-2022 Authlete, Inc.
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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseUserInfoEndpoint;
import com.authlete.jaxrs.UserInfoRequestHandler.Params;
import com.authlete.jaxrs.util.JaxRsUtils;


/**
 * An implementation of userinfo endpoint (<a href=
 * "https://openid.net/specs/openid-connect-core-1_0.html#UserInfo"
 * >OpenID Connect Core 1&#x2E;0, 5&#x2E;3&#x2E; UserInfo Endpoint</a>).
 *
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#UserInfo"
 *      >OpenID Connect Core 10, 5.3. UserInfo Endpoint</a>
 */
@Path("/api/userinfo")
public class UserInfoEndpoint extends BaseUserInfoEndpoint
{
    /**
     * The userinfo endpoint for {@code GET} method.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#UserInfoRequest"
     *      >OpenID Connect Core 1.0, 5.3.1. UserInfo Request</a>
     */
    @GET
    public Response get(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam("DPoP") String dpop,
            @Context HttpServletRequest request)
    {
        // Select either the access token embedded in the Authorization header
        // or the access token in the query component.
        String accessToken = extractAccessToken(authorization, null);

        // Handle the userinfo request.
        return handle(request, accessToken, dpop);
    }


    /**
     * The userinfo endpoint for {@code POST} method.
     *
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#UserInfoRequest"
     *      >OpenID Connect Core 1.0, 5.3.1. UserInfo Request</a>
     */
    @POST
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam("DPoP") String dpop,
            @Context HttpServletRequest request, String body)
    {
        // '@Consumes(MediaType.APPLICATION_FORM_URLENCODED)' and
        // '@FormParam("access_token") are not used here because clients may send
        // a request without 'Content-Type' even if the HTTP method is 'POST'.
        //
        // See Issue 1137 in openid/connect for details.
        //
        //   Is content-type application/x-www-form-urlencoded required
        //   when calling user info endpoint with empty body?
        //
        //     https://bitbucket.org/openid/connect/issues/1137/is-content-type-application-x-www-form
        //

        // Extract "access_token" from the request body if the Content-Type of
        // the request is 'application/x-www-form-urlencoded'.
        String accessToken = extractFormParameter(request, body, "access_token");

        // Select either the access token embedded in the Authorization header
        // or the access token in the request body.
        accessToken = extractAccessToken(authorization, accessToken);

        // Handle the userinfo request.
        return handle(request, accessToken, dpop);
    }


    private static String extractFormParameter(HttpServletRequest request, String body, String key)
    {
        // If the request does not include 'Content-Type' or
        // its value is not 'application/x-www-form-urlencoded'.
        if (!MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType()))
        {
            return null;
        }

        // Get the value of "access_token" if available.
        return JaxRsUtils.parseFormUrlencoded(body).getFirst("access_token");
    }


    /**
     * Handle the userinfo request.
     */
    private Response handle(HttpServletRequest request, String accessToken, String dpop)
    {
        Params params = buildParams(request, accessToken, dpop);

        return handle(AuthleteApiFactory.getDefaultApi(),
                new UserInfoRequestHandlerSpiImpl(), params);
    }


    private Params buildParams(
            HttpServletRequest request, String accessToken, String dpop)
    {
        Params params = new Params();

        // Access Token
        params.setAccessToken(accessToken);

        // Client Certificate
        params.setClientCertificate(extractClientCertificate(request));

        // DPoP
        params.setDpop(dpop)
              .setHtm(request.getMethod())
              //.setHtu(request.getRequestURL().toString())
              ;

        // We can reconstruct the URL of the userinfo endpoint by calling
        // request.getRequestURL().toString() and set it to params by the
        // setHtu(String) method. However, the calculated URL may be invalid
        // behind proxies.
        //
        // If "htu" is not set here, the "userInfoEndpoint" property of "Service"
        // (which can be configured by using Authlete's Service Owner Console)
        // is referred to as the default value. Therefore, we don't call the
        // setHtu(String) method here intentionally. Note that this means you
        // have to set "userInfoEndpoint" properly to support DPoP.

        return params;
    }
}
