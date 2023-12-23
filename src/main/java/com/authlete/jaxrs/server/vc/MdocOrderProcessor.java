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


import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.authlete.common.types.User;
import com.authlete.mdoc.constants.MDLClaimNames;
import com.authlete.mdoc.constants.MDLConstants;


class MdocOrderProcessor extends AbstractOrderProcessor
{
    private static final String KEY_CLAIMS   = "claims";
    private static final String KEY_DOC_TYPE = "doctype";
    private static final String KEY_FORMAT   = "format";


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

        // For each issuable credential.
        for (Map<String, Object> issuableCredential : issuableCredentials)
        {
            // If the format of the issuable credential does not match
            // the target format.
            if (!matchFormat(format, issuableCredential))
            {
                continue;
            }

            // If the document type of the issuable credential does not
            // match that of the requested credential.
            if (!matchDocType(issuableCredential, requestedCredential))
            {
                continue;
            }

            // If the claims of the issuable credentials includes the
            // claims of the requested credential.
            if (includeClaims(issuableCredential, requestedCredential))
            {
                // OK. The credential request is permitted.
                return;
            }
        }

        throw new InvalidCredentialRequestException(
                "The access token does not have permissions to request the credential.");
    }


    private static boolean matchFormat(
            String format, Map<String, Object> issuableCredential)
    {
        // The "format" in the issuable credential.
        String issuableCredentialFormat = (String)issuableCredential.get(KEY_FORMAT);

        return format.equals(issuableCredentialFormat);
    }


    private static boolean matchDocType(
            Map<String, Object> issuableCredential,
            Map<String, Object> requestedCredential)
    {
        // The document type of the issuable credential.
        Object issuableCredentialDocType = issuableCredential.get(KEY_DOC_TYPE);

        // The document type of the requested credential.
        Object requestedCredentialDocType = requestedCredential.get(KEY_DOC_TYPE);

        // If either or both are not strings.
        if (!(issuableCredentialDocType instanceof String) ||
            !(requestedCredentialDocType instanceof String))
        {
            return false;
        }

        return issuableCredentialDocType.equals(requestedCredentialDocType);
    }


    @SuppressWarnings("unchecked")
    private static boolean includeClaims(
            Map<String, Object> issuableCredential,
            Map<String, Object> requestedCredential)
    {
        // The claims in the issuable credential.
        Object issuableCredentialClaims = issuableCredential.get(KEY_CLAIMS);

        // The claims in the requested credential.
        Object requestedCredentialClaims = requestedCredential.get(KEY_CLAIMS);

        // If either or both are not maps.
        if (!(issuableCredentialClaims instanceof Map) ||
            !(requestedCredentialClaims instanceof Map))
        {
            return false;
        }

        // The expected structure of "claims" is as follows.
        //
        //   "namespace1": {
        //     "claimName1": "claimValue1"
        //   },
        //   "namespace": {
        //     "claimName2": "claimValue2"
        //   }
        //
        // This implementation checks only the first-level and the second-level
        // property names. In other words, this implementation checks only the
        // name spaces and their top-level claim names. Property names nested
        // deeper are not checked.
        return includeMap(
                (Map<String, Object>)issuableCredentialClaims,
                (Map<String, Object>)requestedCredentialClaims,
                /* deepest */ 2, /* depth */ 1);
    }


    @SuppressWarnings("unchecked")
    private static boolean includeMap(
            Map<String, Object> mapA, Map<String, Object> mapB,
            int deepest, int depth)
    {
        for (Map.Entry<String, Object> entryB : mapB.entrySet())
        {
            String keyB = entryB.getKey();
            Object valB = entryB.getValue();

            if (!mapA.containsKey(keyB))
            {
                // The mapB contains a key that the mapA does not.
                // The mapA does not include the mapB.
                return false;
            }

            // If it is not necessary to check nested properties.
            if (deepest == depth)
            {
                continue;
            }

            // If the entryB does not have nested properties.
            if (!(valB instanceof Map))
            {
                continue;
            }

            Object valA = mapA.get(keyB);

            // If the value in the mapA does not have a nested structure.
            if (!(valA instanceof Map))
            {
                // The mapB's entry has a nested structure, but the mapA's
                // entry does not. The mapA does not include the mapB.
                return false;
            }

            // Check if the mapA's nested map contains the mapB's nested map.
            boolean included = includeMap(
                    (Map<String, Object>)valA, (Map<String, Object>)valB,
                    deepest, (depth + 1));

            if (!included)
            {
                // The mapA's entry does not include the mapB's entry.
                return false;
            }
        }

        // The mapA contains all the top-level and nested properties
        // (up to the deepest level) of the mapB.
        return true;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> collectClaims(
            OrderContext context, User user, String format,
            Map<String, Object> requestedCredential) throws VerifiableCredentialException
    {
        // The document type of the requested credential.
        String docType = (String)requestedCredential.get(KEY_DOC_TYPE);

        // The requested claims.
        Map<String, Object> requestedClaims =
                (Map<String, Object>)requestedCredential.get(KEY_CLAIMS);

        // The user's claims for the document type.
        Map<String, Object> userClaims =
                (Map<String, Object>)user.getAttribute(docType);
        if (userClaims == null)
        {
            userClaims = Collections.emptyMap();
        }

        // Build claims
        Map<String, Object> claims = buildClaims(userClaims, requestedClaims);

        // In the case of mdoc, CredentialIssuanceOrder.credentialPayload
        // is required to have the following structure.
        //
        //   {
        //     "doctype": "{doctype}",
        //     "claims": {
        //       ...
        //     }
        //   }
        //
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(KEY_DOC_TYPE, docType);
        payload.put(KEY_CLAIMS,   claims);

        return payload;
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Object> buildClaims(
            Map<String, Object> userClaims, Map<String, Object> requestedClaims)
    {
        // The structure of userClaims and requestedClaims:
        //
        //   {
        //      "namespace1": {
        //        "claimName1": "claimValue1",
        //        ...
        //      },
        //      "namespace2": {
        //        "claimName2": "claimValue2",
        //        ...
        //      },
        //      ...
        //   }
        //

        Map<String, Object> claims = new LinkedHashMap<>();

        for (Map.Entry<String, Object> requestedNameSpace : requestedClaims.entrySet())
        {
            // The name space
            String nameSpace = requestedNameSpace.getKey();

            // If userClaims does not have the name space.
            if (!userClaims.containsKey(nameSpace))
            {
                continue;
            }

            // User claims under the name space.
            Object userSubclaims = userClaims.get(nameSpace);

            // Requested claims under the name space.
            Object requestedSubclaims = requestedNameSpace.getValue();

            if (!(userSubclaims instanceof Map) ||
                !(requestedSubclaims instanceof Map))
            {
                continue;
            }

            // Extract user subclaims that are requested.
            Map<String, Object> subclaims = buildSubclaims(
                    nameSpace,
                    (Map<String, Object>)userSubclaims,
                    (Map<String, Object>)requestedSubclaims);

            claims.put(nameSpace, subclaims);
        }

        return claims;
    }


    private static Map<String, Object> buildSubclaims(
            String nameSpace,
            Map<String, Object> userSubclaims,
            Map<String, Object> requestedSubclaims)
    {
        Map<String, Object> subclaims = new LinkedHashMap<>();

        // If the name space is "org.iso.18013.5.1".
        if (nameSpace.equals(MDLConstants.NAME_SPACE_MDL))
        {
            // Add special claims.
            addMDLClaims(subclaims, requestedSubclaims);
        }

        // Add requested claims to subclaims. Shallow copy.
        for (String claimName : requestedSubclaims.keySet())
        {
            if (userSubclaims.containsKey(claimName))
            {
                subclaims.put(claimName, userSubclaims.get(claimName));
            }
        }

        return subclaims;
    }


    private static void addMDLClaims(
            Map<String, Object> subclaims, Map<String, Object> requestedSubclaims)
    {
        // The current time.
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        // If the "issue_date" claim is requested.
        if (requestedSubclaims.containsKey(MDLClaimNames.ISSUE_DATE))
        {
            // "issue_date": 1004("YYYY-MM-DD")
            subclaims.put(MDLClaimNames.ISSUE_DATE, toFullDate(now));
        }

        // If the "expiry_date" claim is requested.
        if (requestedSubclaims.containsKey(MDLClaimNames.EXPIRY_DATE))
        {
            // The expiry date. There is no deep reason for "1 year" here.
            // This is just an example.
            ZonedDateTime exp = now.plusYears(1);

            // "expiry_date": "YYYY-MM-DD"
            subclaims.put(MDLClaimNames.EXPIRY_DATE, toFullDate(exp));
        }
    }


    private static String toFullDate(ZonedDateTime dt)
    {
        return String.format("cbor:1004(\"%s\")",
                dt.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }


    @Override
    protected long computeCredentialDuration()
    {
        // The current time.
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withNano(0);

        // The expiration datetime. There is no deep reason for "1 year" here.
        // This is just an example.
        ZonedDateTime exp = now.plusYears(1);

        // The seconds between the current time and the expiration datetime.
        return ChronoUnit.SECONDS.between(now, exp);
    }
}
