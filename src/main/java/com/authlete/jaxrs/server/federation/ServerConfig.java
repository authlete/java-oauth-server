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
 * Server configuration for ID federation.
 *
 * <pre>
 * {
 *     "name": "(display name of the OpenID Provider)",
 *     "issuer": "(issuer identifier of the OpenID Provider)"
 * }
 * </pre>
 *
 * <p>
 * The value of {@code "issuer"} must match the value of {@code "issuer"}
 * in the discovery document of the OpenID Provider. The OpenID Provider
 * must expose its discovery document at
 * <code><i>{issuer}</i>/.well-known/openid-configuration</code>.
 * </p>
 *
 * @see FederationConfig
 */
public class ServerConfig implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String name;
    private String issuer;


    public String getName()
    {
        return name;
    }


    public ServerConfig setName(String name)
    {
        this.name = name;

        return this;
    }


    public String getIssuer()
    {
        return issuer;
    }


    public ServerConfig setIssuer(String issuer)
    {
        this.issuer = issuer;

        return this;
    }


    public void validate() throws IllegalStateException
    {
        ensureNotEmpty("server/name", name);
        ensureNotEmpty("server/issuer", issuer);
        ensureUri("server/issuer", issuer);
    }
}
