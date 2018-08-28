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
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import com.authlete.common.dto.Property;
import com.authlete.common.types.User;
import com.authlete.common.util.Utils;
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
     * The value of the "id_token" property in the "claims" request parameter
     * (or in the "claims" property in the request object) contained in the
     * original authorization request.
     */
    private Map<String, Object> mIdTokenClaims;


    /**
     * Requested ACRs.
     */
    private String[] mAcrs;


    /**
     * Constructor with a request from the form in the authorization page.
     *
     * <p>
     * This implementation uses {@code authorized}, {@code loginId} and
     * {@code password} in {@code parameters}.
     * </p>
     */
    public AuthorizationDecisionHandlerSpiImpl(
            MultivaluedMap<String, String> parameters, User user,
            Date userAuthenticatedAt, String idTokenClaims, String[] acrs)
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

        // The value of the "id_token" property in the "claims" request parameter
        // (or in the "claims" property in the request object) contained in the
        // original authorization request. See '5.5. Requesting Claims using the
        // "claims" Request Parameter' in OpenID Connect Core 1.0 for details.
        mIdTokenClaims = parseJson(idTokenClaims);

        // The requested ACRs.
        mAcrs = acrs;
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
        // First, check if the claim is a custom one.
        Object value = getCustomClaim(claimName, languageTag);

        // If the value for the custom claim was obtained.
        if (value != null)
        {
            // Return the value of the custom claim.
            return value;
        }

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


    @Override
    public String getAcr()
    {
        // Note that this is a dummy implementation. Regardless of whatever
        // the actual authentication was, this implementation returns the
        // first element of the requested ACRs if it is available.
        //
        // Of course, this implementation is not suitable for commercial use.

        if (mAcrs == null || mAcrs.length == 0)
        {
            return null;
        }

        // The first element of the requested ACRs.
        String acr = mAcrs[0];

        if (acr == null || acr.length() == 0)
        {
            return null;
        }

        // Return the first element of the requested ACRs. Again,
        // this implementation is not suitable for commercial use.
        return acr;
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json)
    {
        if (json == null)
        {
            return null;
        }

        try
        {
            return (Map<String, Object>)Utils.fromJson(json, Map.class);
        }
        catch (Exception e)
        {
            // Failed to parse the input as JSON.
            return null;
        }
    }


    private Object getCustomClaim(String claimName, String languageTag)
    {
        // Special behavior for Open Banking Profile.
        if ("openbanking_intent_id".equals(claimName))
        {
            // The Open Banking Profile requires that an authorization
            // request contains the "openbanking_intent_id" claim and
            // the authorization server embeds the value of the claim
            // in an ID token.
            return getValueFromIdTokenClaims(claimName);
        }

        return null;
    }


    private Object getValueFromIdTokenClaims(String claimName)
    {
        // Try to extract the entry for the claim from the "id_token"
        // property in the "claims" (which was contained in the original
        // authorization request).
        Map<String, Object> entry = getEntryFromIdTokenClaims(claimName);

        // If an entry for the claim is not available.
        if (entry == null)
        {
            // The value of the claim is not available.
            return null;
        }

        // This method expects that the entry has a "value" property.
        return entry.get("value");
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> getEntryFromIdTokenClaims(String claimName)
    {
        // If the original authorization request does not include
        // the "id_token" property in the "claims" request parameter
        // (or in the "claims" property in the request object).
        if (mIdTokenClaims == null)
        {
            // No entry for the claim.
            return null;
        }

        // Extract the entry for the claim from the "id_token" property.
        Object entry = mIdTokenClaims.get(claimName);

        // If the claim is not included.
        if (entry == null)
        {
            // No entry for the claim.
            return null;
        }

        // The expected format of a claim in the "id_token" property is
        // as follows. See '5.5. Requesting Claims using the "claims"
        // Request Parameter' in OpenID Connect Core 1.0 for details.
        //
        //   "claim_name" : {
        //       "essential": <boolean>,   // Optional
        //       "value":     <value>,     // Optional
        //       "values":    [<values>]   // Optional
        //   }
        //
        // Therefore, 'entry' should be able to be parsed as Map.

        if (!(entry instanceof Map))
        {
            // The format of the claim is invalid.
            return null;
        }

        // Found the entry for the claim.
        return (Map<String, Object>)entry;
    }
}
