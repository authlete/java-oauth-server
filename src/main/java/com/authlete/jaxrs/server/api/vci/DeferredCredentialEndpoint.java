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
import com.authlete.common.dto.CredentialDeferredIssueRequest;
import com.authlete.common.dto.CredentialDeferredIssueResponse;
import com.authlete.common.dto.CredentialDeferredParseRequest;
import com.authlete.common.dto.CredentialDeferredParseResponse;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.ErrorCode;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;
import com.authlete.jaxrs.server.vc.OrderContext;


@Path("/api/deferred_credential")
public class DeferredCredentialEndpoint extends AbstractCredentialEndpoint
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
        String htu = computeHtu(api, dpop, "deferred_credential_endpoint");

        // Validate the access token
        IntrospectionResponse introspection =
                introspect(request, api, accessToken, dpop, htu);

        // The headers that the response from this endpoint should include.
        Map<String, Object> headers = prepareHeaders(introspection);

        // Parse the deferred credential request.
        CredentialRequestInfo info = parseRequest(
                api, requestContent, accessToken, headers);

        // Prepare a credential issuance order.
        CredentialIssuanceOrder order =
                prepareOrder(OrderContext.DEFERRED, introspection, info, headers);

        // If the requested credential is not ready yet.
        if (order.isIssuanceDeferred())
        {
            // 400 Bad Request + "error":"issuance_pending"
            throw ExceptionUtil.badRequestExceptionJson(
                    errorJson(ErrorCode.issuance_pending, null), headers);
        }

        // Issue a credential and return a deferred credential response.
        return issue(api, order, headers);
    }


    private CredentialRequestInfo parseRequest(
            AuthleteApi api, String requestContent, String accessToken,
            Map<String, Object> headers) throws WebApplicationException
    {
        // Prepare a request to the /vci/deferred/parse API.
        CredentialDeferredParseRequest request =
                new CredentialDeferredParseRequest()
                        .setRequestContent(requestContent)
                        .setAccessToken(accessToken);

        // Call the /vci/deferred/parse API and get the response.
        CredentialDeferredParseResponse response = api.credentialDeferredParse(request);

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
            AuthleteApi api, CredentialIssuanceOrder order,
            Map<String, Object> headers) throws WebApplicationException
    {
        // Prepare a request to the /vci/deferred/issue API.
        CredentialDeferredIssueRequest request =
                new CredentialDeferredIssueRequest()
                    .setOrder(order);

        // Call the /vci/deferred/issue API and get the response.
        CredentialDeferredIssueResponse response = api.credentialDeferredIssue(request);

        // The response content.
        String content = response.getResponseContent();

        switch (response.getAction())
        {
            case CALLER_ERROR:
                return ResponseUtil.internalServerErrorJson(content, headers);

            case BAD_REQUEST:
                return ResponseUtil.badRequestJson(content, headers);

            case FORBIDDEN:
                return ResponseUtil.forbiddenJson(content, headers);

            case OK:
                return ResponseUtil.okJson(content, headers);

            case OK_JWT:
                return ResponseUtil.okJwt(content, headers);

            case INTERNAL_SERVER_ERROR:
            default:
                return ResponseUtil.internalServerErrorJson(content, headers);
        }
    }
}
