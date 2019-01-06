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


import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.BackchannelAuthenticationCompleteRequest.Result;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BackchannelAuthenticationCompleteRequestHandler;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationCallbackRequest;


/**
 * The callback endpoint for Authlete's CIBA authentication device simulator used
 * in asynchronous mode to notify the authorization server of the result of end-user
 * authentication and authorization.
 *
 * @see com.authlete.jaxrs.server.api.backchannel.AsyncAuthenticationDeviceProcessor AsyncAuthenticationDeviceProcessor
 *
 * @author Hideki Ikeda
 */
@Path("/api/backchannel/authentication/callback")
public class BackchannelAuthenticationCallbackEndpoint
{
    /**
     * The callback endpoint for Authlete's CIBA authentication device simulator
     * used in asynchronous mode.
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
            throw internalServerError("unexpected error", t);
        }
    }


    private Response doProcess(AsyncAuthenticationCallbackRequest request)
    {
        System.out.println("Callbacked");

        // Get the result of end-user authentication and authorization.
        Result result = getResult(request);

        System.out.println("result: " + result);

        // Get the ID of the request that this authorization server made to the
        // authentication device in AsyncAuthenticationDeviceProcessor.
        String requestId = getRequestId(request);

        System.out.println("requestId: " + requestId);

        // Retrieve information that was stored in AsyncAuthenticationDeviceProcessor.
        AuthInfo authInfo = getAuthInfo(requestId);

        // Get some variables from the stored information.
        String ticket       = authInfo.getTicket();
        User user           = authInfo.getUser();
        String[] claimNames = authInfo.getClaimNames();
        String[] acrs       = authInfo.getAcrs();
        Date authTime       = (result == Result.AUTHORIZED) ? new Date() : null;

        // Debug code.
        log(ticket, user, acrs, claimNames);

        // Complete the authentication and authorization process.
        new BackchannelAuthenticationCompleteRequestHandler(
                AuthleteApiFactory.getDefaultApi(),
                new BackchannelAuthenticationCompleteHandlerSpiImpl(result, user, authTime, acrs)
            )
        .handle(ticket, claimNames);

        // Delete the stored information.
        removeAuthInfo(requestId);

        // 204 No Content.
        return builder(Status.NO_CONTENT).build();
    }


    private void log(String ticket, User user, String[] requestedAcrs, String[] requestedClaimNames)
    {
        System.out.println("user: " + user.getSubject());
        System.out.println("ticket: " + ticket);
        if (requestedAcrs != null) { System.out.println("requestedAcrs[0]: " + requestedAcrs[0]); }
        if (requestedClaimNames != null) { System.out.println("requestedClaimNames[0]: " + requestedClaimNames[0]); }
    }


    private Result getResult(AsyncAuthenticationCallbackRequest request)
    {
        com.authlete.jaxrs.server.ad.type.Result result = request.getResult();

        if (result == null)
        {
            System.out.println("result was null.");
            // Invalid result.
            throw badRequest("The result must not be empty.");
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
                return Result.ERROR;

            default:
                // An unknown result returned from the authentication device.
                // This should never happen.
                throw badRequest("Unknown result.");
        }
    }


    private String getRequestId(AsyncAuthenticationCallbackRequest request)
    {
        // The ID of the request that this authorization server made to the
        // authentication device.
        String requestId = request.getRequestId();

        if (requestId == null || requestId.length() == 0)
        {
            System.out.println("request Id was null.");
            // The request ID is empty.
            throw badRequest("The request ID must not be empty.");
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
            throw badRequest("The request ID is invalid.");
        }

        return info;
    }


    private void removeAuthInfo(String requestId)
    {
        // Remove the information for the request ID from the holder.
        AuthInfoHolder.remove(requestId);
    }


    private WebApplicationException badRequest(String message)
    {
        Response response = builder(Status.BAD_REQUEST, message).build();

        return new WebApplicationException(message, response);
    }


    private WebApplicationException internalServerError(String message, Throwable cause)
    {
        // Append the message of the cause.
        message += ": " + cause.getMessage();

        Response response = builder(Status.INTERNAL_SERVER_ERROR, message).build();

        return new WebApplicationException(message, response);
    }


    private ResponseBuilder builder(Status status, String message)
    {
        return builder(status)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                ;
    }


    private ResponseBuilder builder(Status status)
    {
        return Response.status(status);
    }
}
