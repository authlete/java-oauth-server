/*
 * Copyright (C) 2023-2026 Authlete, Inc.
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
import java.util.Objects;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;
import com.google.gson.Gson;


/**
 * A base class for {@link OrderProcessor} implementations.
 *
 * <p>
 * The OpenID for Verifiable Credential Issuance 1.0 (OID4VCI 1.0) specification
 * introduced numerous breaking changes throughout its development process. Even
 * the content of the credential request message body—arguably a core part of
 * the specification—shifted repeatedly during discussions and went through
 * breaking changes.
 * </p>
 *
 * <p>
 * A major flaw in OID4VCI 1.0 Implementer's Draft 1 (ID1) was that, there are
 * cases where the credential configuration could not be identified solely from
 * the content of a credential request. Since multiple credential configurations
 * may share the same {@code format}, specifying only the {@code format} can be
 * insufficient to uniquely determine the credential configuration. Nevertheless,
 * under OID4VCI 1.0 ID1, there were situations in which the only available
 * approach was to infer the credential configuration from the {@code format}
 * parameter included in the credential request. This deficiency had been
 * repeatedly pointed out during the early stages of the OID4VCI 1.0
 * specification development discussions, yet OID4VCI 1.0 ID1 was released with
 * this flaw unresolved.
 * </p>
 *
 * <p>
 * In contrast, in the final version of OID4VCI 1.0, a credential request is
 * required to include either a credential configuration ID or a credential
 * identifier. In either case, the specified information allows the credential
 * configuration to be uniquely determined.
 * </p>
 *
 * <p>
 * Responses from the Authlete API are also affected by the breaking changes
 * described above.
 * </p>
 *
 * <p>
 * When a {@code Service} is configured to support OID4VCI 1.0 Final—that is,
 * when the {@code oid4vciVersion} property of the {@code Service} is set to
 * {@code "1.0"} or {@code "1.0-Final"}—either the
 * {@code credentialConfigurationId} property or the {@code credentialIdentifier}
 * property of {@link CredentialRequestInfo} will be populated. These properties
 * correspond to the {@code credential_configuration_id} parameter and the
 * {@code credential_identifier} parameter of the credential request, respectively.
 * </p>
 *
 * <p>
 * On the other hand, when a {@code Service} is configured to support OID4VCI 1.0
 * ID1—that is, when the {@code oid4vciVersion} property of the {@code Service}
 * is not set or is set to {@code "1.0-ID1"}—neither the
 * {@code credentialConfigurationId} property nor the {@code credentialIdentifier}
 * property of {@link CredentialRequestInfo} will be populated. (Note: Although
 * OID4VCI 1.0 ID1 defines a {@code credential_identifier} parameter in credential
 * requests, Authlete's implementation of OID4VCI 1.0 ID1 does not support this
 * parameter. Therefore, the {@code credentialIdentifier} property of
 * {@link CredentialRequestInfo} will not be set.)
 * </p>
 *
 * <p>
 * In OID4VCI 1.0 Final, a RAR object with {@code type=openid_credential} is now
 * required to include {@code credential_configuration_id}. As a result, it is
 * no longer possible to request a credential that is not associated with any
 * credential configuration. Consequently, all "Issuable Credentials" associated
 * with an access token issued by Authlete are always tied to exactly one
 * credential configuration. Therefore, when a {@code Service} supports OID4VCI
 * 1.0 Final, the {@code Map} representing an Issuable Credential always contains
 * the {@code credential_configuration_id} key. In addition, it always contains
 * {@code credential_identifiers} (an array).
 * </p>
 *
 * <p>
 * By contrast, under OID4VCI 1.0 ID1, a RAR object with
 * {@code type=openid_credential} may omit {@code credential_configuration_id}
 * (in which case it includes {@code format} instead). For such RAR objects,
 * the {@code Map} representing the corresponding Issuable Credential does not
 * contain the {@code credential_configuration_id} key.
 * </p>
 *
 * <p>
 * If the credential configuration ID is available, the abstract methods of
 * {@link AbstractOrderProcessor} can be implemented in a logical manner.
 * Otherwise, the implementation inevitably becomes a compromise. Please keep
 * this point in mind when reading the implementation of this class.
 * </p>
 */
public abstract class AbstractOrderProcessor implements OrderProcessor
{
    private static final String KEY_CREDENTIAL_CONFIGURATION_ID = "credential_configuration_id";
    private static final String KEY_CREDENTIAL_IDENTIFIERS      = "credential_identifiers";


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
        //Map<String, Object> details = parseJson(info.getDetails(), Map.class);

        // === Step 5 ===
        //
        // Confirm that the access token has the necessary permissions for
        // the credential request.
        checkPermissions(context, issuableCredentials, info);

        // === Step 6 ===
        //
        // Determine the set of user claims to embed in the VC being issued
        // based on the credential information, and get the values of the
        // user claims from the dataset retrieved from the user database.
        Map<String, Object> claims =
                collectClaims(context, issuableCredentials, info, user);

        // === Step 7 ===
        //
        // Build a credential issuance order using the collected data.
        CredentialIssuanceOrder order = createOrder(info, claims);

        // The credential issuance order.
        return order;
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
     * Check whether the set of issuable credentials covers the credential
     * request.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @throws InvalidCredentialRequestException
     *         The issuable credentials do not cover the credential request,
     *         the content of the credential request is invalid, or some other
     *         errors.
     */
    private void checkPermissions(
            OrderContext context, List<Map<String, Object>> issuableCredentials,
            CredentialRequestInfo info) throws InvalidCredentialRequestException
    {
        // If no issuable credential is associated with the access token.
        if (issuableCredentials == null)
        {
            throw new InvalidCredentialRequestException(
                    "No credential can be issued with the access token.");
        }

        // If the Service is configured for OID4VCI 1.0 ID1.
        if (is10ID1(info))
        {
            checkPermissions10ID1(issuableCredentials, info);
        }
        else
        {
            checkPermissions10Final(issuableCredentials, info);
        }
    }


    /**
     * Check if the credential request information indicates that the Service
     * is configured for OID4VCI 1.0 ID1.
     */
    private static boolean is10ID1(CredentialRequestInfo info)
    {
        // If the Service is configured to support OID4VCI 1.0 Final,
        // neither credentialConfigurationId or credentialIdentifier
        // of the CredentialRequestInfo is set.
        //
        // NOTE: The credential_identifier request parameter has existed
        // since OID4VCI 1.0 ID1, but Authlete's implementation of
        // OID4VCI 1.0 ID1 does not support this request parameter.
        return info.getCredentialConfigurationId() == null &&
               info.getCredentialIdentifier()      == null;
    }


    /**
     * Check whether the set of issuable credentials covers the credential
     * request under the configuration of OID4VCI 1.0 ID1.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @throws InvalidCredentialRequestException
     *         The issuable credentials do not cover the credential request,
     *         the content of the credential request is invalid, or some other
     *         errors.
     */
    protected abstract void checkPermissions10ID1(
            List<Map<String, Object>> issuableCredentials, CredentialRequestInfo info)
                    throws InvalidCredentialRequestException;


    /**
     * Check whether the set of issuable credentials covers the credential
     * request under the configuration of OID4VCI 1.0 Final.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @throws InvalidCredentialRequestException
     *         The issuable credentials do not cover the credential request,
     *         the content of the credential request is invalid, or some other
     *         errors.
     */
    private void checkPermissions10Final(
            List<Map<String, Object>> issuableCredentials, CredentialRequestInfo info)
                    throws InvalidCredentialRequestException
    {
        // Either of the following is available.
        String credentialConfigurationId = info.getCredentialConfigurationId();
        String credentialIdentifier      = info.getCredentialIdentifier();

        // For each issuable credential.
        for (Map<String, Object> issuableCredential : issuableCredentials)
        {
            if (credentialConfigurationId != null &&
                hasCredentialConfigurationId(issuableCredential, credentialConfigurationId))
            {
                // OK. The access token has the permission to obtain a
                // credential identified by the credential configuration ID.
                return;
            }

            if (credentialIdentifier != null &&
                hasCredentialIdentifier(issuableCredential, credentialIdentifier))
            {
                // OK. The access token has the permission to obtain a
                // credential identified by the credential identifier.
                return;
            }
        }

        throw new InvalidCredentialRequestException(
                "The access token does not have permissions to request the credential.");
    }


    /**
     * Collect the requested claims.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @param user
     *         The user associated with the access token.
     *
     * @return
     *         The key-value pairs representing the requested claims.
     *         If null is returned, the credential issuance will be deferred.
     */
    private Map<String, Object> collectClaims(
            OrderContext context, List<Map<String, Object>> issuableCredentials,
            CredentialRequestInfo info, User user) throws VerifiableCredentialException
    {
        // If the Service is configured to support OID4VCI 1.0 ID1.
        if (is10ID1(info))
        {
            return collectClaims10ID1(context, issuableCredentials, info, user);
        }
        else
        {
            return collectClaims10Final(context, issuableCredentials, info, user);
        }
    }


    /**
     * Collect the requested claims under the configuration of OID4VCI 1.0 ID1.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @param user
     *         The user associated with the access token.
     *
     * @return
     *         The key-value pairs representing the requested claims.
     *         If null is returned, the credential issuance will be deferred.
     */
    protected abstract Map<String, Object> collectClaims10ID1(
            OrderContext context, List<Map<String, Object>> issuableCredentials,
            CredentialRequestInfo info, User user) throws VerifiableCredentialException;


    /**
     * Collect the requested claims under the configuration of OID4VCI 1.0 Final.
     *
     * @param context
     *         The context in which this order processor is executed.
     *
     * @param issuableCredentials
     *         The issuable credentials associated with the access token.
     *
     * @param info
     *         The credential request information.
     *
     * @param user
     *         The user associated with the access token.
     *
     * @return
     *         The key-value pairs representing the requested claims.
     *         If null is returned, the credential issuance will be deferred.
     */
    protected abstract Map<String, Object> collectClaims10Final(
            OrderContext context, List<Map<String, Object>> issuableCredentials,
            CredentialRequestInfo info, User user) throws VerifiableCredentialException;


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


    /**
     * Convert the given JSON to an instance of the specified Java class.
     */
    static <T> T parseJson(String json, Class<T> klass)
    {
        return new Gson().fromJson(json, klass);
    }


    static Map<String, Object> findMatchingIssuableCredential(
            List<Map<String, Object>> issuableCredentials, CredentialRequestInfo info)
    {
        // Either of the following should be available.
        String credentialConfigurationId = info.getCredentialConfigurationId();
        String credentialIdentifier      = info.getCredentialIdentifier();

        // For each issuable credential.
        for (Map<String, Object> issuableCredential : issuableCredentials)
        {
            if (credentialConfigurationId != null)
            {
                if (hasCredentialConfigurationId(issuableCredential, credentialConfigurationId))
                {
                    return issuableCredential;
                }
            }

            if (credentialIdentifier != null)
            {
                if (hasCredentialIdentifier(issuableCredential, credentialIdentifier))
                {
                    return issuableCredential;
                }
            }
        }

        return null;
    }


    /**
     * Check whether the issuable credential contains the specified credential
     * configuration ID.
     */
    private static boolean hasCredentialConfigurationId(
            Map<String, Object> issuableCredential, String credentialConfigurationId)
    {
        return Objects.equals(
                issuableCredential.get(KEY_CREDENTIAL_CONFIGURATION_ID),
                credentialConfigurationId);
    }


    /**
     * Check whether the issuable credential contains the specified credential
     * identifier.
     */
    private static boolean hasCredentialIdentifier(
            Map<String, Object> issuableCredential, String credentialIdentifier)
    {
        List<?> identifiers = (List<?>)issuableCredential.get(KEY_CREDENTIAL_IDENTIFIERS);

        if (identifiers == null)
        {
            // This should not happen. When the Service is configured for
            // OID4VCI 1.0 Final, maps representing Issuable Credentials
            // should always contain the "credential_identifiers" key.
            return false;
        }

        for (Object identifier : identifiers)
        {
            if (Objects.equals(identifier, credentialIdentifier))
            {
                return true;
            }
        }

        return false;
    }
}
