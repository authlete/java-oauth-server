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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.mvc.Viewable;

import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BaseDeviceVerificationEndpoint;
import com.authlete.jaxrs.DeviceVerificationPageModel;
import com.authlete.jaxrs.server.db.UserDao;


/**
 * An implementation of OAuth 2.0 authorization endpoint with OpenID Connect support.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
 *      >RFC 6749, 3.1. Authorization Endpoint</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.1.2. Authorization Endpoint (Authorization Code Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#ImplicitAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.2.2. Authorization Endpoint (Implicit Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#HybridAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.3.2. Authorization Endpoint (Hybrid Flow)</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/device/verification")
public class DeviceVerificationEndpoint extends BaseDeviceVerificationEndpoint
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
     * The authorization endpoint for {@code GET} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MUST support {@code GET} method.
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
     *      >RFC 6749, 3.1 Authorization Endpoint</a>
     */
    @GET
    public Response get(
            @Context HttpServletRequest request,
            @Context UriInfo uriInfo)
    {
        // TODO: catch exception.

        // Get user information from the existing session if possible.
        // This may be null.
        User user = getUserFromSessionIfExist(request);

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel().setUser(user);

        // Create a Viewable instance that represents the verification page.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MEDIA_TYPE_HTML).build();
    }


    private User getUserFromSessionIfExist(HttpServletRequest request)
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
     * The authorization endpoint for {@code POST} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MAY support {@code POST} method.
     * </p>
     *
     * <p>
     * In addition, <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest"
     * >OpenID Connect Core 1.0, 3.1.2.1. Authentication Request</a> says
     * that the authorization endpoint MUST support {@code POST} method.
     * </p>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session or create new one.
        HttpSession session = request.getSession(true);

        // Get the user.
        User user = getUser(session, parameters);

        // Get the user code from the parameters.
        String userCode = parameters.getFirst("userCode");

        // Handle the verification request.
        return handle(session, user, userCode);
    }


    private User getUser(HttpSession session, MultivaluedMap<String, String> parameters)
    {
        // Look up the user in the session to see if they're already logged in.
        User sessionUser = (User)session.getAttribute("user");

        if (sessionUser != null)
        {
            // OK. The user has been already authenticated.
            return sessionUser;
        }

        // The user has not been authenticated yet. Then, check the user credentials
        // in the submitted parameters

        // Look up an end-user who has the login credentials.
        User loginUser = UserDao.getByCredentials(parameters.getFirst("loginId"),
                parameters.getFirst("password"));

        if (loginUser != null)
        {
            // OK. The user having the credentials was found.

            // Set the information about the user in the session.
            session.setAttribute("user", loginUser);

            return loginUser;
        }

        // Error. The user authentication has failed.
        // Urge the user to input valid login credentials again.
        throw createExceptionOnUserNotAuthenticated(parameters);
    }


    private WebApplicationException createExceptionOnUserNotAuthenticated(
            MultivaluedMap<String, String> parameters)
    {
        // The notification to be shown to the user on the verification page.
        String notification = "User authentication failed.";

        // The model for rendering the verification page.
        DeviceVerificationPageModel model = new DeviceVerificationPageModel()
            .setLoginId(parameters.getFirst("loginId"))
            .setUserCode(parameters.getFirst("loginId"))
            .setNotification(notification);

        // Create a Viewable instance that represents the verification page.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Make a response of "401 Unauthorized".
        Response response = Response
                .status(Status.UNAUTHORIZED)
                .entity(viewable)
                .type(MEDIA_TYPE_HTML)
                .build();

        return new WebApplicationException(response);
    }


    /**
     * Handle the device verification request.
     */
    private Response handle(HttpSession session, User user, String userCode)
    {
        System.out.println(userCode);
        return handle(AuthleteApiFactory.getDefaultApi(),
                new DeviceVerificationRequestHandlerSpiImpl(session, user, userCode));
    }
}
