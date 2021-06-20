/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.obb;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.BaseTokenEndpoint;
import com.authlete.jaxrs.TokenRequestHandler.Params;
import com.authlete.jaxrs.server.obb.database.ConsentDao;
import com.authlete.jaxrs.server.obb.model.Consent;
import com.authlete.jaxrs.server.obb.util.ObbUtils;
import com.authlete.jaxrs.spi.TokenRequestHandlerSpiAdapter;
import com.authlete.jaxrs.util.CertificateUtils;


/**
 * Sample implementation of Token Endpoint of Open Banking Brasil.
 */
@Path("/api/obb/token")
public class TokenEndpoint extends BaseTokenEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Authlete API
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();

        // Process the token request in a standard way.
        Response response = processTokenRequest(authleteApi, request, parameters);

        // If the token request succeeded.
        if (response.getStatus() == Status.OK.getStatusCode())
        {
            // Do additional tasks specific to Open Banking Brasil.
            doTasks(authleteApi, (String)response.getEntity());
        }

        return response;
    }


    private Response processTokenRequest(
            AuthleteApi authleteApi, HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Parameters for Authlete's /api/auth/token API.
        Params params = buildParams(request, parameters);

        // Handle the token request.
        return handle(authleteApi, new TokenRequestHandlerSpiAdapter(), params);
    }


    private static Params buildParams(
            HttpServletRequest request, MultivaluedMap<String, String> parameters)
    {
        // Parameters for Authlete's /api/auth/token API.
        return new Params()
                .setParameters(parameters)
                .setAuthorization(request.getHeader(HttpHeaders.AUTHORIZATION))
                .setClientCertificatePath(CertificateUtils.extractChain(request))
                ;
    }


    @SuppressWarnings("unchecked")
    private static void doTasks(AuthleteApi authleteApi, String entity)
    {
        // The entity conforms to the token response defined in RFC 6749.
        // Parse it as JSON.
        Map<String, Object> responseParams = Utils.fromJson(entity, Map.class);

        // Do tasks related to "consent".
        doConsentTasks(authleteApi, responseParams);
    }


    private static void doConsentTasks(
            AuthleteApi authleteApi, Map<String, Object> responseParams)
    {
        // Get the consent ID associated with the access token.
        String consentId = extractConsentId(responseParams);

        // If no consent ID is associated with the access token.
        if (consentId == null)
        {
            // Nothing to do.
            return;
        }

        // Get the consent corresponding to the consent ID.
        Consent consent = ConsentDao.getInstance().read(consentId);

        // If there is no consent which corresponds to the consent ID.
        if (consent == null)
        {
            // Delete the access token (and the refresh token).
            deleteAccessToken(authleteApi, responseParams);

            // Return an error response to the client application.
            throw badRequestException("invalid_request", String.format(
                    "There is no consent corresponding to the consent ID '%s'.", consentId));
        }

        // Task on a refresh token.
        doConsentTaskOnRefreshToken(authleteApi, responseParams, consent);
    }


    private static String extractConsentId(Map<String, Object> responseParams)
    {
        // Get the value of the "scope" response parameter.
        String scope = (String)responseParams.get("scope");

        // If the token response does not contain "scope".
        if (scope == null)
        {
            // Nothing to do.
            return null;
        }

        // The value of "scope" is a space-delimited scope names.
        String[] scopes = scope.split(" +");

        // Extract a "consent:{consentId}" scope from the scope list.
        String consentScope = ObbUtils.extractConsentScope(scopes);

        // If the scope list does not contain "consent:{consentId}".
        if (consentScope == null)
        {
            // Consent ID is not available.
            return null;
        }

        // Extract the "{consentId}" part from "consent:{consentId}".
        return consentScope.substring(8);
    }


    private static void deleteAccessToken(
            AuthleteApi authleteApi, Map<String, Object> responseParams)
    {
        // The access token issued for the token request.
        String accessToken = (String)responseParams.get("access_token");

        // If the token response does not contain "access_token".
        if (accessToken == null)
        {
            // This won't happen.
            return;
        }

        try
        {
            // Delete the access token. Authlete will remove the refresh
            // token that is coupled with the access token, too.
            authleteApi.tokenDelete(accessToken);
        }
        catch (Exception e)
        {
            // Ignore the error.
        }
    }


    private static void doConsentTaskOnRefreshToken(
            AuthleteApi authleteApi, Map<String, Object> responseParams, Consent consent)
    {
        // The refresh token issued for the token request.
        String refreshToken = (String)responseParams.get("refresh_token");

        // If the token response does not contain a refresh token.
        if (refreshToken == null)
        {
            // Nothing to do.
            return;
        }

        // Open Banking Brasil Financial-grade API Security Profile 1.0
        // 7.2.2. Authorization server
        //
        //   1. shall issue refresh tokens with validity equal to the
        //      expirationDateTime defined on the linked Consent Resource;

        // Change the expiration date of the refresh token.
        changeRefreshTokenExpirationDate(
                authleteApi, refreshToken, consent.getExpirationDateTime());

        // Bind the refresh token to the consent.
        consent.setRefreshToken(refreshToken);
        ConsentDao.getInstance().update(consent);
    }


    private static void changeRefreshTokenExpirationDate(
            AuthleteApi authleteApi, String refreshToken, String expirationDate)
    {
        // TODO
        // Authlete will provide an API whereby to change the expiration date
        // of a refresh token.
    }


    private static WebApplicationException badRequestException(
            String code, String description)
    {
        Response response = Response.status(Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(error(code, description))
                .build()
                ;

        return new WebApplicationException(response);
    }


    private static String error(String code, String description)
    {
        return String.format(
                "{\n  \"error\":\"%s\",\n  \"error_description\":\"%s\"\n}\n",
                code, description);
    }
}
