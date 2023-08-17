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
package com.authlete.jaxrs.server.api.credential;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.CredentialSingleIssueRequest;
import com.authlete.common.dto.CredentialSingleIssueResponse;
import com.authlete.common.dto.CredentialSingleParseRequest;
import com.authlete.common.dto.CredentialSingleParseResponse;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.util.CredentialUtil;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;


@Path("/api/credential")
public class CredentialEndpoint extends AbstractCredentialEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(@Context HttpServletRequest request,
                         final String requestContent)
    {
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Check request content
        final String accessToken = super.checkContentExtractToken(request, requestContent);

        // Validate access token
        final IntrospectionResponse introspection = introspect(api, accessToken);

        // Parse credential and make it an order
        final CredentialRequestInfo credential = credentialSingleParse(api,
                                                                       requestContent,
                                                                       accessToken);
        final CredentialIssuanceOrder order = CredentialUtil.toOrder(introspection,
                                                                     credential);

        // Issue
        final String issuance = credentialIssue(api, order, accessToken);
        return ResponseUtil.ok(issuance);
    }


    private CredentialRequestInfo credentialSingleParse(final AuthleteApi api,
                                     final String requestContent,
                                     final String accessToken)
            throws WebApplicationException
    {
        final CredentialSingleParseRequest parseRequest = new CredentialSingleParseRequest()
                .setRequestContent(requestContent)
                .setAccessToken(accessToken);

        final CredentialSingleParseResponse response = api.credentialSingleParse(parseRequest);
        final String resultMessage = response.getResultMessage();

        switch (response.getAction())
        {
            case BAD_REQUEST:
                throw ExceptionUtil.badRequestException(resultMessage);

            case UNAUTHORIZED:
                throw ExceptionUtil.unauthorizedException(accessToken, resultMessage);

            case FORBIDDEN:
                throw ExceptionUtil.forbiddenException(resultMessage);

            case OK:
                return response.getInfo();

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorException(resultMessage);
        }
    }


    private String credentialIssue(final AuthleteApi api,
                            final CredentialIssuanceOrder order,
                            final String accessToken)
    {
        final CredentialSingleIssueRequest credentialSingleIssueRequest = new CredentialSingleIssueRequest()
                .setAccessToken(accessToken)
                .setOrder(order);

        final CredentialSingleIssueResponse response = api.credentialSingleIssue(credentialSingleIssueRequest);
        final String resultMessage = response.getResultMessage();

        switch (response.getAction())
        {
            case CALLER_ERROR:
                throw ExceptionUtil.badRequestException(resultMessage);

            case UNAUTHORIZED:
                throw ExceptionUtil.unauthorizedException(accessToken, resultMessage);

            case FORBIDDEN:
                throw ExceptionUtil.forbiddenException(resultMessage);

            case OK:
            case ACCEPTED:
                return resultMessage;

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorException(resultMessage);
        }
    }
}
