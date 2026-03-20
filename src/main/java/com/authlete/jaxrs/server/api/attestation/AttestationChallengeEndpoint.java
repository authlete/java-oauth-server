/*
 * Copyright (C) 2026 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.attestation;


import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.AttestationChallengeRequest;
import com.authlete.jaxrs.BaseAttestationChallengeEndpoint;


/**
 * An implementation of the challenge endpoint defined in the <a href=
 * "https://datatracker.ietf.org/doc/draft-ietf-oauth-attestation-based-client-auth/"
 * >OAuth 2&#x2E;0 Attestation-Based Client Authentication</a>.
 *
 * @see <a href="https://datatracker.ietf.org/doc/draft-ietf-oauth-attestation-based-client-auth/">
 *      OAuth 2.0 Attestation-Based Client Authentication</a>
 */
@Path("/api/challenge")
public class AttestationChallengeEndpoint extends BaseAttestationChallengeEndpoint
{
    /**
     * The challenge endpoint.
     *
     * <p>
     * From <a href=
     * "https://datatracker.ietf.org/doc/draft-ietf-oauth-attestation-based-client-auth/"
     * >OAuth 2.0 Attestation-Based Client Authentication</a>:
     * </p>
     *
     * <blockquote>
     * <p>
     * A request for a Challenge is made by sending an HTTP POST request to the URL
     * provided in the {@code challenge_endpoint} of the Authorization Serve metadata.
     * </p>
     * </blockquote>
     *
     * @return
     *         A response from the challenge endpoint.
     */
    @POST
    public Response post()
    {
        // Authlete API interface
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Request to the Authlete's /api/{service-id}/attestation/challenge API
        AttestationChallengeRequest request =
                new AttestationChallengeRequest()
                        .setPretty(true);

        // Process the request.
        return handle(api, request);
    }
}
