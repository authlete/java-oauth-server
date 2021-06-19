/*
 * Copyright (C) 2021 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.obb.util;


import static com.authlete.common.util.FapiUtils.X_FAPI_INTERACTION_ID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiException;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.dto.IntrospectionResponse.Action;
import com.authlete.common.util.FapiUtils;
import com.authlete.common.util.Utils;
import com.authlete.common.web.BearerToken;
import com.authlete.common.web.DpopToken;
import com.authlete.jaxrs.server.obb.model.ResponseError;
import com.authlete.jaxrs.util.CertificateUtils;


public class ObbUtils
{
    private static final SimpleDateFormat sDateFormat;


    static
    {
        sDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public static String formatDate(Date date)
    {
        return sDateFormat.format(date);
    }


    public static String formatNow()
    {
        return formatDate(new Date());
    }


    public static String computeOutgoingInteractionId(
            String code, String incomingInteractionId) throws WebApplicationException
    {
        try
        {
            // Compute the value for the 'x-fapi-interaction-id' HTTP response header.
            return FapiUtils.computeOutgoingInteractionId(incomingInteractionId);
        }
        catch (IllegalArgumentException e)
        {
            // The format of the incoming interaction ID is wrong.
        }

        throw badRequestException(null, code,
                "The format of the incoming 'x-fapi-interaction-id' is wrong.");
    }


    public static IntrospectionResponse validateAccessToken(
            String outgoingInteractionId, String code,
            AuthleteApi authleteApi, HttpServletRequest request,
            String... requiredScopes)
    {
        // Extract the access token from the Authorization header.
        String accessToken = extractAccessToken(request);

        // Extract the client certificate.
        String clientCertificate = CertificateUtils.extract(request);

        IntrospectionResponse response;

        try
        {
            // Call Authlete's /api/auth/introspection API.
            response = callIntrospection(
                    authleteApi, accessToken, requiredScopes, clientCertificate);
        }
        catch (AuthleteApiException e)
        {
            // Failed to call Authlete's /api/auth/interaction API.
            e.printStackTrace();

            throw internalServerErrorException(
                    outgoingInteractionId, code, e.getMessage());
        }

        // 'action' in the response denotes the next action which
        // this service implementation should take.
        Action action = response.getAction();

        // If the protected resource endpoint conforms to RFC 6750,
        // response.getResponseContent() can be used. However, the
        // protected resource endpoints of Open Banking Brasil behave
        // differently in error cases.
        String detail = response.getResultMessage();

        // Dispatch according to the action.
        switch (action)
        {
            case INTERNAL_SERVER_ERROR:
                // 500 Internal Server Error
                throw internalServerErrorException(
                        outgoingInteractionId, code, detail);

            case BAD_REQUEST:
                // 400 Bad Request
                throw badRequestException(
                        outgoingInteractionId, code, detail);

            case UNAUTHORIZED:
                // 401 Unauthorized
                throw unauthorizedException(
                        outgoingInteractionId, code, detail);

            case FORBIDDEN:
                // 403 Forbidden
                throw forbiddenException(
                        outgoingInteractionId, code, detail);

            case OK:
                // Return access token information.
                return response;

            default:
                // Unknown action. This never happens.
                throw unknownAction(
                        outgoingInteractionId, code, action);
        }
    }


    private static String extractAccessToken(HttpServletRequest request)
    {
        // The value of the "Authorization" header.
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Extract a DPoP access token from the value of Authorization header.
        String accessToken = DpopToken.parse(authorization);

        if (accessToken == null)
        {
            // if a DPoP token wasn't found, look for a Bearer in the authorization header
            accessToken = BearerToken.parse(authorization);
        }

        return accessToken;
    }


    private static IntrospectionResponse callIntrospection(
            AuthleteApi authleteApi, String accessToken,
            String[] requiredScopes, String clientCertificate) throws AuthleteApiException
    {
        // Create a request to Authlete's /api/auth/introspection API.
        IntrospectionRequest request = new IntrospectionRequest()
                .setToken(accessToken)
                .setScopes(requiredScopes)
                .setClientCertificate(clientCertificate)
                ;

        // Call Authlete's /api/auth/introspection API.
        return authleteApi.introspection(request);
    }


    public static Response generateResponse(
            Status status, String outgoingInteractionId, Object entity)
    {
        ResponseBuilder builder = Response.status(status);

        if (outgoingInteractionId != null)
        {
            builder.header(X_FAPI_INTERACTION_ID, outgoingInteractionId);
        }

        if (entity != null)
        {
            builder.type(MediaType.APPLICATION_JSON_TYPE);
            builder.entity(entity instanceof String ? entity : Utils.toJson(entity, true));
        }

        return builder.build();
    }


    public static Response ok(String outgoingInteractionId, Object entity)
    {
        return generateResponse(Status.OK, outgoingInteractionId, entity);
    }


    public static Response created(String outgoingInteractionId, Object entity)
    {
        return generateResponse(Status.CREATED, outgoingInteractionId, entity);
    }


    public static Response noContent(String outgoingInteractionId)
    {
        return generateResponse(Status.NO_CONTENT, outgoingInteractionId, null);
    }


    public static Response generateErrorResponse(
            Status status, String outgoingInteractionId,
            String code, String title, String detail)
    {
        ResponseError entity = ResponseError.create(code, title, detail);

        return generateResponse(status, outgoingInteractionId, entity);
    }


    public static Response badRequest(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.BAD_REQUEST, outgoingInteractionId,
                code, "Bad Request", detail);
    }


    public static WebApplicationException badRequestException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                badRequest(outgoingInteractionId, code, detail));
    }


    public static Response unauthorized(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.UNAUTHORIZED, outgoingInteractionId,
                code, "Unauthorized", detail);
    }


    public static WebApplicationException unauthorizedException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                unauthorized(outgoingInteractionId, code, detail));
    }


    public static Response forbidden(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.FORBIDDEN, outgoingInteractionId,
                code, "Forbidden", detail);
    }


    public static WebApplicationException forbiddenException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                forbidden(outgoingInteractionId, code, detail));
    }


    public static Response notFound(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.NOT_FOUND, outgoingInteractionId,
                code, "Not Found", detail);
    }


    public static WebApplicationException notFoundException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                notFound(outgoingInteractionId, code, detail));
    }


    public static Response internalServerError(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.INTERNAL_SERVER_ERROR, outgoingInteractionId,
                code, "Internal Server Error", detail);
    }


    public static WebApplicationException internalServerErrorException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                internalServerError(outgoingInteractionId, code, detail));
    }


    private static WebApplicationException unknownAction(
            String outgoingInteractionId, String code,
            IntrospectionResponse.Action action)
    {
        String detail = String.format(
                "Unknow action '%s' from Authlete's introspection API.", action);

        return internalServerErrorException(
                outgoingInteractionId, code, detail);
    }
}
