/*
 * Copyright (C) 2022 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.federation;


import static com.authlete.jaxrs.server.federation.ConfigValidationHelper.ensureNotEmpty;
import static com.authlete.jaxrs.server.federation.ConfigValidationHelper.ensureUri;
import java.io.Serializable;


/**
 * Client configuration for ID federation.
 *
 * <pre>
 * {
 *     "clientId": "(client ID issued by the OpenID Provider)",
 *     "clientSecret": "(client secret issued by the OpenID Provider)",
 *     "redirectUri": "(redirect URI registered to the OpenID Provider)",
 *     "idTokenSignedResponseAlg": "(algorithm of ID Token signature)"
 * }
 * </pre>
 *
 * <p>
 * {@code "clientId"} is the client ID issued to your client application by
 * the OpenID Provider.
 * </p>
 *
 * <p>
 * If {@code "clientSecret"} is set, token requests made by {@link Federation}
 * will include an {@code Authorization} header for client authentication.
 * This behavior assumes that the token endpoint of the OpenID Provider
 * supports {@code client_secret_basic} as a method of client authentication.
 * </p>
 *
 * <p>
 * {@code "redirectUri"} must be a redirect URI that you have registered into
 * the OpenID Provider. For example,
 * <code>http://localhost:8080/api/federation/callback/okta</code>.
 * </p>
 *
 * <p>
 * If {@code "idTokenSignedResponseAlg"} is omitted, {@code "RS256"} is used
 * as the default value. See technical documents of the OpenID Provider
 * about the actual algorithm it uses for signing ID tokens.
 * </p>
 *
 * @see FederationConfig
 */
public class ClientConfig implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String idTokenSignedResponseAlg;


    public String getClientId()
    {
        return clientId;
    }


    public ClientConfig setClientId(String clientId)
    {
        this.clientId = clientId;

        return this;
    }


    public String getClientSecret()
    {
        return clientSecret;
    }


    public ClientConfig setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;

        return this;
    }


    public String getRedirectUri()
    {
        return redirectUri;
    }


    public ClientConfig setRedirectUri(String redirectUri)
    {
        this.redirectUri = redirectUri;

        return this;
    }


    public String getIdTokenSignedResponseAlg()
    {
        return idTokenSignedResponseAlg;
    }


    public ClientConfig setIdTokenSignedResponseAlg(String idTokenSignedResponseAlg)
    {
        this.idTokenSignedResponseAlg = idTokenSignedResponseAlg;

        return this;
    }


    public void validate() throws IllegalStateException
    {
        ensureNotEmpty("client/clientId", clientId);
        ensureNotEmpty("client/redirectUri", redirectUri);
        ensureUri("client/redirectUri", redirectUri);
    }
}
