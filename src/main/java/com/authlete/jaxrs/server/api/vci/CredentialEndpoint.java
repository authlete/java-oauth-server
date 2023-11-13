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


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;
import com.authlete.jaxrs.server.vc.OrderContext;


@Path("/api/credential")
public class CredentialEndpoint extends AbstractCredentialEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(
            @Context HttpServletRequest request,
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam("DPoP") String dpop,
            String requestContent)
    {
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Extract the access token from the request.
        String accessToken = extractAccessToken(authorization, null);

        // The expected value of the 'htu' claim in the DPoP proof JWT.
        String htu = computeHtu(api, dpop, "credential_endpoint");

        // Validate the access token.
        IntrospectionResponse introspection =
                introspect(request, api, accessToken, dpop, htu);

        // The headers that the response from this endpoint should include.
        Map<String, Object> headers = prepareHeaders(introspection);

        // Parse the credential request.
        CredentialRequestInfo info = parseRequest(
                api, requestContent, accessToken, headers);

        // Prepare a credential issuance order.
        CredentialIssuanceOrder order =
                prepareOrder(OrderContext.SINGLE, introspection, info, headers);

        // Issue a credential and return a credential response.
        return issue(api, order, accessToken, headers);
    }


    private CredentialRequestInfo parseRequest(
            AuthleteApi api, String requestContent, String accessToken,
            Map<String, Object> headers) throws WebApplicationException
    {
        // Prepare a request to the /vci/single/parse API.
        CredentialSingleParseRequest request =
                new CredentialSingleParseRequest()
                    .setRequestContent(requestContent)
                    .setAccessToken(accessToken);

        // Call the /vci/single/parse API and get the response.
        CredentialSingleParseResponse response = api.credentialSingleParse(request);

        // The response content.
        String content = response.getResponseContent();

        switch (response.getAction())
        {
            case BAD_REQUEST:
                throw ExceptionUtil.badRequestExceptionJson(content, headers);

            case UNAUTHORIZED:
                throw ExceptionUtil.unauthorizedException(accessToken, content, headers);

            case FORBIDDEN:
                throw ExceptionUtil.forbiddenExceptionJson(content, headers);

            case OK:
                return response.getInfo();

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorExceptionJson(content, headers);
        }
    }


    private Response issue(
            AuthleteApi api, CredentialIssuanceOrder order, String accessToken,
            Map<String, Object> headers) throws WebApplicationException
    {
        // Prepare a request to the /vci/single/issue API.
        CredentialSingleIssueRequest request =
                new CredentialSingleIssueRequest()
                        .setAccessToken(accessToken)
                        .setOrder(order);

        // Call the /vci/single/issue API and get the response.
        CredentialSingleIssueResponse response = api.credentialSingleIssue(request);

        // The response content.
        String content = response.getResponseContent();

        switch (response.getAction())
        {
            case CALLER_ERROR:
                return ResponseUtil.internalServerErrorJson(content, headers);

            case BAD_REQUEST:
                return ResponseUtil.badRequestJson(content, headers);

            case UNAUTHORIZED:
                return ResponseUtil.unauthorized(accessToken, content, headers);

            case FORBIDDEN:
                return ResponseUtil.forbiddenJson(content, headers);

            case OK:
                return ResponseUtil.okJson(content, headers);

            case OK_JWT:
                return ResponseUtil.okJwt(content, headers);

            case ACCEPTED:
                return ResponseUtil.acceptedJson(content, headers);

            case ACCEPTED_JWT:
                return ResponseUtil.acceptedJwt(content, headers);

            case INTERNAL_SERVER_ERROR:
            default:
                return ResponseUtil.internalServerErrorJson(content, headers);
        }
    }
}
