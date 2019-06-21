/*
 * Copyright (C) 2016 Authlete, Inc.
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


import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.mvc.Viewable;

import com.authlete.common.dto.DeviceVerificationResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.DeviceAuthorizationPageModel;
import com.authlete.jaxrs.DeviceVerificationPageModel;
import com.authlete.jaxrs.spi.DeviceVerificationRequestHandlerSpiAdapter;


/**
 * Empty implementation of {@link TokenRequestHandlerSpi} interface.
 *
 * <p>
 * If you don't support <a href="https://tools.ietf.org/html/rfc6749#section-4.3"
 * >Resource Owner Password Credentials Grant</a>, you don't have to
 * override {@link #authenticateUser(String, String)} method.
 * </p>
 *
 * @author Takahiko Kawasaki
 */
public class DeviceVerificationRequestHandlerSpiImpl extends DeviceVerificationRequestHandlerSpiAdapter
{
    /**
     * {@code "text/html;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_HTML =
            MediaType.TEXT_HTML_TYPE.withCharset("UTF-8");


    /**
     * The page template to ask the resource owner for authorization.
     */
    private static final String TEMPLATE = "/device/verification";


    /**
     * The page template to ask the resource owner for authorization.
     */
    private static final String COMPLETE_TEMPLATE = "/device/authorization";


    /**
     * The current user session.
     */
    private HttpSession mSession;


    /**
     *  The authenticated user.
     */
    private User mUser;


    /**
     *  The user code given by the user..
     */
    private String mUserCode;


    public DeviceVerificationRequestHandlerSpiImpl(HttpSession session, User user, String userCode)
    {
        mSession  = session;
        mUser     = user;
        mUserCode = userCode;
    }


    @Override
    public String getUserCode()
    {
        return mUserCode;
    }


    @Override
    public Response onUserCodeValid(DeviceVerificationResponse info)
    {
        // Ask the user to authorize the client.

        // Store the valid user code to the user's session for later use.
        mSession.setAttribute("userCode", mUserCode);

        // The model for rendering the verification page.
        DeviceAuthorizationPageModel model = new DeviceAuthorizationPageModel(info);

        // Create a Viewable instance that represents the verification page.
        Viewable viewable = new Viewable(COMPLETE_TEMPLATE, model);

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MEDIA_TYPE_HTML).build();
    }


    @Override
    public Response onUserCodeExpired()
    {
        // Urge the user to re-initiate device flow.
        return Response
                .status(Status.BAD_REQUEST)
                .entity("User Code Expired.")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }


    @Override
    public Response onUserCodeNotExist()
    {
        // Urge the user to re-input a valid user code.

        // The user.
        User user = (User)mSession.getAttribute("user");

        // The notification to be shown to the user on the verification page.
        String notification = "The user code does not exist.";

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel(
                null, mUserCode, user, notification);

        // Create a Viewable instance that represents the authorization
        // page. Viewable is a class provided by Jersey for MVC.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Return a response of "404 Not Found".
        return Response
                .status(Status.NOT_FOUND)
                .entity(viewable)
                .type(MEDIA_TYPE_HTML)
                .build();
    }


    @Override
    public Response onServerError()
    {
        // Urge the user to re-initiate device flow.
        return Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .entity("Server Error.")
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
