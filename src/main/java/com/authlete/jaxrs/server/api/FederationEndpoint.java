/*
 * Copyright (C) 2022 Authlete, Inc.
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


import java.io.IOException;
import java.net.URI;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BaseEndpoint;
import com.authlete.jaxrs.server.db.UserDao;
import com.authlete.jaxrs.server.db.UserEntity;
import com.authlete.jaxrs.server.federation.Federation;
import com.authlete.jaxrs.server.federation.FederationManager;
import com.authlete.jaxrs.server.util.ResponseUtil;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;


@Path("/api/federation")
public class FederationEndpoint extends BaseEndpoint
{
    private static final MediaType MEDIA_TYPE_HTML =
            MediaType.TEXT_HTML_TYPE.withCharset("UTF-8");
    private static final String TEMPLATE = "/authorization";

    private static final String KEY_MODEL    = "authzPageModel";
    private static final String KEY_STATE    = "state";
    private static final String KEY_VERIFIER = "codeVerifier";


    @GET
    @Path("initiation/{federationId}")
    public Response initiation(
            @Context HttpServletRequest req,
            @PathParam("federationId") String federationId)
    {
        // Get the Federation instance that corresponds to the federation ID.
        Federation federation = getFederation(federationId);

        // Generate a state and a code verifier.
        String state    = new State().getValue();
        String verifier = new CodeVerifier().getValue();

        // Put them in the session so that callback() can use them later.
        putToSession(req, KEY_STATE,    state);
        putToSession(req, KEY_VERIFIER, verifier);

        // Build an authentication request that conforms to OpenID Connect.
        URI authenticationRequest =
                buildAuthenticationRequest(federation, state, verifier);

        // Redirect the web browser to the authorization endpoint of the
        // OpenID Provider. As a result, the web browser will send the
        // authentication request to the authorization endpoint.
        return redirectTo(authenticationRequest);
    }


    @GET
    @Path("callback/{federationId}")
    public Response callback(
            @Context HttpServletRequest req,
            @PathParam("federationId") String federationId)
    {
        // Authentication response from the OpenID Provider.
        URI authenticationResponse = getFullUri(req);

        // Get the Federation instance that corresponds to the federation ID.
        Federation federation = getFederation(federationId);

        // Data used to render the authorization page.
        AuthzPageModel model = getAuthzPageModel(req);

        // "state" and "code_verifier" which were generated in initiation().
        String state    = takeFromSession(req, KEY_STATE);
        String verifier = takeFromSession(req, KEY_VERIFIER);

        // Ensure that 'state' is available.
        ensureState(state);

        // Communicate with the OpenID Provider to get information about the user.
        UserInfo userInfo = getUserInfo(
                federation, authenticationResponse, state, verifier, model);

        // Register the user into this server (or overwrite the existing info).
        User user = registerUser(federation, userInfo);

        // Make the user login.
        makeUserLogin(req, user);

        // Go back to the authorization page.
        return authorizationPage(model, user, null);
    }


    private Federation getFederation(String federationId) throws WebApplicationException
    {
        // Get the Federation instance that corresponds to the federation ID.
        Federation federation =
                FederationManager.getInstance().getFederation(federationId);

        if (federation == null)
        {
            // 404 Not Found
            throw notFound("Unknown federation ID: " + federationId);
        }

        return federation;
    }


    private URI buildAuthenticationRequest(
            Federation federation, String state, String verifier) throws WebApplicationException
    {
        try
        {
            // Build an authentication request that conforms to OpenID Connect.
            return federation.createFederationRequest(state, verifier);
        }
        catch (IOException e)
        {
            throw internalServerError("Failed to build an authentication request: " + e.getMessage());
        }
    }


    private Response redirectTo(URI location)
    {
        // 302 Found
        // Location: {location}
        return Response.status(Status.FOUND).location(location).build();
    }


    private URI getFullUri(HttpServletRequest req)
    {
        StringBuffer url   = req.getRequestURL();
        String queryString = req.getQueryString();

        if (queryString != null)
        {
            url.append("?").append(queryString);
        }

        return URI.create(url.toString());
    }


    private AuthzPageModel getAuthzPageModel(HttpServletRequest req) throws WebApplicationException
    {
        AuthzPageModel model = getFromSession(req, KEY_MODEL);

        if (model == null)
        {
            // 400 Bad Request
            throw badRequest("Not in the context of an authorization flow.");
        }

        return model;
    }


    private void ensureState(String state) throws WebApplicationException
    {
        if (state == null || state.isEmpty())
        {
            // 400 Bad Request
            throw badRequest("Invalid state.");
        }
    }


    private UserInfo getUserInfo(
            Federation federation, URI authenticationResponse,
            String state, String verifier, AuthzPageModel model) throws WebApplicationException
    {
        try
        {
            // Send a token request with the authorization code and the code
            // verifier to the token endpoint of the OpenID Provider and
            // receive an ID token and an access token.
            //
            // Access the userinfo endpoint of the OpenID Provider with the
            // access token and receive information about the end-user.
            //
            // Necessary validation steps (such as checking the "state" and
            // verifying the signature of the ID token) will be executed in
            // processFederationResponse().
            return federation.processFederationResponse(
                    authenticationResponse, state, verifier);
        }
        catch (IOException e)
        {
            // The authorization page with an error message.
            Response page = authorizationPage(model, null,
                    "ID federation failed: " + e.getMessage());

            // Return the authorization page to the web browser.
            throw new WebApplicationException(page);
        }
    }


    private User registerUser(Federation federation, UserInfo userInfo)
    {
        // Create a user entity from the userinfo.
        UserEntity userEntity = createUserEntity(federation, userInfo);

        // Register (or overwrite) the user.
        UserDao.add(userEntity);

        return userEntity;
    }


    private static UserEntity createUserEntity(Federation federation, UserInfo userInfo)
    {
        // The subject of the user.
        String subject = String.format("%s@%s",
                userInfo.getSubject(), federation.getConfiguration().getId());

        return new UserEntity(userInfo).setSubject(subject);
    }


    private void makeUserLogin(HttpServletRequest req, User user)
    {
        putToSession(req, "user",     user);
        putToSession(req, "authTime", new Date());
    }


    private Response authorizationPage(AuthzPageModel model, User user, String message)
    {
        model.setUser(user);
        model.setFederations(FederationManager.getInstance().getConfigurations());
        model.setFederationMessage(message);

        // Create a Viewable instance that represents the authorization page.
        // Viewable is a class provided by Jersey for MVC.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MEDIA_TYPE_HTML).build();
    }


    @SuppressWarnings("unchecked")
    private <T> T getFromSession(HttpServletRequest req, String key)
    {
        HttpSession session = req.getSession();

        if (session == null)
        {
            return null;
        }

        return (T)session.getAttribute(key);
    }


    private void putToSession(HttpServletRequest req, String key, Object value)
    {
        HttpSession session = req.getSession(true);

        session.setAttribute(key, value);
    }


    @SuppressWarnings("unchecked")
    private <T> T takeFromSession(HttpServletRequest req, String key)
    {
        HttpSession session = req.getSession();

        if (session == null)
        {
            return null;
        }

        return (T)takeAttribute(session, key);
    }


    private WebApplicationException badRequest(String message)
    {
        // 400 Bad Request
        return new WebApplicationException(ResponseUtil.badRequest(message));
    }


    private WebApplicationException notFound(String message)
    {
        // 404 Not Found
        return new WebApplicationException(ResponseUtil.notFound(message));
    }


    private WebApplicationException internalServerError(String message)
    {
        // 500 Internal Server Error
        return new WebApplicationException(ResponseUtil.internalServerError(message));
    }
}
