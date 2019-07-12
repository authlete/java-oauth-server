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


import static com.authlete.jaxrs.server.util.ResponseUtil.ok;
import static com.authlete.jaxrs.server.util.ExceptionUtil.unauthorizedException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BaseDeviceVerificationEndpoint;
import com.authlete.jaxrs.DeviceVerificationPageModel;
import com.authlete.jaxrs.server.db.UserDao;


/**
 * An implementation of verification endpoint of OAuth 2.0 Device Authorization
 * Grant (Device Flow).
 *
 * @author Hideki Ikeda
 */
@Path("/api/device/verification")
public class DeviceVerificationEndpoint extends BaseDeviceVerificationEndpoint
{
    /**
     * The page template to ask the end-user for a user code.
     */
    private static final String TEMPLATE = "/device/verification";


    /**
     * The value for {@code WWW-Authenticate} header on 401 Unauthorized.
     */
    private static final String CHALLENGE = "Basic realm=\"device/verification\"";


    /**
     * The verification endpoint for {@code GET} method. This method returns a
     * verification page where the end-user is asked to input her login credentials
     * (if not authenticated) and a user code.
     */
    @GET
    public Response get(
            @Context HttpServletRequest request,
            @Context UriInfo uriInfo)
    {
        // Get user information from the existing session if present.
        User user = getUserFromSessionIfPresent(request);

        // Get the user code from the query parameters if present.
        String userCode = uriInfo.getQueryParameters().getFirst("user_code");

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel()
            .setUser(user)
            .setUserCode(userCode);

        // Create a response of "200 OK" having the verification page.
        return ok(new Viewable(TEMPLATE, model));
    }


    private User getUserFromSessionIfPresent(HttpServletRequest request)
    {
        // Get the existing session.
        HttpSession session = request.getSession(false);

        if (session == null)
        {
            // No existing session.
            return null;
        }

        // Get the user from the existing session. This may be null.
        return (User)session.getAttribute("user");
    }


    /**
     * The verification endpoint for {@code POST} method. This method receives a
     * request from the form in the verification page.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session or create a new one.
        HttpSession session = request.getSession(true);

        // Authenticate the user.
        authenticateUser(session, parameters);

        // Get the user code from the parameters.
        String userCode = parameters.getFirst("userCode");

        // Handle the verification request.
        return handle(session, userCode);
    }


    private void authenticateUser(HttpSession session, MultivaluedMap<String, String> parameters)
    {
        // Look up the user in the session to see if they're already logged in.
        User sessionUser = (User)session.getAttribute("user");

        if (sessionUser != null)
        {
            // OK. The user has been already authenticated.
            return;
        }

        // The user has not been authenticated yet. Then, check the user credentials
        // in the submitted parameters

        // Look up an end-user who has the login credentials.
        User loginUser = UserDao.getByCredentials(parameters.getFirst("loginId"),
                parameters.getFirst("password"));

        if (loginUser != null)
        {
            // OK. The user having the credentials was found.

            // Set the login information about the user in the session.
            session.setAttribute("user", loginUser);
            session.setAttribute("authTime", new Date());

            return;
        }

        // Error. The user authentication has failed.
        // Urge the user to input valid login credentials again.

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel()
            .setLoginId(parameters.getFirst("loginId"))
            .setUserCode(parameters.getFirst("userCode"))
            .setNotification("User authentication failed.");

        // Throw a "401 Unauthorized" exception and show the verification page.
        throw unauthorizedException(new Viewable(TEMPLATE, model), CHALLENGE);
    }


    /**
     * Handle the device verification request.
     */
    private Response handle(HttpSession session, String userCode)
    {
        return handle(AuthleteApiFactory.getDefaultApi(),
                new DeviceVerificationRequestHandlerSpiImpl(session, userCode));
    }
}
