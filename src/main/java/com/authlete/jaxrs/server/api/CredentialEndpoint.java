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
package com.authlete.jaxrs.server.api;


import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialOfferInfoRequest;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.CredentialSingleIssueRequest;
import com.authlete.common.dto.CredentialSingleIssueResponse;
import com.authlete.common.dto.CredentialSingleParseRequest;
import com.authlete.common.dto.CredentialSingleParseResponse;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.BaseEndpoint;
import com.authlete.jaxrs.server.util.CredentialUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minidev.json.JSONObject;


@Path("/api/credential")
public class CredentialEndpoint extends BaseEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(@Context HttpServletRequest request,
                         final String requestContent)
    {
        if (requestContent == null)
        {
            return ResponseUtil.badRequest("Missing request content.");
        }

        String accessToken = request.getHeader("Authorization");
        if (accessToken == null || !accessToken.startsWith("Bearer "))
        {
            return ResponseUtil.badRequest("Missing access token.");
        }
        accessToken = accessToken.replaceFirst("Bearer ", "");

        // 2.
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();
        final IntrospectionRequest introspectionRequest = new IntrospectionRequest()
                .setToken(accessToken);

        final IntrospectionResponse response = api.introspection(introspectionRequest);
        String resultMessage = response.getResultMessage();

        switch (response.getAction())
        {
            case INTERNAL_SERVER_ERROR:
                return ResponseUtil.internalServerError(resultMessage);
            case BAD_REQUEST:
                return ResponseUtil.badRequest(resultMessage);
            case UNAUTHORIZED:
                return ResponseUtil.unauthorized(accessToken, resultMessage);
            case FORBIDDEN:
                //TODO
                return ResponseUtil.internalServerError(resultMessage);
        }

        // 3.
        final CredentialSingleParseRequest parseRequest = new CredentialSingleParseRequest()
                .setRequestContent(requestContent)
                .setAccessToken(accessToken);

        final CredentialSingleParseResponse credentialSingleParseResponse = api.credentialSingleParse(parseRequest);
        resultMessage = credentialSingleParseResponse.getResultMessage();

        switch (credentialSingleParseResponse.getAction())
        {
            case INTERNAL_SERVER_ERROR:
                return ResponseUtil.internalServerError(resultMessage);
            case BAD_REQUEST:
                return ResponseUtil.badRequest(resultMessage);
            case UNAUTHORIZED:
                return ResponseUtil.unauthorized(accessToken, resultMessage);
            case FORBIDDEN:
                //TODO
                return ResponseUtil.internalServerError(resultMessage);
        }

        // 4.
        final CredentialIssuanceOrder credentialIssuanceOrder = CredentialUtil.toOrder(
                credentialSingleParseResponse.getInfo());

        // 5.
        final CredentialSingleIssueRequest credentialSingleIssueRequest = new CredentialSingleIssueRequest()
                .setAccessToken(accessToken)
                .setOrder(credentialIssuanceOrder);

        final CredentialSingleIssueResponse credentialSingleIssueResponse = api.credentialSingleIssue(credentialSingleIssueRequest);
        resultMessage = credentialSingleIssueResponse.getResultMessage();

        switch (credentialSingleIssueResponse.getAction())
        {
            case INTERNAL_SERVER_ERROR:
                return ResponseUtil.internalServerError(resultMessage);
            case CALLER_ERROR:
                return ResponseUtil.badRequest(resultMessage);
            case UNAUTHORIZED:
                return ResponseUtil.unauthorized(accessToken, resultMessage);
            case FORBIDDEN:
                //TODO
                return ResponseUtil.internalServerError(resultMessage);
        }

        return ResponseUtil.ok(credentialSingleIssueResponse.getResponseContent());
    }
}
