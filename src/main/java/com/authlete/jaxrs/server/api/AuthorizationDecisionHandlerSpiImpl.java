/*
 * Copyright (C) 2016-2025 Authlete, Inc.
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.assurance.VerifiedClaims;
import com.authlete.common.assurance.constraint.VerifiedClaimsConstraint;
import com.authlete.common.dto.Client;
import com.authlete.common.dto.Property;
import com.authlete.common.types.SubjectType;
import com.authlete.common.types.User;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.server.db.DatasetDao;
import com.authlete.jaxrs.server.db.VerifiedClaimsDao;
import com.authlete.jaxrs.server.util.ResponseUtil;
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
    // The pattern of "openbanking_intent_id".
    // See openbanking/AccountRequestsEndpoint.java in java-resource-server.
    private static final Pattern OPENBANKING_INTENT_ID_PATTERN
        = Pattern.compile("^([0-9]+):.*$");


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
     * Client associated with the request.
     */
    private Client mClient;


    /**
     * The session ID of the user's authentication session.
     */
    private String mSessionId;


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
            Date userAuthenticatedAt, String idTokenClaims, String[] acrs,
            Client client, String sessionId)
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

        // The client associated with the request.
        mClient = client;

        // The session ID of the user's authentication session.
        mSessionId = sessionId;
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
            return Utils.fromJson(json, Map.class);
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
            return getOpenBankingIntentIdFromIdTokenClaims(claimName);
        }

        if ("txn".equals(claimName)) {
            // txn claim as used in ConnectID Australia:
            // https://cdn.connectid.com.au/specifications/digitalid-identity-assurance-profile-06.html
            return UUID.randomUUID();
        }

        // If the name indicates that the claim is a transformed claim.
        // See "OpenID Connect Advanced Syntax for Claims (ASC) 1.0"
        // for details about transformed claims.
        if (claimName.startsWith(":"))
        {
            // The value of the transformed claim will be computed by
            // Authlete later. The value returned here is not so
            // important.
            return "placeholder";
        }

        return null;
    }


    private Object getOpenBankingIntentIdFromIdTokenClaims(String claimName)
    {
        // Get the value of "openbanking_intent_id" from "id_token" property
        // in the "claims" request parameter.
        Object intentId = getValueFromIdTokenClaims(claimName);

        // If the value of "openbanking_intent_id" is null.
        if (intentId == null)
        {
            throw badRequest("The value of 'openbanking_intent_id' is not available.");
        }

        // Validate the value of the intent ID.
        validateOpenBankingIntentId(intentId);

        // Return the validated intent ID.
        return intentId;
    }


    private void validateOpenBankingIntentId(Object value)
    {
        // If the type of "openbanking_intent_id" is not String.
        if (!(value instanceof String))
        {
            throw badRequest("The value of 'openbanking_intent_id' is not a string.");
        }

        String intentId = (String)value;

        // Matcher that checks whether the value of openbanking_intent_id
        // matches the pattern "{ClientId}:...".
        Matcher matcher = OPENBANKING_INTENT_ID_PATTERN.matcher(intentId);

        // If the openbanking_intent_id does not match the pattern.
        if (!matcher.matches())
        {
            // No validation on the value.
            return;
        }

        // The client ID embedded in the openbanking_intent_id.
        String clientId = matcher.group(1);

        // If the client ID embedded in the openbanking_intent_id matches
        // the ID of the client that has made the authorization request.
        if (clientId.equals(String.valueOf(mClient.getClientId())))
        {
            // OK. The intent ID is being used by the legitimate client.
            return;
        }

        throw badRequest("The 'openbanking_intent_id' is not for the client.");
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


    @Override
    public String getSub()
    {
        if (mClient.getSubjectType() == SubjectType.PAIRWISE)
        {
            // it's a pairwise subject, calculate it here

            String sectorIdentifier = mClient.getDerivedSectorIdentifier();

            return mClient.getSubjectType().name() + "-" + sectorIdentifier + "-" + mUserSubject;
        }
        else
        {
            return null;
        }
    }


    @Override
    public List<VerifiedClaims> getVerifiedClaims(String subject, VerifiedClaimsConstraint constraint)
    {
        // This method, getVerifiedClaims(String, VerifiedClaimsConstraint),
        // is no longer called since authlete-java-jaxrs 2.42 unless the
        // 'oldIdaFormatUsed' flag of AuthorizationDecisionHandler.Params is on.
        // Instead, getVerifiedClaims(String, Object) is called.

        // The third Implementer's Draft of OpenID Connect for Identity
        // Assurance 1.0 (which was published in September 2021) has introduced
        // many breaking changes. In addition, it is scheduled that the next
        // draft will introduce further breaking changes. The specification is
        // still unstable. It turned out to be inadequate to define Java classes
        // that correspond to data structures of elements under "verified_claims".
        // In that sense, the classes under com.authlete.common.assurance package
        // of the authlete-java-common library are no longer useful.
        //
        // Authlete 2.3 has implemented a different approach for ID3 and future
        // drafts of OIDC4IDA that is less susceptible to specification changes.

        return VerifiedClaimsDao.get(subject, constraint);
    }


    private WebApplicationException badRequest(String description)
    {
        // The body of the response.
        String content = String.format(
                "{\"error\":\"invalid_request\", \"error_description\":\"%s\"}", description);

        // A response with 400 Bad Request and application/json.
        Response response = ResponseUtil.badRequest(content);

        return new WebApplicationException(response);
    }


    @Override
    public Object getVerifiedClaims(String subject, Object verifiedClaimsRequest)
    {
        // The list of available datasets of the subject.
        List<Map<String, Object>> datasets = DatasetDao.get(subject);

        // Build the content of "verified_claims" which meets conditions
        // of the request from the available datasets.
        return new VerifiedClaimsBuilder(verifiedClaimsRequest, datasets).build();
    }


    @Override
    public String getSessionId()
    {
        return mSessionId;
    }
}
