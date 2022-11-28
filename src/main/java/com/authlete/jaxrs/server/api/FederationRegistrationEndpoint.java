/*
 * Copyright (C) 2022 Authlete, Inc.
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


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.FederationRegistrationRequest;
import com.authlete.jaxrs.BaseFederationRegistrationEndpoint;


/**
 * An implementation of the federation registration endpoint.
 *
 * <p>
 * An OpenID Provider that supports the "explicit" client registration defined
 * in <a href="https://openid.net/specs/openid-connect-federation-1_0.html"
 * >OpenID Connect Federation 1.0</a> is supposed to provide a federation
 * registration endpoint that accepts explicit client registration requests.
 * </p>
 *
 * <p>
 * The endpoint accepts {@code POST} requests whose {@code Content-Type}
 * is either of the following.
 * </p>
 *
 * <ol>
 *   <li>{@code application/entity-statement+jwt}
 *   <li>{@code application/trust-chain+json}
 * </ol>
 *
 * <p>
 * When the {@code Content-Type} of a request is
 * {@code application/entity-statement+jwt}, the content of the request is
 * the entity configuration of a relying party that is to be registered.
 * </p>
 *
 * <p>
 * On the other hand, when the {@code Content-Type} of a request is
 * {@code application/trust-chain+json}, the content of the request is a
 * JSON array that contains entity statements in JWT format. The sequence
 * of the entity statements composes the trust chain of a relying party
 * that is to be registered.
 * </p>
 *
 * <p>
 * On successful registration, the endpoint should return a kind of entity
 * statement (JWT) with the HTTP status code {@code 200 OK} and the content
 * type {@code application/jose}.
 * </p>
 *
 * <p>
 * The discovery document (<a href=
 * "https://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect
 * Discovery 1.0</a>) should include the {@code federation_registration_endpoint}
 * server metadata that denotes the URL of the federation registration endpoint.
 * </p>
 *
 * <p>
 * Note that OpenID Connect Federation 1.0 is supported since Authlete 2.3.
 * </p>
 *
 * @see <a href="https://openid.net/specs/openid-connect-federation-1_0.html"
 *      >OpenID Connect Federation 1.0</a>
 */
@Path("/api/federation/register")
public class FederationRegistrationEndpoint extends BaseFederationRegistrationEndpoint
{
    @POST
    @Consumes("application/entity-statement+jwt")
    public Response entityConfiguration(String jwt)
    {
        // Client registration by a relying party's entity configuration.
        return handle(
                AuthleteApiFactory.getDefaultApi(),
                request().setEntityConfiguration(jwt));
    }


    @POST
    @Consumes("application/trust-chain+json")
    public Response trustChain(String json)
    {
        // Client registration by a relying party's trust chain.
        return handle(
                AuthleteApiFactory.getDefaultApi(),
                request().setTrustChain(json));
    }


    private static FederationRegistrationRequest request()
    {
        return new FederationRegistrationRequest();
    }
}
