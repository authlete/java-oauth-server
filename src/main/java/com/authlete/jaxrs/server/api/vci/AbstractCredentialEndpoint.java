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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.BaseResourceEndpoint;
import com.authlete.jaxrs.server.util.ExceptionUtil;


public abstract class AbstractCredentialEndpoint extends BaseResourceEndpoint
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
        // The value of the "Authorization" header.
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        return super.extractAccessToken(authorization, null);
    }


    protected IntrospectionResponse introspect(final AuthleteApi api,
                                final String accessToken)
            throws WebApplicationException
    {
        final IntrospectionRequest introspectionRequest = new IntrospectionRequest()
                .setToken(accessToken);

        final IntrospectionResponse response = api.introspection(introspectionRequest);
        final String content = response.getResponseContent();

        switch (response.getAction())
        {
            case BAD_REQUEST:
                throw ExceptionUtil.badRequestException(content);

            case UNAUTHORIZED:
                throw ExceptionUtil.unauthorizedException(accessToken, content);

            case FORBIDDEN:
                throw ExceptionUtil.forbiddenException(content);

            case OK:
                return response;

            case INTERNAL_SERVER_ERROR:
            default:
                throw ExceptionUtil.internalServerErrorException(content);
        }
    }
}
