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


import static com.authlete.jaxrs.server.util.ResponseUtil.badRequest;
import static com.authlete.jaxrs.server.util.ResponseUtil.internalServerError;
import static com.authlete.jaxrs.server.util.ResponseUtil.notFound;
import static com.authlete.jaxrs.server.util.ResponseUtil.ok;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.dto.DeviceVerificationResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.DeviceAuthorizationPageModel;
import com.authlete.jaxrs.DeviceVerificationPageModel;
import com.authlete.jaxrs.spi.DeviceVerificationRequestHandlerSpiAdapter;


/**
 * Empty implementation of {@link DeviceVerificationRequestHandlerSpi} interface.
 *
 * @author Hideki Ikeda
 */
public class DeviceVerificationRequestHandlerSpiImpl extends DeviceVerificationRequestHandlerSpiAdapter
{
    /**
     * The page template to ask the end-user for a user code.
     */
    private static final String VERIFICATION_PAGE_TEMPLATE = "/device/verification";


    /**
     * The page template to ask the end-user for authorization.
     */
    private static final String AUTHORIZATION_PAGE_TEMPLATE = "/device/authorization";


    /**
     * The current user session.
     */
    private HttpSession mSession;


    /**
     *  The user code given by the user..
     */
    private String mUserCode;


    public DeviceVerificationRequestHandlerSpiImpl(HttpSession session, String userCode)
    {
        mSession  = session;
        mUserCode = userCode;
    }


    @Override
    public String getUserCode()
    {
        return mUserCode;
    }


    @Override
    public Response onValid(DeviceVerificationResponse info)
    {
        // Ask the user to authorize the client.

        // Store some information to the user's session for later use.
        mSession.setAttribute("userCode",   mUserCode);
        mSession.setAttribute("claimNames", info.getClaimNames());
        mSession.setAttribute("acrs",       info.getAcrs());

        // The model for rendering the authorization page.
        DeviceAuthorizationPageModel model = new DeviceAuthorizationPageModel(info);

        // Create a response having the page.
        return ok(new Viewable(AUTHORIZATION_PAGE_TEMPLATE, model));
    }


    @Override
    public Response onExpired()
    {
        // Urge the user to re-initiate the device flow.
        return badRequest("The user Code Expired. Please re-initiate the flow again.");
    }


    @Override
    public Response onNotExist()
    {
        // Urge the user to re-input a valid user code.

        // The user.
        User user = (User)mSession.getAttribute("user");

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel()
            .setUserCode(mUserCode)
            .setUser(user)
            .setNotification("The user code does not exist.");

        // Return a response of "404 Not Found" having the verification page and
        // urge the user to re-input a valid user code.
        return notFound(new Viewable(VERIFICATION_PAGE_TEMPLATE, model));
    }


    @Override
    public Response onServerError()
    {
        // Urge the user to re-initiate device flow.
        return internalServerError("Server Error. Please re-initiate the flow again.");
    }
}
