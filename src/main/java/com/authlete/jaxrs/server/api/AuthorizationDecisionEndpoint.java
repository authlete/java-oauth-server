/*
 * Copyright (C) 2016-2018 Authlete, Inc.
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


import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BaseAuthorizationDecisionEndpoint;
import com.authlete.jaxrs.server.db.UserDao;


/**
 * The endpoint that receives a request from the form in the authorization page.
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/authorization/decision")
public class AuthorizationDecisionEndpoint extends BaseAuthorizationDecisionEndpoint
{
    /**
     * Process a request from the form in the authorization page.
     *
     * <p>
     * NOTE:
     * A better implementation would re-display the authorization page
     * when the pair of login ID and password is wrong, but this
     * implementation does not do it for brevity. A much better
     * implementation would check the login credentials by Ajax.
     * </p>
     *
     * @param request
     *         A request from the form in the authorization page.
     *
     * @param parameters
     *         Request parameters.
     *
     * @return
     *         A response to the user agent. Basically, the response
     *         will trigger redirection to the client's redirect
     *         endpoint.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session.
        HttpSession session = getSession(request);

        // Retrieve some variables from the session. See the implementation
        // of AuthorizationRequestHandlerSpiImpl.getAuthorizationPage().
        String   ticket        = (String)  takeAttribute(session, "ticket");
        String[] claimNames    = (String[])takeAttribute(session, "claimNames");
        String[] claimLocales  = (String[])takeAttribute(session, "claimLocales");
        String   idTokenClaims = (String)  takeAttribute(session, "idTokenClaims");
        String[] acrs          = (String[])takeAttribute(session, "acrs");
        User user              = getUser(session, parameters);
        Date authTime          = (Date)session.getAttribute("authTime");

        // Handle the end-user's decision.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new AuthorizationDecisionHandlerSpiImpl(
                        parameters, user, authTime, idTokenClaims, acrs),
                ticket, claimNames, claimLocales);
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
        String message = "A session does not exist.";

        Response response = Response
                .status(Status.BAD_REQUEST)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();

        throw new WebApplicationException(message, response);
    }


    /**
     * Look up an end-user.
     */
    private static User getUser(HttpSession session, MultivaluedMap<String, String> parameters)
    {
        // Look up the user in the session to see if they're already logged in.
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null)
        {
            return sessionUser;
        }

        // Look up an end-user who has the login credentials.
        User loginUser = UserDao.getByCredentials(parameters.getFirst("loginId"),
                parameters.getFirst("password"));

        if (loginUser != null)
        {
            session.setAttribute("user", loginUser);
            session.setAttribute("authTime", new Date());
        }

        return loginUser;
    }


    /**
     * Get the value of an attribute from the given session and
     * remove the attribute from the session after the retrieval.
     */
    private Object takeAttribute(HttpSession session, String key)
    {
        // Retrieve the value from the session.
        Object value = session.getAttribute(key);

        // Remove the attribute from the session.
        session.removeAttribute(key);

        // Return the value of the attribute.
        return value;
    }
}
