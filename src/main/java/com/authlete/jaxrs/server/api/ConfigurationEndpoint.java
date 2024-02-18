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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.ServiceConfigurationRequest;
import com.authlete.jaxrs.BaseConfigurationEndpoint;


/**
 * An implementation of an OpenID Provider configuration endpoint.
 *
 * <p>
 * An OpenID Provider that supports <a href=
 * "https://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect
 * Discovery 1.0</a> must provide an endpoint that returns its configuration
 * information in a JSON format. Details about the format are described in
 * "<a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" in OpenID Connect Discovery 1.0.
 * </p>
 *
 * <p>
 * Note that the URI of an OpenID Provider configuration endpoint is defined in
 * "<a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationRequest"
 * >4.1. OpenID Provider Configuration Request</a>" in OpenID Connect Discovery
 * 1.0. In short, the URI must be:
 * </p>
 *
 * <blockquote>
 * Issuer Identifier + {@code /.well-known/openid-configuration}
 * </blockquote>
 *
 * <p>
 * <i>Issuer Identifier</i> is a URL to identify an OpenID Provider. For example,
 * {@code https://example.com}. For details about Issuer Identifier, See <b>{@code issuer}</b>
 * in "<a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" (OpenID Connect Discovery 1.0) and <b>{@code iss}</b> in
 * "<a href="https://openid.net/specs/openid-connect-core-1_0.html#IDToken">2. ID Token</a>"
 * (OpenID Connect Core 1.0).
 * </p>
 *
 * <p>
 * You can change the Issuer Identifier of your service using the management console
 * (<a href="https://www.authlete.com/documents/so_console">Service Owner Console</a>).
 * Note that the default value of Issuer Identifier is not appropriate for commercial
 * use, so you should change it.
 * </p>
 *
 * @see <a href="https://openid.net/specs/openid-connect-discovery-1_0.html"
 *      >OpenID Connect Discovery 1.0</a>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414.html"
 *      >RFC 8414 OAuth 2.0 Authorization Server Metadata</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/.well-known/{path : openid-configuration|oauth-authorization-server}")
public class ConfigurationEndpoint extends BaseConfigurationEndpoint
{
    /**
     * OpenID Provider configuration endpoint.
     *
     * <p>
     * This implementation accepts {@code "pretty"} and {@code "patch"} as
     * request parameters, but they are not standardized ones. They are
     * processed just to demonstrate capabilities of Authlete's
     * {@code /service/configuration} API. Note that the version of Authlete
     * must be 2.2.36 or greater to use the request parameters.
     * </p>
     *
     * <p>
     * The value of the {@code patch} request parameter is a <b>JSON Patch</b>
     * that conforms to <a href="https://www.rfc-editor.org/rfc/rfc6902">RFC
     * 6902 JavaScript Object Notation (JSON) Patch</a>. API callers can make
     * the Authlete API modify JSON on Authlete side before it returns the
     * configuration JSON. Of course, API callers can modify JSON as they like
     * AFTER they receive a response from the Authlete API, so API callers do
     * not necessarily need to use the {@code patch} request parameter.
     * </p>
     */
    @GET
    public Response get(
            @QueryParam("pretty") String pretty,
            @QueryParam("patch") String patch
            )
    {
        // An AuthleteApi instance to access Authlete APIs.
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // If either or both of the 'pretty' request parameter
        // and the 'patch' request parameter are given.
        if ((pretty != null && !pretty.isEmpty()) ||
            (patch  != null && !patch .isEmpty()) )
        {
            // Call the /service/configuration API with HTTP POST,
            // which is supported since Authlete 2.2.36.
            return handle(api, createRequest(pretty, patch));
        }

        // Call the /service/configuration API with HTTP GET.
        return handle(api);
    }


    private static ServiceConfigurationRequest createRequest(String pretty, String patch)
    {
        return new ServiceConfigurationRequest()
                .setPretty(determinePretty(pretty))
                .setPatch(patch);
    }


    private static boolean determinePretty(String pretty)
    {
        // If the 'pretty' request parameter is not given.
        if (pretty == null || pretty.isEmpty())
        {
            // The default value of 'pretty' is true.
            return true;
        }

        return Boolean.parseBoolean(pretty);
    }
}
