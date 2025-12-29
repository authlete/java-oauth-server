/*
 * Copyright (C) 2025 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.vci;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialNonceRequest;
import com.authlete.jaxrs.BaseCredentialNonceEndpoint;


/**
 * An implementation of the nonce endpoint defined in the <a href=
 * "https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html"
 * >OpenID for Verifiable Credential Issuance 1&#x2E;0</a> specification.
 *
 * @see <a href="https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html">
 *      OpenID for Verifiable Credential Issuance 1.0</a>
 */
@Path("/api/nonce")
public class CredentialNonceEndpoint extends BaseCredentialNonceEndpoint
{
    /**
     * The nonce endpoint.
     *
     * <p>
     * From <a href=
     * "https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-7.1"
     * >Section 7.1. Nonce Request</a> of <a href=
     * "https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html"
     * >OpenID for Verifiable Credential Issuance 1.0</a>:
     * </p>
     *
     * <blockquote>
     * <p>
     * A request for a nonce is made by sending an HTTP POST request to the URL
     * provided in the {@code nonce_endpoint} Credential Issuer Metadata parameter.
     * The Nonce Endpoint is not a protected resource, meaning the Wallet does
     * not need to supply an access token to access it.
     * </p>
     * </blockquote>
     *
     * @return
     *         A response from the nonce endpoint.
     */
    @POST
    public Response post()
    {
        // Authlete API interface
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Request to the Authlete's /api/{service-id}/vci/nonce API
        CredentialNonceRequest request =
                new CredentialNonceRequest()
                        .setPretty(true);

        // Process the request.
        return handle(api, request);
    }
}
