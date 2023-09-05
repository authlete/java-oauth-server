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


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialIssuerMetadataRequest;
import com.authlete.common.dto.CredentialIssuerMetadataResponse;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.CredentialSingleIssueRequest;
import com.authlete.common.dto.CredentialSingleIssueResponse;
import com.authlete.common.dto.CredentialSingleParseRequest;
import com.authlete.common.dto.CredentialSingleParseResponse;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.util.CredentialUtil;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;


@Path("/.well-known/openid-credential-issuer")
public class CredentialMetadataEndpoint extends AbstractCredentialEndpoint
{
    @GET
    public Response get()
    {
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        return metadata(api);
    }


    private Response metadata(final AuthleteApi api)
            throws WebApplicationException
    {
        final CredentialIssuerMetadataRequest request =
                new CredentialIssuerMetadataRequest()
                        .setPretty(false);

        final CredentialIssuerMetadataResponse response =
                api.credentialIssuerMetadata(request);
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
