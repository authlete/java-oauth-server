/*
 * Copyright (C) 2025 Authlete, Inc.
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


import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.dto.NativeSsoRequest;
import com.authlete.common.dto.NativeSsoResponse;
import com.authlete.common.dto.TokenResponse;
import com.authlete.common.types.GrantType;
import com.authlete.jaxrs.server.core.SessionTracker;
import com.authlete.jaxrs.server.nativesso.DeviceSecret;
import com.authlete.jaxrs.server.nativesso.DeviceSecretManager;
import com.authlete.jaxrs.server.util.ResponseUtil;


public class NativeSsoProcessor
{
    private final AuthleteApi mAuthleteApi;
    private final HttpServletRequest mRequest;
    private final TokenResponse mTokenResponse;
    private final Map<String, Object> mHeaders;


    public NativeSsoProcessor(
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
        // The device secret value and device secret hash that may be
        // included in the response from the /auth/token API.
        String deviceSecretValue = retrieveDeviceSecretValue();
        String deviceSecretHash  = retrieveDeviceSecretHash();

        // The session ID included in the response from the /auth/token API.
        String sessionId = retrieveSessionId();

        // The identifier of the device accessing this authorization server.
        String deviceId = retrieveDeviceId();

        // Validate the Native SSO parameters.
        DeviceSecret ds = validateParameters(
                deviceSecretValue, deviceSecretHash, sessionId, deviceId);

        // Call Authlete's /nativesso API to generate a Native SSO-compliant
        // ID token and a token response.
        NativeSsoResponse nsr = nativeSso(ds);

        // Generate a token response.
        return generateResponse(nsr);
    }


    private String retrieveDeviceSecretValue()
    {
        // The device secret that may be included in the response from the
        // /auth/token API.
        //
        // When the flow is the authorization code flow or the refresh token
        // flow, the device secret is the value of the "device_secret" request
        // parameter to the token endpoint.
        //
        // When the flow is the token exchange flow, the device secret is the
        // value of the "actor_token" request parameter to the token endpoint.
        return mTokenResponse.getDeviceSecret();
    }


    private String retrieveDeviceSecretHash()
    {
        // The device secret hash that may be included in the response from
        // the /auth/token API.
        //
        // The device secret hash is available only when the flow is the token
        // exchange flow. Its value originates from the "ds_hash" claim in the
        // subject token.
        return mTokenResponse.getDeviceSecretHash();
    }


    private String retrieveSessionId()
    {
        // The session ID included in the response from the /auth/token API.
        //
        // When the flow is the authorization code flow, the session ID is the
        // value included in the preceding call of the /auth/authorization/issue
        // API.
        //
        // When the flow is the refresh token flow, the session ID is the one
        // associated with the refresh token.
        //
        // When the flow is the token exchange flow, the session ID is the
        // value of the "sid" claim in the subject token.
        return mTokenResponse.getSessionId();
    }


    private String retrieveDeviceId()
    {
        // Information that can identify the device should be extracted from the
        // HTTP request (mRequest) and processed before being used as a device ID.
        //
        // However, this sample implementation does not perform such processing.
        // As a result, it cannot determine whether Native App 1 and Native App 2
        // are running on the same device.
        return null;
    }


    private DeviceSecret validateParameters(
            String deviceSecretValue, String deviceSecretHash,
            String sessionId, String deviceId)
    {
        // Validate the session ID.
        validateSessionId(sessionId);

        if (deviceSecretValue == null)
        {
            // This happens when (1) the flow is either the authorization code
            // flow or the refresh token flow, and (2) the token request does
            // not contain the "device_secret" request parameter.

            // Create a new DeviceSecret instance and register it.
            return createAndRegisterDeviceSecret(sessionId, deviceId);
        }

        // Look up the DeviceSecret instance corresponding to the device
        // secret value specified by the "device_secret" request parameter
        // or the "actor_token" request parameter.
        DeviceSecret ds = DeviceSecretManager.getByValue(deviceSecretValue);

        // If the specified device secret exists and is valid.
        if (ds != null && isValid(ds, deviceSecretHash, sessionId, deviceId))
        {
            // Use the existing DeviceSecret instance.
            return ds;
        }

        // The specified device secret does not exist or is invalid.

        if (deviceSecretHash == null)
        {
            // This happens when (1) the flow is either the authorization code
            // flow or the refresh token flow, and (2) the token request contains
            // the "device_secret" request parameter.

            // Since the Native SSO specification states as follows:
            //
            //   If a device_secret is provided as part of the token request,
            //   and the device_secret is invalid, then the AS must process
            //   the request as if no device_secret was specified.
            //
            // We don't treat this case as an error. Instead, we provide a
            // new DeviceSecret instance.
            return createAndRegisterDeviceSecret(sessionId, deviceId);
        }

        // This happens when (1) the flow is the token exchange flow.

        // Build a message describing the error.
        String message = buildInvalidDeviceSecretErrorMessage(
                ds, deviceSecretValue, deviceSecretHash, sessionId, deviceId);

        // 400 Bad Request with error=invalid_grant
        throw invalidGrant(message);
    }


    private void validateSessionId(String sessionId)
    {
        // If the session ID is still active.
        if (SessionTracker.isActiveSessionId(sessionId))
        {
            // Okay. The session is still active.
            return;
        }

        // Build an error message indicating that the session ID is no longer valid.
        String message = buildInvalidSessionIdErrorMessage(mTokenResponse.getGrantType());

        // 400 Bad Request with error=invalid_grant
        throw invalidGrant(message);
    }


    private static String buildInvalidSessionIdErrorMessage(GrantType grantType)
    {
        switch (grantType)
        {
            case AUTHORIZATION_CODE:
                return "The session ID used during the authorization request is no longer valid.";

            case REFRESH_TOKEN:
                return "The session ID associated with the refresh token is no longer valid.";

            case TOKEN_EXCHANGE:
                return "The session ID associated with the subject token is no longer valid";

            default:
                // This should never happen.
                return "The session ID associated with the token request is no longer valid.";
        }
    }


    private DeviceSecret createAndRegisterDeviceSecret(String sessionId, String deviceId)
    {
        // Create a new DeviceSecret instance.
        DeviceSecret ds = createDeviceSecret(sessionId, deviceId);

        // Register it.
        DeviceSecretManager.register(ds);

        return ds;
    }


    private DeviceSecret createDeviceSecret(String sessionId, String deviceId)
    {
        // The device secret value.
        String dsValue = generateDeviceSecretValue();

        // The device secret hash.
        String dsHash = computeDeviceSecretHash(dsValue);

        // Create a DeviceSecret instance tied to the device and session.
        return new DeviceSecret()
                .setValue(dsValue)
                .setHash(dsHash)
                .setSessionId(sessionId)
                .setDeviceId(deviceId)
                ;
    }


    private String generateDeviceSecretValue()
    {
        // A random value.
        return UUID.randomUUID().toString();
    }


    private String computeDeviceSecretHash(String deviceSecretValue)
    {
        // Compute the hash of the specified device secret value.
        return DeviceSecret.computeHash(deviceSecretValue);
    }


    private boolean isValid(
            DeviceSecret ds, String deviceSecretHash, String sessionId, String deviceId)
    {
        // If the device secret hash is specified.
        if (deviceSecretHash != null)
        {
            // If the device secret hashes do not match.
            if (!Objects.equals(ds.getHash(), deviceSecretHash))
            {
                // Invalid.
                return false;
            }
        }

        // If the session IDs do not match.
        if (!Objects.equals(ds.getSessionId(), sessionId))
        {
            // Invalid.
            return false;
        }

        // If the existing DeviceSecret instance is tied to a device ID.
        if (ds.getDeviceId() != null)
        {
            // If the device IDs do not match.
            if (!Objects.equals(ds.getDeviceId(), deviceId))
            {
                // Invalid.
                return false;
            }
        }

        // Valid.
        return true;
    }


    private static String buildInvalidDeviceSecretErrorMessage(
            DeviceSecret ds, String deviceSecretValue, String deviceSecretHash,
            String sessionId, String deviceId)
    {
        // This method is called only from the context of the token exchange flow.

        if (ds == null)
        {
            return String.format(
                    "The specified device secret ('%s') does not exist.",
                    deviceSecretValue);
        }

        // If the device secret hashes don't match.
        if (!Objects.equals(ds.getHash(), deviceSecretHash))
        {
            return String.format(
                    "The device secret hash ('%s') in the subject token does " +
                    "not match the hash of the presented device secret ('%s').",
                    deviceSecretHash, deviceSecretValue);
        }

        // If the session IDs don't match.
        if (!Objects.equals(ds.getSessionId(), sessionId))
        {
            return String.format(
                    "The session ID ('%s') in the subject token does not match " +
                    "the one associated with the presented device secret ('%s').",
                    sessionId, deviceSecretValue);
        }

        // If the existing device secret is tied to a device ID and it does not
        // match the identifier of the device accessing this authorization server.
        if (ds.getDeviceId() != null && !Objects.equals(ds.getDeviceId(), deviceId))
        {
            return String.format(
                    "The identifier of the device accessing this authorization " +
                    "server does not match the one associated with the presented " +
                    "device secret ('%s').",
                    deviceSecretValue);
        }

        // Hmm. The code flow should not reach here.
        return String.format(
                "The specified device secret ('%s') is invalid for an unknown reason.",
                deviceSecretValue);
    }


    private NativeSsoResponse nativeSso(DeviceSecret ds)
    {
        // Prepare request parameters for the /nativesso API.
        NativeSsoRequest request = new NativeSsoRequest()
                .setAccessToken(chooseAccessToken())
                .setRefreshToken(mTokenResponse.getRefreshToken())
                .setDeviceSecret(ds.getValue())
                .setDeviceSecretHash(ds.getHash())
                ;

        try
        {
            // Call Authlete's /nativesso API.
            return mAuthleteApi.nativeSso(request, null);
        }
        catch (Exception cause)
        {
            // API call to /nativeSso failed.
            cause.printStackTrace();

            throw serverError("API call to /nativesso failed: " + cause.getMessage());
        }
    }


    private String chooseAccessToken()
    {
        // The access token in the JWT format. Whether this is available
        // depends on configuration.
        String jwtAt = mTokenResponse.getJwtAccessToken();

        return (jwtAt != null) ? jwtAt : mTokenResponse.getAccessToken();
    }


    private Response generateResponse(NativeSsoResponse nsr)
    {
        // The message body of the token response the /nativesso API prepared.
        String content = nsr.getResponseContent();

        // Dispatch according to the "action" parameter in the response from
        // the /nativesso API.
        switch (nsr.getAction())
        {
            case OK:
                // 200 OK with application/json
                return ResponseUtil.okJson(content, mHeaders);

            case INTERNAL_SERVER_ERROR:
            case CALLER_ERROR:
                // 500 Internal Server Error with application/json
                return ResponseUtil.internalServerErrorJson(content, mHeaders);

            default:
                // 500 Internal Server Error with application/json
                throw unknownAction(nsr.getAction());
        }
    }


    private WebApplicationException invalidGrant(String message)
    {
        // {"error":"invalid_grant", "error_description":"<message>"}
        String content = buildErrorJson("invalid_grant", message);

        // 400 Bad Request with application/json
        Response response = ResponseUtil.badRequestJson(content, mHeaders);

        // Wrap the response in a WebApplicationException.
        return new WebApplicationException(response);
    }


    private WebApplicationException serverError(String message)
    {
        // {"error":"server_error", "error_description":"<message>"}
        String content = buildErrorJson("server_error", message);

        // 500 Internal Server Error with application/json
        Response response = ResponseUtil.internalServerErrorJson(content, mHeaders);

        // Wrap the response in a WebApplicationException.
        return new WebApplicationException(response);
    }


    private WebApplicationException unknownAction(NativeSsoResponse.Action action)
    {
        String message = String.format(
                "The /nativesso has returned an unknown action '%s'.", action);

        // 500 Internal Server Error with application/json
        return serverError(message);
    }


    private static String buildErrorJson(String error, String description)
    {
        return String.format(
                "{\n" +
                "  \"error\": \"%s\",\n" +
                "  \"error_description\": \"%s\"\n" +
                "}\n",
                error, description);
    }
}
