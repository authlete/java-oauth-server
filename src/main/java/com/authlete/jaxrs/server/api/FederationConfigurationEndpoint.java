/*
 * Copyright (C) 2022-2023 Authlete, Inc.
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
import com.authlete.common.dto.FederationConfigurationRequest;
import com.authlete.common.types.EntityType;
import com.authlete.jaxrs.BaseFederationConfigurationEndpoint;


/**
 * An implementation of the entity configuration endpoint.
 *
 * <p>
 * An OpenID Provider that supports <a href=
 * "https://openid.net/specs/openid-federation-1_0.html">OpenID
 * Federation 1.0</a> must provide an endpoint that returns its <b>entity
 * configuration</b> in the JWT format. The URI of the endpoint is defined
 * as follows:
 * </p>
 *
 * <ol>
 * <li>Entity ID + {@code /.well-known/openid-federation}
 * <li>Host component of Entity ID + {@code /.well-known/openid-federation}
 *     + Path component of Entity ID (The same rule in <a href=
 *     "https://www.rfc-editor.org/rfc/rfc8414.html">RFC 8414</a>)
 * </ol>
 *
 * <p>
 * <b>Entity ID</b> is a URL that identifies an OpenID Provider (and other
 * entities including Relying Parties, Trust Anchors and Intermediate
 * Authorities) in the context of OpenID Federation 1.0.
 * </p>
 *
 * <p>
 * Note that OpenID Federation 1.0 is supported since Authlete 2.3.
 * </p>
 *
 * @see <a href="https://openid.net/specs/openid-federation-1_0.html"
 *      >OpenID Federation 1.0</a>
 */
@Path("/.well-known/openid-federation")
public class FederationConfigurationEndpoint extends BaseFederationConfigurationEndpoint
{
    /**
     * The request to Authlete's /federation/configuration API.
     */
    private static final FederationConfigurationRequest REQUEST =
            new FederationConfigurationRequest()
                .setEntityTypes(new EntityType[] {
                        EntityType.OPENID_PROVIDER,
                        EntityType.OPENID_CREDENTIAL_ISSUER
                });


    /**
     * Entity configuration endpoint.
     */
    @GET
    public Response get()
    {
        // Handle the request to the endpoint.
        return handle(AuthleteApiFactory.getDefaultApi(), REQUEST);
    }
}
