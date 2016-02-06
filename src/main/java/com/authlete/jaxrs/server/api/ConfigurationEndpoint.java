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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseConfigurationEndpoint;


/**
 * An implementation of an OpenID Provider configuration endpoint.
 *
 * <p>
 * An OpenID Provider that supports <a href=
 * "http://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect
 * Discovery 1.0</a> must provide an endpoint that returns its configuration
 * information in a JSON format. Details about the format are described in
 * "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" in OpenID Connect Discovery 1.0.
 * </p>
 *
 * <p>
 * Note that the URI of an OpenID Provider configuration endpoint is defined in
 * "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationRequest"
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
 * in "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" (OpenID Connect Discovery 1.0) and <b>{@code iss}</b> in
 * "<a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">2. ID Token</a>"
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
 * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html"
 *      >OpenID Connect Discovery 1.0</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/.well-known/openid-configuration")
public class ConfigurationEndpoint extends BaseConfigurationEndpoint
{
    /**
     * OpenID Provider configuration endpoint.
     */
    @GET
    public Response get()
    {
        // Handle the configuration request.
        return handle(AuthleteApiFactory.getDefaultApi());
    }
}
