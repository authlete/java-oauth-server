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


import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

import com.authlete.common.dto.AuthorizationResponse;
import com.authlete.common.types.Prompt;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationPageModel;
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
        session.setAttribute("ticket",       info.getTicket());
        session.setAttribute("claimNames",   info.getClaims());
        session.setAttribute("claimLocales", info.getClaimsLocales());

        // get the user from the session if they exist
        User user = (User) session.getAttribute("user");
        Date authTime = (Date) session.getAttribute("authTime");

        //System.err.println("USER: " + user);
        //System.err.println("Auth Time: " + authTime);
        //System.err.println("AuthorizationResponse: " + info.summarize());

        if (user != null && authTime != null)
        {
            // See if the user should be prompted for login anyway
            if (info.getPrompts() != null)
            {
                List<Prompt> prompts = Arrays.asList(info.getPrompts());

                // System.err.println("Prompts: " + prompts);

                if (prompts.contains(Prompt.LOGIN))
                {
                    // force a login by clearing out the current user

                    //System.err.println("XX Logged out from prompt");

                    user = null;
                    session.removeAttribute("user");
                    session.removeAttribute("authTime");
                }
            }

            // check the auth age to make sure this session isn't too old

            // TODO: max_age == 0 effectively means "log in the user interactively now" but it's used here as
            // a flag, we should fix this to use Integer instead of int probably
            if (info.getMaxAge() > 0)
            {
                Date now = new Date();

                // calculate number of seconds that have elapsed since login
                long authAge = (now.getTime() - authTime.getTime()) / 1000;

                if (authAge > info.getMaxAge())
                {
                    // session age is too old, clear out the current user

                    //System.err.println("XX Logged out from max_auth");

                    user = null;
                    session.removeAttribute("user");
                    session.removeAttribute("authTime");
                }
            }
        }

        // Prepare a model object which contains information needed to
        // render the authorization page. Feel free to create a subclass
        // of AuthorizationPageModel or define another different class
        // according to what you need in the authorization page.
        AuthorizationPageModel model = new AuthorizationPageModel(info, user);

        // Create a Viewable instance that represents the authorization
        // page. Viewable is a class provided by Jersey for MVC.
        Viewable viewable = new Viewable(TEMPLATE, model);

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MEDIA_TYPE_HTML).build();
    }


    /* (non-Javadoc)
     * @see com.authlete.jaxrs.spi.AuthorizationRequestHandlerSpiAdapter#isUserAuthenticated()
     */
    @Override
    public boolean isUserAuthenticated()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // get the user from the session if they exist
        User user = (User) session.getAttribute("user");

        if (user != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /* (non-Javadoc)
     * @see com.authlete.jaxrs.spi.AuthorizationRequestHandlerSpiAdapter#getUserAuthenticatedAt()
     */
    @Override
    public long getUserAuthenticatedAt()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // get the user from the session if they exist
        Date authTime = (Date) session.getAttribute("authTime");

        if (authTime != null)
        {
            return authTime.getTime() / 1000L;
        }
        else
        {
            return 0;
        }
    }


    /* (non-Javadoc)
     * @see com.authlete.jaxrs.spi.AuthorizationRequestHandlerSpiAdapter#getUserSubject()
     */
    @Override
    public String getUserSubject()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // get the user from the session if they exist
        User user = (User) session.getAttribute("user");

        if (user != null)
        {
            return user.getSubject();
        }
        else
        {
            return null;
        }
    }
}
