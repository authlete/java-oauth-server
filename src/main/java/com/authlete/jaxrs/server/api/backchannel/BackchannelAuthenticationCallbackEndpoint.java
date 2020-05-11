/*
 * Copyright (C) 2019 Authlete, Inc.
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
package com.authlete.jaxrs.server.api.backchannel;


import static com.authlete.jaxrs.server.util.ExceptionUtil.badRequestException;
import static com.authlete.jaxrs.server.util.ExceptionUtil.internalServerErrorException;
import static com.authlete.jaxrs.server.util.ResponseUtil.noContent;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.BackchannelAuthenticationCompleteRequest.Result;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BackchannelAuthenticationCompleteRequestHandler;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationCallbackRequest;


/**
 * The endpoint called back from <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a> when the authentication device
 * simulator is used in asynchronous mode.
 *
 * <p>
 * Note that it is assumed that the authorization server has made a request to the
 * authentication device simulator for end-user authentication and authorization
 * in {@link AsyncAuthenticationDeviceProcessor} before this endpoint is called
 * back from the authentication device simulator. The result of the end-user
 * authentication and authorization is expected to be contained in the request to
 * this endpoint.
 * </p>
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication device
 *      simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/">Authlete
 *      CIBA authentication device simulator API</a>
 *
 * @see AsyncAuthenticationDeviceProcessor
 *
 * @author Hideki Ikeda
 */
@Path("/api/backchannel/authentication/callback")
public class BackchannelAuthenticationCallbackEndpoint
{
    /**
     * The callback endpoint called back from Authlete CIBA authentication device
     * simulator when it is used in in asynchronous mode.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(AsyncAuthenticationCallbackRequest request)
    {
        try
        {
            return doProcess(request);
        }
        catch (WebApplicationException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            throw internalServerErrorException("unexpected error: " + t.getMessage());
        }
    }


    private Response doProcess(AsyncAuthenticationCallbackRequest request)
    {
        // Get the result of end-user authentication and authorization.
        Result result = getResult(request);

        // Get the ID of the request that this authorization server made to the
        // authentication device in AsyncAuthenticationDeviceProcessor.
        String requestId = getRequestId(request);

        // Retrieve information that was stored in AsyncAuthenticationDeviceProcessor.
        AuthInfo authInfo = getAuthInfo(requestId);

        // Get some variables from the stored information.
        String ticket           = authInfo.getTicket();
        User user               = authInfo.getUser();
        String[] claimNames     = authInfo.getClaimNames();
        String[] acrs           = authInfo.getAcrs();
        Date authTime           = (result == Result.AUTHORIZED) ? new Date() : null;
        String errorDescription = determineErrorDescription(request);

        // Complete the authentication and authorization process.
        new BackchannelAuthenticationCompleteRequestHandler(
                AuthleteApiFactory.getDefaultApi(),
                new BackchannelAuthenticationCompleteHandlerSpiImpl(
                        result, user, authTime, acrs, errorDescription, null)
            )
        .handle(ticket, claimNames);

        // Delete the stored information.
        removeAuthInfo(requestId);

        // 204 No Content.
        return noContent();
    }


    private Result getResult(AsyncAuthenticationCallbackRequest request)
    {
        com.authlete.jaxrs.server.ad.type.Result result = request.getResult();

        if (result == null)
        {
            // Invalid result.
            throw badRequestException("The result must not be empty.");
        }

        switch (result)
        {
            case allow:
                // The user authorized the client.
                return Result.AUTHORIZED;

            case deny:
                // The user denied the client.
                return Result.ACCESS_DENIED;

            case timeout:
                // Timeout occurred while the authentication device was authenticating
                // the user.
                return Result.TRANSACTION_FAILED;

            default:
                // An unknown result returned from the authentication device.
                // This should never happen.
                throw badRequestException("Unknown result.");
        }
    }


    private String getRequestId(AsyncAuthenticationCallbackRequest request)
    {
        // The ID of the request that this authorization server made to the
        // authentication device.
        String requestId = request.getRequestId();

        if (requestId == null || requestId.length() == 0)
        {
            // The request ID is empty.
            throw badRequestException("The request ID must not be empty.");
        }

        return requestId;
    }


    private AuthInfo getAuthInfo(String requestId)
    {
        // Retrieve the information that was stored when this authorization server
        // made the request to the authentication device in the asynchronous mode
        // at '/api/backchannel/authentication' API.
        AuthInfo info = AuthInfoHolder.get(requestId);

        if (info == null)
        {
            // The information for the request ID doesn't exist.
            throw badRequestException("The request ID is invalid.");
        }

        return info;
    }


    private String determineErrorDescription(AsyncAuthenticationCallbackRequest request)
    {
        com.authlete.jaxrs.server.ad.type.Result result = request.getResult();

        if (result == null)
        {
            return null;
        }

        switch (result)
        {
            case allow:
                return null;

            case deny:
                return "The backchannel authentication request was denied by the end-user.";

            case timeout:
                return "Timeout occurred on the authentication device.";

            default:
                return "An unrecognizable result was returned from the authentication device.";
        }
    }


    private void removeAuthInfo(String requestId)
    {
        // Remove the information for the request ID from the holder.
        AuthInfoHolder.remove(requestId);
    }
}
