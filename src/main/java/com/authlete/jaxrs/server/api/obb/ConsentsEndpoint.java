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


import static com.authlete.common.util.FapiUtils.X_FAPI_INTERACTION_ID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiException;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.obb.database.ConsentDao;
import com.authlete.jaxrs.server.obb.model.Consent;
import com.authlete.jaxrs.server.obb.model.CreateConsent;
import com.authlete.jaxrs.server.obb.model.ResponseConsent;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


/**
 * Sample implementation of Consents API of Open Banking Brasil.
 */
@Path("/api/obb/consents")
public class ConsentsEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @Context HttpServletRequest request,
            @HeaderParam(X_FAPI_INTERACTION_ID) String incomingInteractionId,
            CreateConsent createConsent)
    {
        String code = "Consent Create";

        // Compute a value for the "x-fapi-interaction-id" HTTP response header.
        String outgoingInteractionId =
                ObbUtils.computeOutgoingInteractionId(code, incomingInteractionId);

        // Validate the access token.
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();
        IntrospectionResponse info = ObbUtils.validateAccessToken(
                outgoingInteractionId, code, authleteApi, request, "consents");

        // Validate the input.
        validateCreateConsent(
                outgoingInteractionId, code, createConsent);

        // Create "consent".
        Consent consent = ConsentDao.getInstance()
                .create(createConsent, info.getClientId());

        // Build a response body.
        ResponseConsent rc = ResponseConsent.create(consent);

        // Build a successful response.
        return ObbUtils.created(outgoingInteractionId, rc);
    }


    @GET
    @Path("{consentId}")
    public Response read(
            @Context HttpServletRequest request,
            @HeaderParam(X_FAPI_INTERACTION_ID) String incomingInteractionId,
            @PathParam("consentId") String consentId)
    {
        String code = "Consent Read";

        // Compute a value for the "x-fapi-interaction-id" HTTP response header.
        String outgoingInteractionId =
                ObbUtils.computeOutgoingInteractionId(code, incomingInteractionId);

        // Validate the access token.
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();
        IntrospectionResponse info = ObbUtils.validateAccessToken(
                outgoingInteractionId, code, authleteApi, request, "consents");

        // Find "consent".
        Consent consent = ConsentDao.getInstance().read(consentId);

        // Validate the consent.
        validateConsent(outgoingInteractionId, code, consent, info);

        // Build a response body.
        ResponseConsent rc = ResponseConsent.create(consent);

        // Build a successful response.
        return ObbUtils.ok(outgoingInteractionId, rc);
    }


    @DELETE
    @Path("{consentId}")
    public Response delete(
            @Context HttpServletRequest request,
            @HeaderParam(X_FAPI_INTERACTION_ID) String incomingInteractionId,
            @PathParam("consentId") String consentId)
    {
        String code = "Consent Delete";

        // Compute a value for the "x-fapi-interaction-id" HTTP response header.
        String outgoingInteractionId =
                ObbUtils.computeOutgoingInteractionId(code, incomingInteractionId);

        // Validate the access token.
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();
        IntrospectionResponse info = ObbUtils.validateAccessToken(
                outgoingInteractionId, code, authleteApi, request, "consents");

        // Find "consent".
        Consent consent = ConsentDao.getInstance().read(consentId);

        // Validate the consent.
        validateConsent(outgoingInteractionId, code, consent, info);

        // Delete the refresh token associated with the consent.
        deleteRefreshToken(
                outgoingInteractionId, code, authleteApi, consent.getRefreshToken());

        // Delete the consent.
        ConsentDao.getInstance().delete(consentId);

        // Build a successful response.
        return ObbUtils.noContent(outgoingInteractionId);
    }


    private static void validateCreateConsent(
            String outgoingInteractionId, String code,
            CreateConsent createConsent)
    {
        if (createConsent == null)
        {
            throw ObbUtils.badRequestException(outgoingInteractionId,
                    code, "The request has no body.");
        }

        // This sample implementation does not validate the content.
    }


    private static void validateConsent(
            String outgoingInteractionId, String code,
            Consent consent, IntrospectionResponse info)
    {
        // If there is no consent corresponding to the presented consent ID.
        if (consent == null)
        {
            throw ObbUtils.notFoundException(outgoingInteractionId,
                    code, "The consent ID does not exist.");
        }

        // If the client Id of the consent does not match the client ID
        // of the access token.
        if (consent.getClientId() != info.getClientId())
        {
            throw ObbUtils.forbiddenException(outgoingInteractionId,
                    code, "Cannot access the consent with the access token.");
        }
    }


    private static void deleteRefreshToken(
            String outgoingInteractionId, String code,
            AuthleteApi authleteApi, String refreshToken)
    {
        if (refreshToken == null)
        {
            return;
        }

        try
        {
            // Delete the refresh token by calling Authlete's
            // /api/auth/token/delete/{tokenIdentifier} API.
            authleteApi.tokenDelete(refreshToken);
        }
        catch (AuthleteApiException e)
        {
            // Failed to delete the token.
            e.printStackTrace();

            throw ObbUtils.internalServerErrorException(
                    outgoingInteractionId, code, e.getMessage());
        }
    }
}
