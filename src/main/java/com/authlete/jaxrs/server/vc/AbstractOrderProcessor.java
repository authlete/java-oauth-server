/*
 * Copyright (C) 2023 Authlete, Inc.
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
package com.authlete.jaxrs.server.vc;


import java.util.List;
import java.util.Map;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;
import com.google.gson.Gson;


public abstract class AbstractOrderProcessor implements OrderProcessor
{
    @SuppressWarnings("unchecked")
    @Override
    public CredentialIssuanceOrder toOrder(
            OrderContext context,
            IntrospectionResponse introspection,
            CredentialRequestInfo info) throws VerifiableCredentialException
    {
        // See "Credential Issuance Order"
        //
        //  3.5.1. Credential Issuance Order
        //  https://www.authlete.com/developers/oid4vci/#351-credential-issuance-order
        //

        // === Step 1 ===
        //
        // Get the subject (= unique identifier) of the user associated
        // with the access token from the access token information.
        String subject = introspection.getSubject();

        // === Step 2 ===
        //
        // Retrieve information about the user identified by the subject
        // from the user database.
        User user = UserDao.getBySubject(subject);

        // === Step 3 ===
        //
        // Get the information about the issuable credentials associated
        // with the access token from the access token information.
        List<Map<String, Object>> issuableCredentials =
                parseJson(introspection.getIssuableCredentials(), List.class);

        // === Step 4 ===
        //
        // Get the credential information included in the credential request
        // from the credential request information.
        Map<String, Object> requestedCredential =
                parseJson(info.getDetails(), Map.class);

        // === Step 5 ===
        //
        // Confirm that the access token has the necessary permissions for
        // the credential request.
        checkPermissions(context, issuableCredentials, info.getFormat(), requestedCredential);

        // === Step 6 ===
        //
        // Determine the set of user claims to embed in the VC being issued
        // based on the credential information, and get the values of the
        // user claims from the dataset retrieved from the user database.
        Map<String, Object> claims =
                collectClaims(context, user, info.getFormat(), requestedCredential);

        // === Step 7 ===
        //
        // Build a credential issuance order using the collected data.
        CredentialIssuanceOrder order = createOrder(info, claims);

        // The credential issuance order.
        return order;
    }


    /**
     * Convert the given JSON to an instance of the specified Java class.
     */
    private <T> T parseJson(String json, Class<T> klass)
    {
        return new Gson().fromJson(json, klass);
    }


    /**
     * Create a credential issuance order.
     */
    private CredentialIssuanceOrder createOrder(
            CredentialRequestInfo info, Map<String, Object> claims)
    {
        String  payload  = (claims != null) ? new Gson().toJson(claims) : null;
        boolean deferred = (payload == null);

        return new CredentialIssuanceOrder()
                .setRequestIdentifier(info.getIdentifier())
                .setCredentialPayload(payload)
                .setIssuanceDeferred(deferred)
                .setCredentialDuration(computeCredentialDuration())
                ;
    }


    /**
     * Check whether the issuable credentials include the requested credential.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param format
     *         The credential format.
     *
     * @param requestedCredential
     *         The requested credential.
     *
     * @throws InvalidCredentialRequestException
     *         The issuable credentials do not include the requested credential,
     *         the content of the requested credential is invalid, or some other
     *         errors.
     */
    protected abstract void checkPermissions(
            OrderContext context,
            List<Map<String, Object>> issuableCredentials,
            String format, Map<String, Object> requestedCredential)
                    throws InvalidCredentialRequestException;


    /**
     * Collect the requested claims.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param user
     *         The user associated with the access token.
     *
     * @param format
     *         The credential format.
     *
     * @param requestedCredential
     *         The requested credential.
     *
     * @return
     *         The key-value pairs representing the requested claims.
     *         If null is returned, the credential issuance will be deferred.
     */
    protected abstract Map<String, Object> collectClaims(
            OrderContext context, User user, String format,
            Map<String, Object> requestedCredential) throws VerifiableCredentialException;


    /**
     * Compute the credential duration in seconds.
     *
     * <p>
     * The default implementation of this method returns 0, which tells
     * Authlete to try to generate a VC that does not expire. Subclasses
     * may override this method to set duration.
     * </p>
     *
     * @return
     *         The credential duration in seconds.
     */
    protected long computeCredentialDuration()
    {
        return 0;
    }
}
