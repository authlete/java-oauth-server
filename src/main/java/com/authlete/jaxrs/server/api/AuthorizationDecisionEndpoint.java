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
package com.authlete.jaxrs.server.api;


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
import com.authlete.jaxrs.BaseAuthorizationDecisionEndpoint;


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
        String   ticket       = (String)  takeAttribute(session, "ticket");
        String[] claimNames   = (String[])takeAttribute(session, "claimNames");
        String[] claimLocales = (String[])takeAttribute(session, "claimLocales");

        // Handle the end-user's decision.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new AuthorizationDecisionHandlerSpiImpl(parameters),
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
