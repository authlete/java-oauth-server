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
package com.authlete.jaxrs.server.api.device;


import static com.authlete.jaxrs.server.util.ExceptionUtil.badRequestException;
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import com.authlete.jaxrs.server.resilience.ResilientAuthleteApiFactory;
import com.authlete.common.types.User;
import com.authlete.jakarta.BaseDeviceCompleteEndpoint;


/**
 * The endpoint that receives a request from the form in the authorization page
 * in OAuth 2.0 Device Authorization Grant (Device Flow).
 *
 * @author Hideki Ikeda
 */
@Path("/api/device/complete")
public class DeviceCompleteEndpoint extends BaseDeviceCompleteEndpoint
{
    /**
     * Process a request from the form in the authorization page in OAuth 2.0
     * Device Authorization Grant (Device Flow).
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session.
        HttpSession session = getSession(request);

        // Get the information from the session.
        String userCode     = getUserCode(session);
        User user           = getUser(session);
        Date authTime       = (Date)session.getAttribute("authTime");
        String[] claimNames = (String[])takeAttribute(session, "claimNames");
        String[] acrs       = (String[])takeAttribute(session, "acrs");

        // Handle the device complete request.
        return handle(parameters, user, authTime, acrs, userCode, claimNames);
    }


    /**
     * Get the existing session.
     */
    private HttpSession getSession(HttpServletRequest request)
    {
        // Get the existing session.
        HttpSession session = request.getSession(false);

        // If there exists a session.
        if (session != null)
        {
            // OK.
            return session;
        }

        // A session does not exist. Make a response of "400 Bad Request".
        throw badRequestException("A session does not exist. Re-initiate the flow again.");
    }


    private String getUserCode(HttpSession session)
    {
        // Get and remove the user code from the session.
        String userCode = (String)takeAttribute(session, "userCode");

        if (userCode != null)
        {
            return userCode;
        }

        // A user code was not found in the session.
        throw badRequestException("A user code was not found in the session. Re-initiate the flow again.");
    }


    private User getUser(HttpSession session)
    {
        // Look up the user in the session to see if the user is already logged in.
        User sessionUser = (User)session.getAttribute("user");

        if (sessionUser != null)
        {
            // OK. The user has been already authenticated.
            return sessionUser;
        }

        // TODO: In this case, should we invalidate the user code here by calling
        // Authlete /api/device/complete API with result='TRANSACTION_FAILED'?

        // An authenticated user was not found in the session.
        throw badRequestException("An authenticated user was not found in the session. Re-initiate the flow again.");
    }


    private Response handle(
            MultivaluedMap<String, String> parameters, User user, Date userAuthenticatedAt,
            String[] acrs, String userCode, String[] claimNames)
    {
        return handle(ResilientAuthleteApiFactory.getDefaultApi(), new DeviceCompleteRequestHandlerSpiImpl(
                parameters, user, userAuthenticatedAt, acrs), userCode, claimNames);
    }
}
