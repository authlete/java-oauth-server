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


import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;

import com.authlete.common.dto.Property;
import com.authlete.common.types.User;
import com.authlete.jaxrs.spi.AuthorizationDecisionHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.AuthorizationDecisionHandlerSpi
 * AuthorizationDecisionHandlerSpi} interface which needs to be given
 * to the constructor of {@link com.authlete.jaxrs.AuthorizationDecisionHandler
 * AuthorizationDecisionHandler}.
 *
 * <p>
 * Note: The current implementation does not implement {@link #getAcr()} method.
 * </p>
 *
 * @author Takahiko Kawasaki
 */
class AuthorizationDecisionHandlerSpiImpl extends AuthorizationDecisionHandlerSpiAdapter
{
    /**
     * The flag to indicate whether the client application has been granted
     * permissions by the user.
     */
    private final boolean mClientAuthorized;


    /**
     * The authenticated user.
     */
    private User mUser;


    /**
     * The time when the user was authenticated in seconds since Unix epoch.
     */
    private long mUserAuthenticatedAt;


    /**
     * The subject (= unique identifier) of the user.
     */
    private String mUserSubject;


    /**
     * Constructor with a request from the form in the authorization page.
     *
     * <p>
     * This implementation uses {@code authorized}, {@code loginId} and
     * {@code password} in {@code parameters}.
     * </p>
     */
    public AuthorizationDecisionHandlerSpiImpl(MultivaluedMap<String, String> parameters, User user, Date userAuthenticatedAt)
    {
        // If the end-user clicked the "Authorize" button, "authorized"
        // is contained in the request.
        mClientAuthorized = parameters.containsKey("authorized");

        // If the end-user denied the authorization request.
        if (mClientAuthorized == false)
        {
            return;
        }

        // Look up an end-user who has the login credentials.
        mUser = user;

        // If nobody has the login credentials.
        if (mUser == null)
        {
            return;
        }

        // The authentication time is calculated externally and passed in.
        if (userAuthenticatedAt == null)
        {
            return;
        }

        // TODO: This should be passing in seconds to the API but we currently
        // need to pass in milliseconds to get the correct behavior.
        mUserAuthenticatedAt = userAuthenticatedAt.getTime() / 1000L;

        // The subject (= unique identifier) of the end-user.
        mUserSubject = mUser.getSubject();
    }


    @Override
    public boolean isClientAuthorized()
    {
        // True if the end-user has authorized the authorization request.
        return mClientAuthorized;
    }


    @Override
    public long getUserAuthenticatedAt()
    {
        // The time when the end-user was authenticated in seconds
        // since Unix epoch (1970-01-01).
        return mUserAuthenticatedAt;
    }


    @Override
    public String getUserSubject()
    {
        // The subject (= unique identifier) of the end-user.
        return mUserSubject;
    }


    @Override
    public Object getUserClaim(String claimName, String languageTag)
    {
        // getUserClaim() is called only when getUserSubject() has returned
        // a non-null value. So, mUser is not null when the flow reaches here.
        return mUser.getClaim(claimName, languageTag);
    }


    @Override
    public Property[] getProperties()
    {
        // Properties returned from this method will be associated with
        // an access token (in the case of "Implicit" flow") and/or an
        // authorization code (in the case of "Authorization Code" flow)
        // that may be issued as a result of the authorization request.
        return null;
    }
}
