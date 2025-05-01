/*
 * Copyright (C) 2022-2025 Authlete, Inc.
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


import java.net.URI;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.dto.TokenCreateRequest;
import com.authlete.common.dto.TokenCreateResponse;
import com.authlete.common.dto.TokenInfo;
import com.authlete.common.dto.TokenResponse;
import com.authlete.common.types.GrantType;
import com.authlete.common.types.TokenType;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;


/**
 * A sample implementation of processing a token exchange request (<a href=
 * "https://www.rfc-editor.org/rfc/rfc8693.html">RFC 8693 OAuth 2&#x002E;0
 * Token Exchange</a>).
 *
 * <p>
 * RFC 8693 is very flexible. In other words, the specification does not define
 * details that are necessary for secure token exchange. Therefore,
 * implementations have to complement the specification with their own rules.
 * </p>
 *
 * <p>
 * There are various patterns for such deployment-specific rules. The
 * implementation in this file is just an example and does not intend to be
 * perfect for commercial use.
 * </p>
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8693.html"
 *      >RFC 8693 OAuth 2.0 Token Exchange</a>
 */
class TokenExchanger
{
    private final AuthleteApi mAuthleteApi;
    private final HttpServletRequest mRequest;
    private final TokenResponse mTokenResponse;
    private final Map<String, Object> mHeaders;


    public TokenExchanger(
            AuthleteApi authleteApi, HttpServletRequest request,
            TokenResponse tokenResponse, Map<String, Object> headers)
    {
        mAuthleteApi   = authleteApi;
        mRequest       = request;
        mTokenResponse = tokenResponse;
        mHeaders       = headers;
    }


    public Response process()
    {
        try
        {
            return createResponse();
        }
        catch (WebApplicationException cause)
        {
            return cause.getResponse();
        }
    }


    private Response createResponse() throws WebApplicationException
    {
        // This sample implementation creates an access token.

        // Client ID to assign.
        long clientId = determineClientId();

        // Scopes to assign.
        String[] scopes = determineScopes();

        // Resources to assign.
        URI[] resources = determineResources();

        // Subject to assign.
        String subject = determineSubject();

        // Create an access token.
        TokenCreateResponse tcResponse =
                createAccessToken(clientId, scopes, resources, subject);

        // Create a successful token response.
        return createSuccessfulResponse(tcResponse);
    }


    private long determineClientId()
    {
        // The client ID of the client that made the token exchange request.
        long clientId = mTokenResponse.getClientId();

        // If 'Service.tokenExchangeByIdentifiableClientsOnly' is false,
        // token exchange requests that contain no client identifier are not
        // rejected. In that case, 'clientId' here becomes 0.
        //
        // However, this authorization server implementation does not allow
        // unidentifiable clients to make token exchange requests regardless
        // of whether 'Service.tokenExchangeByIdentifiableClientsOnly' is
        // true or false.
        if (clientId == 0)
        {
            throw invalidRequest(
                    "This authorization server does not allow unidentifiable " +
                    "clients to make token exchange requests.");
        }

        // This simple implementation uses the client ID of the client
        // that made the token exchange request.
        return clientId;
    }


    private String[] determineScopes()
    {
        // This simple implementation uses the scopes specified
        // by the token exchange request.
        return mTokenResponse.getScopes();
    }


    private URI[] determineResources()
    {
        // This simple implementation uses the resources specified
        // by the token exchange request.
        return mTokenResponse.getResources();
    }


    private String determineSubject()
    {
        // The value of the "subject_token_type" request parameter.
        TokenType tokenType = mTokenResponse.getSubjectTokenType();

        // The subject to be assigned to a new access token.
        String subject = null;

        switch (tokenType)
        {
            case ACCESS_TOKEN:
            case REFRESH_TOKEN:
                // Use the subject associated with the token as the subject of
                // a new access token.
                subject = determineSubjectByTokenInfo();
                break;

            case JWT:
            case ID_TOKEN:
                // Use the value of the "sub" claim of the JWT as the subject of
                // a new access token.
                subject = determineSubjectByJwt();
                break;

            case SAML1:
            case SAML2:
            default:
                throw invalidRequest(
                        "This authorization server does not support the token type '" +
                        tokenType + "'.");
        }

        // If 'subject' failed to be determined.
        if (subject == null)
        {
            // This happens (1) when an access token that was created by
            // the client credentials flow was given or (2) when a JWT
            // that does not contain the "sub" claim was given.
            throw invalidRequest(
                    "Could not determine the subject from the given subject token.");
        }

        return subject;
    }


    private String determineSubjectByTokenInfo()
    {
        // When the token type is "urn:ietf:params:oauth:token-type:access_token"
        // or "urn:ietf:params:oauth:token-type:refresh_token", Authlete returns
        // more information about the token.
        TokenInfo tokenInfo = mTokenResponse.getSubjectTokenInfo();

        // The subject associated with the token. If the token was created by the
        // client credentials flow, the value is null.
        return tokenInfo.getSubject();
    }


    private String determineSubjectByJwt()
    {
        // When the token type is "urn:ietf:params:oauth:token-type:jwt" or
        // "urn:ietf:params:oauth:token-type:id_token", the format of the
        // subject token is JWT.
        //
        // Basic validation on the JWT has already been done by Authlete's
        // /auth/token API. See the JavaDoc of the TokenResponse class for
        // details about the validation steps.
        String subjectToken = mTokenResponse.getSubjectToken();

        JWT jwt;

        try
        {
            // Parse the subject token as JWT.
            jwt = JWTParser.parse(subjectToken);
        }
        catch (Exception cause)
        {
            // This won't happen because Authlete has already confirmed that
            // the format of the subject token conforms to the JWT specification.
            throw invalidRequest("The subject token failed to be parsed as JWT.");
        }

        // If the JWT is encrypted.
        if (jwt instanceof EncryptedJWT)
        {
            throw invalidRequest(
                    "This authorization server does not accept " +
                    "an encrypted JWT as a subject token.");
        }

        try
        {
            // Get the value of the "sub" claim from the payload of the JWT.
            //
            // An ID Token must always have the "sub" claim (OIDC Core) while
            // a JWT does not necessarily have the "sub" claim (RFC 7519).
            return jwt.getJWTClaimsSet().getSubject();
        }
        catch (Exception cause)
        {
            throw invalidRequest(
                    "The value of the 'sub' claim failed to be extracted " +
                    "from the payload of the subject token.");
        }
    }


    private TokenCreateResponse createAccessToken(
            long clientId, String[] scopes, URI[] resources, String subject)
    {
        // A request to Authlete's /auth/token/create API.
        TokenCreateRequest request = new TokenCreateRequest()
                .setGrantType(GrantType.TOKEN_EXCHANGE)
                .setClientId(clientId)
                .setScopes(scopes)
                .setResources(resources)
                .setSubject(subject)
                ;

        try
        {
            // Call Authlete's /auth/token/create API to create an access token.
            return mAuthleteApi.tokenCreate(request);
        }
        catch (Exception cause)
        {
            // API call to /auth/token/create failed.
            cause.printStackTrace();
            throw serverError("API call to /auth/token/create failed.");
        }
    }


    private Response createSuccessfulResponse(TokenCreateResponse tcResponse)
    {
        // The content of a successful token response that conforms to
        // Section 2.2.1. Successful Response of RFC 8693.
        String content = String.format(
                "{\n" +
                "  \"access_token\":\"%s\",\n" +
                "  \"issued_token_type\":\"urn:ietf:params:oauth:token-type:access_token\",\n" +
                "  \"token_type\":\"Bearer\",\n" +
                "  \"expires_in\":%d,\n" +
                "  \"scope\":\"%s\",\n" +
                "  \"refresh_token\":\"%s\"\n" +
                "}\n",
                extractAccessToken(tcResponse),
                tcResponse.getExpiresIn(),
                buildScope(tcResponse),
                tcResponse.getRefreshToken()
                );

        return toJsonResponse(Status.OK, content);
    }


    private String extractAccessToken(TokenCreateResponse tcResponse)
    {
        // If a JWT access token has been issued, it takes precedence over
        // a random-string access token.

        // An access token in the JWT format. This response parameter holds
        // a non-null value when Service.accessTokenSignAlg is not null.
        String at = tcResponse.getJwtAccessToken();

        // If an access token in the JWT format has not been issued.
        if (at == null)
        {
            // An access token whose format is just a random string.
            at = tcResponse.getAccessToken();
        }

        // The newly issued access token.
        return at;
    }


    private String buildScope(TokenCreateResponse tcResponse)
    {
        String[] scopes = tcResponse.getScopes();

        if (scopes == null)
        {
            return "";
        }

        return String.join(" ", scopes);
    }


    private Response toJsonResponse(Status status, String content)
    {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoCache(true);
        cacheControl.setNoStore(true);

        ResponseBuilder builder = Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .cacheControl(cacheControl)
                .entity(content)
                ;

        addResponseHeaders(builder, mHeaders);

        return builder.build();
    }


    private static void addResponseHeaders(ResponseBuilder builder, Map<String, Object> headers)
    {
        if (headers == null)
        {
            return;
        }

        for (Map.Entry<String, Object> header : headers.entrySet())
        {
            builder.header(header.getKey(), header.getValue());
        }
    }


    private WebApplicationException toException(Status status, String error, String description)
    {
        String content = String.format(
                "{\n" +
                "  \"error\":\"%s\",\n" +
                "  \"error_description\":\"%s\"\n" +
                "}\n",
                error, description);

        Response response = toJsonResponse(status, content);

        return new WebApplicationException(response);
    }


    private WebApplicationException invalidRequest(String message)
    {
        return toException(Status.BAD_REQUEST, "invalid_request", message);
    }


    private WebApplicationException serverError(String message)
    {
        return toException(Status.INTERNAL_SERVER_ERROR, "server_error", message);
    }
}
