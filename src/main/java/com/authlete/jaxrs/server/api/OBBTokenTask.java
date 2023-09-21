package com.authlete.jaxrs.server.api;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.jaxrs.server.obb.database.ConsentDao;
import com.authlete.jaxrs.server.obb.model.Consent;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


public class OBBTokenTask
{
    public void process(
            AuthleteApi authleteApi, HttpServletRequest request,
            MultivaluedMap<String, String> requestParams,
            Response response, Map<String, Object> responseParams)
    {
        // If further processing is not needed.
        if (!needsProcessing(requestParams, response, responseParams))
        {
            // Nothing to do.
            return;
        }

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


    private static boolean needsProcessing(
            MultivaluedMap<String, String> requestParams,
            Response response, Map<String, Object> responseParams)
    {
        // If the token request failed.
        if (response.getStatus() != Status.OK.getStatusCode())
        {
            // Nothing to do.
            return false;
        }

        // If the token request is a refresh token request.
        String grantType = requestParams.getFirst("grant_type");
        if (grantType != null && grantType.equals("refresh_token"))
        {
            // Because Open Baning Brasil prohibits refresh token rotation,
            // no new refresh token is issued by the refresh token request.
            //
            // The value of "refresh_token" in the response, even if any,
            // holds the same value of "refresh_token" in the request.
            //
            // To make the service behave in this way, the setting of the
            // "Service.refreshTokenKept" flag needs to be set to true.
            // On the web console, "Refresh Token Continuous Use" represents
            // the flag. Selecting the option "Kept" prevents the Service
            // from doing refresh token rotation.

            // Nothing to do.
            return false;
        }

        // If no refresh token has been issued.
        if (!responseParams.containsKey("refresh_token"))
        {
            // Nothing to do.
            return false;
        }

        // There are some tasks to be done for the newly issued refresh token.
        return true;
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
        // 400 Bad Request with Content-Type:application/json.
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
