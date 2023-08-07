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
import com.authlete.common.dto.CredentialBatchIssueRequest;
import com.authlete.common.dto.CredentialBatchIssueResponse;
import com.authlete.common.dto.CredentialBatchParseRequest;
import com.authlete.common.dto.CredentialBatchParseResponse;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.BaseEndpoint;
import com.authlete.jaxrs.server.util.CredentialUtil;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ResponseUtil;


public abstract class AbstractCredentialEndpoint extends BaseEndpoint
{
    protected String checkContentExtractToken(final HttpServletRequest request,
                                 final String requestContent)
    {
        if (requestContent == null)
        {
            throw ExceptionUtil.badRequestException("Missing request content.");
        }

        final String accessToken = processAccessToken(request);
        if (accessToken == null)
        {
            throw ExceptionUtil.badRequestException("Missing access token.");
        }

        return accessToken;
    }


    private String processAccessToken(final HttpServletRequest request)
    {
        String accessToken = request.getHeader("Authorization");
        if (accessToken == null || !accessToken.startsWith("Bearer "))
        {
            return null;
        }
        accessToken = accessToken.replaceFirst("Bearer ", "");
        return accessToken;
    }


    protected String introspect(final AuthleteApi api,
                                final String accessToken)
            throws WebApplicationException
    {
        final IntrospectionRequest introspectionRequest = new IntrospectionRequest()
                .setToken(accessToken);

        final IntrospectionResponse response = api.introspection(introspectionRequest);
        String resultMessage = response.getResultMessage();

        switch (response.getAction())
        {
            case BAD_REQUEST:
                throw ExceptionUtil.badRequestException(resultMessage);

            case UNAUTHORIZED:
                throw ExceptionUtil.unauthorizedException(accessToken, resultMessage);

            case FORBIDDEN:
                throw ExceptionUtil.forbiddenException(resultMessage);

            case OK:
                return resultMessage;

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorException(resultMessage);
        }
    }
}
