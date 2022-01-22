/*
 * Copyright (C) 2016-2019 Authlete, Inc.
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


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.dto.AuthorizationResponse;
import com.authlete.common.dto.Client;
import com.authlete.common.types.Prompt;
import com.authlete.common.types.SubjectType;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationDecisionHandler.Params;
import com.authlete.jaxrs.server.federation.FederationManager;
import com.authlete.jaxrs.spi.AuthorizationRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.AuthorizationRequestHandlerSpi
 * AuthorizationRequestHandlerSpi} interface which needs to be given
 * to the constructor of {@link com.authlete.jaxrs.AuthorizationRequestHandler
 * AuthorizationRequestHandler}.
 *
 * <p>
 * Note: The current implementation implements only {@link
 * #generateAuthorizationPage(AuthorizationResponse) generateAuthorizationPage()}
 * method. Other methods need to be implemented only when you want to support
 * {@code prompt=none} in authorization requests. See <a href=
 * "http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest">3.1.2.1.
 * Authentication Request</a> in <a href=
 * "http://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core
 * 1.0</a> for details about {@code prompt=none}.
 * </p>
 *
 * @author Takahiko Kawasaki
 */
class AuthorizationRequestHandlerSpiImpl extends AuthorizationRequestHandlerSpiAdapter
{
    /**
     * {@code "text/html;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_HTML =
            MediaType.TEXT_HTML_TYPE.withCharset("UTF-8");


    /**
     * The page template to ask the resource owner for authorization.
     */
    private static final String TEMPLATE = "/authorization";


    /**
     * Authorization request to the authorization endpoint.
     */
    private final HttpServletRequest mRequest;


    /**
     * Client associated with the authorization request. (Filled in during authorization response.)
     */
    private Client mClient;


    /**
     * Constructor with an authorization request to the authorization endpoint.
     */
    public AuthorizationRequestHandlerSpiImpl(HttpServletRequest request)
    {
        mRequest = request;
    }


    @Override
    public Response generateAuthorizationPage(AuthorizationResponse info)
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Store some variables into the session so that they can be
        // referred to later in AuthorizationDecisionEndpoint.
        session.setAttribute("params", Params.from(info));
        session.setAttribute("acrs",   info.getAcrs());
        session.setAttribute("client", info.getClient());

        mClient = info.getClient(); // update the client in case we need it with a no-interaction response

        // Clear the current user information in the session if necessary.
        clearCurrentUserInfoInSessionIfNecessary(info, session);

        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        // Prepare a model object which contains information needed to
        // render the authorization page.
        AuthzPageModel model = new AuthzPageModel(info, user,
                FederationManager.getInstance().getConfigurations());

        // Prepare another model object which contains information only
        // from the AuthorizationResponse instance. This model will be
        // used in FederationEndpoint if the end-user chooses to use an
        // external OpenID Provider at the authorization page.
        AuthzPageModel model2 = new AuthzPageModel(info, null, null);
        session.setAttribute("authzPageModel", model2);

        // Create a Viewable instance that represents the authorization
        // page. Viewable is a class provided by Jersey for MVC.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MEDIA_TYPE_HTML).build();
    }


    @Override
    public boolean isUserAuthenticated()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        // If the user information exists in the session, the user is already
        // authenticated; Otherwise, the user is not authenticated.
        return user != null;
    }


    @Override
    public long getUserAuthenticatedAt()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        Date authTime = (Date)session.getAttribute("authTime");

        if (authTime == null)
        {
            return 0;
        }

        return authTime.getTime() / 1000L;
    }


    @Override
    public String getUserSubject()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        if (user == null)
        {
            return null;
        }

        return user.getSubject();
    }


    private void clearCurrentUserInfoInSessionIfNecessary(AuthorizationResponse info, HttpSession session)
    {
        // Get the user from the session if they exist.
        User user     = (User)session.getAttribute("user");
        Date authTime = (Date)session.getAttribute("authTime");

        if (user == null || authTime == null)
        {
            // The information about the user does not exist in the session.
            return;
        }

        // Check 'prompts'.
        checkPrompts(info, session);

        // Check 'authentication age'.
        checkAuthenticationAge(info, session, authTime);
    }


    private void checkPrompts(AuthorizationResponse info, HttpSession session)
    {
        if (info.getPrompts() == null)
        {
            return;
        }

        List<Prompt> prompts = Arrays.asList(info.getPrompts());

        if (prompts.contains(Prompt.LOGIN))
        {
            // Force a login by clearing out the current user.
            clearCurrentUserInfoInSession(session);
        };
    }


    private void checkAuthenticationAge(AuthorizationResponse info, HttpSession session, Date authTime)
    {
        // TODO: max_age == 0 effectively means "log in the user interactively
        // now" but it's used here as a flag, we should fix this to use Integer
        // instead of int probably.
        if (info.getMaxAge() <= 0)
        {
            return;
        }

        Date now = new Date();

        // Calculate number of seconds that have elapsed since login.
        long authAge = (now.getTime() - authTime.getTime()) / 1000L;

        if (authAge > info.getMaxAge())
        {
            // Session age is too old, clear out the current user.
            clearCurrentUserInfoInSession(session);
        };
    }


    private void clearCurrentUserInfoInSession(HttpSession session)
    {
        session.removeAttribute("user");
        session.removeAttribute("authTime");
    }


    @Override
    public String getSub()
    {
        if (mClient != null &&
                mClient.getSubjectType() == SubjectType.PAIRWISE)
        {
            // it's a pairwise subject, calculate it here

            String sectorIdentifier = mClient.getDerivedSectorIdentifier();

            return mClient.getSubjectType().name() + "-" + sectorIdentifier + "-" + getUserSubject();
        }
        else
        {
            return null;
        }
    }
}
