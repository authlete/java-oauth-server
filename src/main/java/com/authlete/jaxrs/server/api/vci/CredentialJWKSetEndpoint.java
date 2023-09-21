/*
 * Copyright (C) 2023 Authlete, Inc.
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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialIssuerJwksRequest;
import com.authlete.common.dto.CredentialIssuerJwksResponse;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;


@Path("/api/vci/jwks")
public class CredentialJWKSetEndpoint extends AbstractCredentialEndpoint
{
    @GET
    public Response get()
    {
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        return process(api);
    }


    private Response process(final AuthleteApi api)
            throws WebApplicationException
    {
        final CredentialIssuerJwksRequest request =
                new CredentialIssuerJwksRequest()
                        .setPretty(false);

        final CredentialIssuerJwksResponse response =
                api.credentialIssuerJwks(request);
        final String content = response.getResponseContent();

        switch (response.getAction())
        {
            case NOT_FOUND:
                return ResponseUtil.notFoundJson(content);

            case OK:
                return ResponseUtil.okJson(response.getResponseContent());

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorExceptionJson(content);
        }
    }
}
