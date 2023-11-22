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


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.authlete.common.types.User;


public class SdJwtOrderProcessor extends AbstractOrderProcessor
{
    private static final String KEY_CREDENTIAL_DEFINITION = "credential_definition";
    private static final String KEY_FORMAT                = "format";
    private static final String KEY_SUB                   = "sub";
    private static final String KEY_VCT                   = "vct";


    @SuppressWarnings("unchecked")
    @Override
    protected void checkPermissions(
            OrderContext context,
            List<Map<String, Object>> issuableCredentials,
            String format, Map<String, Object> requestedCredential)
                    throws InvalidCredentialRequestException
    {
        // If no issuable credential is associated with the access token.
        if (issuableCredentials == null)
        {
            throw new InvalidCredentialRequestException(
                    "No credential can be issued with the access token.");
        }

        // As explained in https://www.authlete.com/developers/oid4vci/,
        // it is challenging to implement this step in a manner consistent
        // across all implementations due to the flaws of the OID4VCI spec.

        // The implementation here follows "SD-JWT-based Verifiable Credentials"
        // as much as possible.
        //
        //   https://datatracker.ietf.org/doc/draft-ietf-oauth-sd-jwt-vc/

        // The requested credential must contain "credential_definition".
        Map<String, Object> credentialDefinition =
                extractCredentialDefinition(requestedCredential);

        // The "credential_definition" object must contain "vct".
        String vct = extractVct(credentialDefinition);

        // For each issuable credential.
        for (Map<String, Object> issuableCredential : issuableCredentials)
        {
            // The format of the issuable credential.
            String issuableCredentialFormat = (String)issuableCredential.get(KEY_FORMAT);

            // If the format of the requested credential is different from
            // the format of the issuable credential
            if (!format.equals(issuableCredentialFormat))
            {
                continue;
            }

            // The "credential_definition" in the issuable credential.
            Object value = issuableCredential.get(KEY_CREDENTIAL_DEFINITION);

            // If the "credential_definition" property is not available as a JSON object.
            if (!(value instanceof Map))
            {
                continue;
            }

            // The "vct" in the "credential_definition" object of the issuable credential.
            value = ((Map<String, Object>)value).get(KEY_VCT);

            // If the "type" property is not available as a string.
            if (!(value instanceof String))
            {
                continue;
            }

            // This implementation of the checkPermissions method is simple.
            // If "credential_definition.vct" of the requested credential
            // matches "credential_definition.vct" of any of the issuable
            // credentials, it is regarded that the credential request is
            // permitted.
            if (vct.equals(value))
            {
                // The credential request is permitted.
                return;
            }
        }

        throw new InvalidCredentialRequestException(
                "The access token does not have permissions to request the credential.");
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> extractCredentialDefinition(
            Map<String, Object> requestedCredential) throws InvalidCredentialRequestException
    {
        // If the requested credential does not contain "credential_definition".
        if (!requestedCredential.containsKey(KEY_CREDENTIAL_DEFINITION))
        {
            throw new InvalidCredentialRequestException(
                    "The credential request does not contain 'credential_definition'.");
        }

        // The value of the "credential_definition" property.
        Object value = requestedCredential.get(KEY_CREDENTIAL_DEFINITION);

        // If the value of the "credential_definition" property is not a JSON object.
        if (!(value instanceof Map))
        {
            throw new InvalidCredentialRequestException(
                    "The value of the 'credential_definition' property in the credential request is not a JSON object.");
        }

        return (Map<String, Object>)value;
    }


    private String extractVct(
            Map<String, Object> credentialDefinition) throws InvalidCredentialRequestException
    {
        // If the "credential_definition" does not contain "vct".
        if (!credentialDefinition.containsKey(KEY_VCT))
        {
            throw new InvalidCredentialRequestException(
                    "The 'credential_definition' object does not contain 'vct'.");
        }

        // The value of the "vct" property.
        Object value = credentialDefinition.get(KEY_VCT);

        // If the value of the "vct" property is not a string.
        if (!(value instanceof String))
        {
            throw new InvalidCredentialRequestException(
                    "The value of the 'vct' property in the 'credential_definition' object is not a string.");
        }

        return (String)value;
    }


    @Override
    protected Map<String, Object> collectClaims(
            OrderContext context, User user, String format,
            Map<String, Object> requestedCredential) throws VerifiableCredentialException
    {
        // The "credential_definition.vct" in the requested credential.
        @SuppressWarnings("unchecked")
        String vct = (String)((Map<String, Object>)requestedCredential.get(KEY_CREDENTIAL_DEFINITION)).get(KEY_VCT);

        // Find a CredentialDefinitionType having the vct.
        CredentialDefinitionType cdType = CredentialDefinitionType.byId(vct);

        if (cdType == null)
        {
            // The credential type is not supported.
            throw new UnsupportedCredentialTypeException(String.format(
                    "The credential type '%s' is not supported.", vct));
        }

        // For testing purposes, the credential issuance for a certain user
        // (subject = "1003", loginId = "max") is intentionally deferred.
        if (context != OrderContext.DEFERRED && user.getSubject().equals("1003"))
        {
            // Returning null from the collectClaims() method will result in
            // issuing a transaction ID instead of a verifiable credential.
            return null;
        }

        // Claims.
        Map<String, Object> claims = new LinkedHashMap<>();

        // "vct"
        claims.put(KEY_VCT, vct);

        // "sub"
        claims.put(KEY_SUB, user.getSubject());

        // The CredentialDefinitionType has a set of claims.
        // For each claim in the set.
        for (String claimName : cdType.getClaims())
        {
            // The value of the claim.
            Object claimValue = user.getClaim(claimName, null);

            // If the value of the claim is available.
            if (claimValue != null)
            {
                claims.put(claimName, claimValue);
            }
        }

        return claims;
    }
}
