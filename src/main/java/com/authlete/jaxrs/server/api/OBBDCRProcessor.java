/*
 * Copyright (C) 2021 Authlete, Inc.
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


import static com.authlete.jaxrs.server.api.OBBDCRConstants.CLIENT_AUTHENTICATION_METHODS;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.JWE_ALG_CLIENT_METADATA;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.JWE_ENC_CLIENT_METADATA;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.JWS_ALG_CLIENT_METADATA;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.RECOGNIZED_CLIENT_METADATA;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.ROLE_TO_SCOPES;
import static com.authlete.jaxrs.server.api.OBBDCRConstants.TLS_CLIENT_AUTH_SAN_CLIENT_METADATA;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.server.obb.util.ObbUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;


/**
 * A sample implementation of Client Registration Endpoint that
 * conforms to requirements of Open Banking Brasil.
 *
 * <p>
 * NOTE: It is not assured that this implementation is perfect.
 * There are no warranties even if you have troubles by using
 * and/or referencing this implementation.
 * </p>
 *
 * @see <a href="https://openbanking-brasil.github.io/specs-seguranca/open-banking-brasil-dynamic-client-registration-1_ID1.html"
 *      >Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0 Implementers Draft 1</a>
 */
public class OBBDCRProcessor
{
    // Named boolean values just for code readability.
    private static boolean OPTIONAL  = true;
    private static boolean REQUIRED  = false;
    private static boolean NULLABLE  = true;
    private static boolean NOT_NULL  = false;
    private static boolean FROM_SS   = true;
    private static boolean FROM_BODY = false;


    public Map<String, Object> process(HttpServletRequest request, String requestBody)
    {
        // Parse the request body.
        Map<String, Object> requestParams = parseRequestBody(requestBody);

        // Validate the software statement.
        SignedJWT softwareStatement = validateSoftwareStatement(requestParams);

        // Validate the client metadata.
        Map<String, Object> ssClaims = validateClientMetadata(requestParams, softwareStatement);

        // Merge the client metadata.
        return mergeClientMetadata(requestParams, ssClaims);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequestBody(String body)
    {
        // If the request has no body.
        if (body == null)
        {
            throw invalidRequest("The request has no body.");
        }

        Map<String, Object> params;

        try
        {
            // According to RFC 7591, the format of the body of a Client
            // Registration Request is JSON, so let's parse the request
            // body as JSON.
            //
            // FYI: In UK Open Banking, the format is JWT. In that sense, the
            // Client Registration Endpoint of UK Open Banking does not conform
            // to RFC 7591. See also:
            //
            //   [OpenBanking-Brasil/specs-seguranca] Issue 86
            //   Question : Should DCR payload be a JSON payload on the DCR request or JWS?
            //
            //     https://github.com/OpenBanking-Brasil/specs-seguranca/issues/86
            //
            params = Utils.fromJson(body, Map.class);
        }
        catch (Exception e)
        {
            // Failed to parse the request body as JSON.
            e.printStackTrace();

            throw invalidRequest("The request body is not JSON.");
        }

        return params;
    }


    private SignedJWT validateSoftwareStatement(Map<String, Object> params)
    {
        // Extract a software statement from the request.
        SignedJWT ss = extractSoftwareStatement(params);

        // Verify the signature of the software statement.
        verifySoftwareStatementSignature(ss);

        return ss;
    }


    private SignedJWT extractSoftwareStatement(Map<String, Object> params)
    {
        // Dynamic Client Registration Request in Open Banking Brasil must
        // include a software statement assertion that has been issued by
        // the Directory.
        //
        // The "software_statement" request parameter is defined in Section
        // 3.1.1 of RFC 7591.

        // If the request does not include "software_statement".
        if (!params.containsKey("software_statement"))
        {
            throw invalidRequest(
                    "The request body does not include the 'software_statement' parameter.");
        }

        // Extract the value of "software_statement".
        Object ss = params.get("software_statement");

        // If the value of "software_statement" is not a string.
        if (!(ss instanceof String))
        {
            throw invalidSoftwareStatement(
                    "The value of the 'software_statement' parameter is not a string.");
        }

        try
        {
            // Parse the value of "software_statement" as a signed JWT.
            return SignedJWT.parse((String)ss);
        }
        catch (ParseException e)
        {
            // Failed to parse "software_statement" as a signed JWT.
            e.printStackTrace();

            throw invalidSoftwareStatement(
                    "The value of the 'software_statement' parameter is not a signed JWT.");
        }
    }


    private void verifySoftwareStatementSignature(SignedJWT ss)
    {
        // Check if the signature algorithm of the software statement is permitted.
        checkSoftwareStatementSignatureAlgorithm(ss);

        // Get a verifier to verify the signature of the software statement.
        JWSVerifier verifier = getVerifierForSoftwareStatementSignature(ss);

        boolean verified;

        try
        {
            // Verify the signature of the software statement with the verifier.
            verified = ss.verify(verifier);
        }
        catch (JOSEException e)
        {
            // Failed to verify the signature of the software statement.
            e.printStackTrace();

            throw invalidSoftwareStatement(
                    "Failed to verify the signature of the software statement.");
        }

        if (verified == false)
        {
            throw invalidSoftwareStatement(
                    "The signature of the software statement is invalid.");
        }
    }


    private void checkSoftwareStatementSignatureAlgorithm(SignedJWT ss)
    {
        // The value of "alg" in the header. It represents the signature algorithm
        // of the JWT.
        JWSAlgorithm alg = ss.getHeader().getAlgorithm();

        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   2. shall validate that the request contains software_statement jwt
        //      signed using the PS256 algorithm issued by the Open Banking
        //      Brasil directory of participants;

        // The algorithm must be "PS256".
        if (alg != JWSAlgorithm.PS256)
        {
            throw invalidSoftwareStatement(
                    "The signature algorithm of the software statement is not 'PS256'.");
        }
    }


    private JWSVerifier getVerifierForSoftwareStatementSignature(SignedJWT ss)
    {
        // Get the JWK Set that contains the public key for signature verification.
        JWKSet jwkset = getJwkSetForSoftwareStatementSignatureVerification(ss);

        // Select a JWK from the JWK Set.
        JWK jwk = selectJwkForSoftwareStatementSignatureVerification(ss, jwkset);

        try
        {
            // Build a verifier from the JWK. Because Open Banking Brasil allows
            // PS256 only, verifiers are always RSSSAVerifier instances.
            return new RSASSAVerifier(((RSAKey)jwk).toRSAPublicKey());
        }
        catch (JOSEException e)
        {
            // Failed to build a verifier.
            e.printStackTrace();

            throw serverError(
                    "Failed to create a verifier to verify the signature of the software statement with.");
        }
    }


    private JWKSet getJwkSetForSoftwareStatementSignatureVerification(SignedJWT ss)
    {
        // Get the location of the JWK Set that contains the JWK whereby to verify
        // the signature of the software statement.
        String location = getDirectoryJwksLocation(ss);

        // Parameters for JWKSet.load() method.
        int connectTimeout = 10000; // in milliseconds
        int readTimeout    = 10000; // in milliseconds
        int sizeLimit      =     0; // in bytes

        try
        {
            // Fetch the JWK Set from the location.
            return JWKSet.load(new URL(location), connectTimeout, readTimeout, sizeLimit);
        }
        catch (IOException e)
        {
            // Failed to fetch the JWK Set.
            e.printStackTrace();

            throw serverError("Failed to fetch the JWK Set from '%s'.", location);
        }
        catch (ParseException e)
        {
            // Failed to parse the content as a JWK Set.
            e.printStackTrace();

            throw serverError("Failed to parse the content at '%s' as a JWK Set.", location);
        }
    }


    private String getDirectoryJwksLocation(SignedJWT ss)
    {
        // This system property allows developers to specify the location of
        // the JWK Set of the Directory for debugging and testing purposes.
        //
        // Developers can specify the system property like below when invoking
        // this server.
        //
        //   -Dobb.directory.jwks_uri=LOCATION_OF_JWK_SET
        //
        String location = System.getProperty("obb.directory.jwks_uri");

        if (location != null)
        {
            // If the system property is given, we use it as the location of
            // the JWK Set of the Directory.
            return location;
        }

        String environment = null;

        try
        {
            // Get the value of "software_environment" in the software statement.
            environment = ss.getJWTClaimsSet().getStringClaim("software_environment");
        }
        catch (Exception e)
        {
        }

        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 9.2. Open Banking Brasil SSA Key Store and Issuer Details
        //
        //   Production
        //     https://keystore.directory.openbankingbrasil.org.br/openbanking.jwks
        //     Open Banking Open Banking Brasil production SSA issuer
        //
        //   Sandbox
        //     https://keystore.sandbox.directory.openbankingbrasil.org.br/openbanking.jwks
        //     Open Banking Open Banking Brasil sandbox SSA issuer
        //

        // The example in the OBB DCR specification indicates that a software
        // statement contains a "software_environment" claim. In the example,
        // its value is "production".
        //
        // It is not explicitly written, but this implementation assumes that
        // "software_environment":"production" means that the issuer of the
        // software statement is the production SSA issuer.

        // In the case of "software_environment":"production".
        if (environment != null && environment.equals("production"))
        {
            // The location of the JWK Set of the production SSA issuer.
            return "https://keystore.directory.openbankingbrasil.org.br/openbanking.jwks";
        }
        // In other cases.
        else
        {
            // The location of the JWK Set of the sandbox SSA issuer.
            return "https://keystore.sandbox.directory.openbankingbrasil.org.br/openbanking.jwks";
        }
    }


    private JWK selectJwkForSoftwareStatementSignatureVerification(SignedJWT ss, JWKSet jwkset)
    {
        // Prepare a selector that selects a JWK from a given JWK Set.
        JWKMatcher  matcher  = JWKMatcher.forJWSHeader(ss.getHeader());
        JWKSelector selector = new JWKSelector(matcher);

        // Select JWKs that match the conditions from the JWK Set.
        List<JWK> jwks = selector.select(jwkset);

        if (jwks == null || jwks.size() == 0)
        {
            throw invalidSoftwareStatement(
                    "The JWK Set contains no JWK to verify the signature of the software statement with.");
        }

        if (1 < jwks.size())
        {
            throw invalidSoftwareStatement(
                    "The JWK Set contains multiple JWKs to verify the signature of the software statement with.");
        }

        return jwks.get(0);
    }


    private Map<String, Object> validateClientMetadata(
            Map<String, Object> requestParams, SignedJWT softwareStatement)
    {
        // Extract the payload part of the software statement.
        Map<String, Object> ssClaims = extractClaimsFromSoftwareStatement(softwareStatement);

        // Perform validation specific to Open Banking Brasil.

        // OBB DCR
        validateIat(requestParams, ssClaims);
        validateJwks(requestParams, ssClaims);
        validateJwksUri(requestParams, ssClaims);
        validateRedirectUris(requestParams, ssClaims);
        validateClientAuthenticationMethod(requestParams, ssClaims);
        validateRequestObjectEncryption(requestParams, ssClaims);
        validateScopesWithRoles(requestParams, ssClaims);
        validateClientAuthSubject(requestParams, ssClaims);

        // OBB FAPI
        validateJwsAlg(requestParams, ssClaims);
        validateJweAlg(requestParams, ssClaims);
        validateJweEnc(requestParams, ssClaims);

        return ssClaims;
    }


    private Map<String, Object> extractClaimsFromSoftwareStatement(SignedJWT ss)
    {
        try
        {
            // Get the payload part of the software statement as Map.
            return ss.getJWTClaimsSet().getClaims();
        }
        catch (Exception e)
        {
            // Failed to get the payload part of the software statement.
            e.printStackTrace();

            throw invalidSoftwareStatement(
                    "Failed to extract claims from the software statement.");
        }
    }


    private void validateIat(Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   3. shall validate that the software_statement was issued (iat) not
        //      more than 5 minutes prior to the request being received;

        // Extract the value of the "iat" claim from the software statement.
        Date iat  = extractAsDate(ssClaims, "iat", REQUIRED, NOT_NULL, FROM_SS);
        long iat_ = iat.getTime();

        // The difference between the current time and the 'iat' in milliseconds.
        //
        // According to RFC 7519, the value of the "iat" claim is NumericDate
        // which is "A JSON numeric value representing the number of seconds
        // from 1970-01-01T00:00:00Z UTC until the specified UTC date/time,
        // ignoring leap seconds."
        //
        // Date.getTime() returns the number of milliseconds elapsed since the
        // Unix epoch.
        //
        Date now  = new Date();
        long now_ = now.getTime();
        long diff = now_ - iat_;

        if (diff < 0L)
        {
            throw invalidSoftwareStatement(
                    "The issue time of the software statement is pointing to the future: now=%s(%d), iat=%s(%d)",
                    ObbUtils.formatDate(now), now_, ObbUtils.formatDate(iat), iat_);
        }

        if (300000L < diff)
        {
            throw invalidSoftwareStatement(
                    "More than 5 minutes have passed since the issue time of the software statement: now=%s(%d), iat=%s(%d)",
                    ObbUtils.formatDate(now), now_, ObbUtils.formatDate(iat), iat_);
        }
    }


    private void validateJwks(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   4. shall validate that a jwks (key set by value) was not included;

        // If the request body contains "jwks".
        if (requestParams.containsKey("jwks"))
        {
            throw invalidClientMetadata(
                    "The request body contains a 'jwks' parameter.");
        }

        // If the software statement contains "jwks".
        if (ssClaims.containsKey("jwks"))
        {
            throw invalidClientMetadata(
                    "The software statement contains a 'jwks' claim.");
        }
    }


    private void validateJwksUri(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   5. shall require and validate that the jwks_uri matches the
        //      software_jwks_uri provided in the software statement;

        // Extract "jwks_uri" from the request body.
        String jwksUri = extractAsString(
                requestParams, "jwks_uri", REQUIRED, NOT_NULL, FROM_BODY);

        // Extract "software_jwks_uri" from the software statement.
        String softwareJwksUri = extractAsString(
                ssClaims, "software_jwks_uri", REQUIRED, NOT_NULL, FROM_SS);

        // If "jwks_uri" and "software_jwks_uri" hold the same value.
        if (jwksUri.equals(softwareJwksUri))
        {
            // Okay.
            return;
        }

        throw invalidClientMetadata(
                "The value of 'jwks_uri' and the value of 'software_jwks_uri' do not match.");
    }


    private void validateRedirectUris(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   6. shall require and validate that redirect_uris match or contain a sub
        //      set of software_redirect_uris provided in the software statement;

        // Extract "redirect_uris" from the request body.
        List<String> redirectUris = extractAsStringList(
                requestParams, "redirect_uris", REQUIRED, NOT_NULL, FROM_BODY);

        // Extract "software_redirect_uris" from the software statement.
        List<String> softwareRedirectUris = extractAsStringList(
                ssClaims, "software_redirect_uris", REQUIRED, NOT_NULL, FROM_SS);

        // Convert the list of redirect URIs into a Set instance for faster lookup.
        Set<String> softwareRedirectUriSet = new HashSet<>(softwareRedirectUris);

        for (int i = 0; i < redirectUris.size(); i++)
        {
            // If the value in 'redirect_uris' is included in 'software_redirect_uris'.
            if (softwareRedirectUriSet.contains(redirectUris.get(i)))
            {
                // Okay.
                continue;
            }

            throw invalidRedirectUri(
                    "The 'software_redirect_uris' claim in the software statement " +
                    "does not include the value at the '%d' index of the " +
                    "'redirect_uris' parameter in the request body.", i);
        }
    }


    private void validateClientAuthenticationMethod(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   7. shall require and validate that all client authentication mechanism
        //      adhere to the requirements defined in Financial-grade API Security
        //      Profile 1.0 - Part 2: Advanced;

        // In the context of FAPI 1.0 Advanced, permitted client authentication
        // methods are as follows.
        //
        //   1. private_key_jwt
        //   2. tls_client_auth
        //   3. self_signed_tls_client_auth
        //
        // Among client metadata defined in the following specifications,
        //
        //   - Section 2 of OpenID Connect Dynamic Client Registration 1.0
        //   - Section 2 of RFC 7591 OAuth 2.0 Dynamic Client Registration Protocol
        //
        // "token_endpoint_auth_method" only takes a client authentication method.
        //
        // See also:
        //
        //   [OpenBanking-Brasil/specs-seguranca] Issue 111
        //   Question: Client Authentication Method at Introspection and Revocation Endpoints
        //
        //     https://github.com/OpenBanking-Brasil/specs-seguranca/issues/111
        //

        // Obtain "token_endpoint_auth_method" from the client registration request.
        String method = obtainAsString(requestParams, ssClaims, "token_endpoint_auth_method");

        if (method == null)
        {
            // Open Banking Brasil Financial-grade API extends FAPI 1.0 Advanced.
            // Because clients for FAPI 1.0 Advanced are all confidential clients,
            // a client authentication method must be always set.
            throw invalidClientMetadata(
                    "'token_endpoint_auth_method' is not specified or null.");
        }

        // If the value of "token_endpoint_auth_method" is included in the list
        // of valid client authentication methods.
        if (CLIENT_AUTHENTICATION_METHODS.contains(method))
        {
            // Okay.
            return;
        }

        throw invalidClientMetadata(
                "The value of 'token_endpoint_auth_method' is not allowed.");
    }


    private void validateRequestObjectEncryption(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   8. shall require encrypted request objects as required by the Brasil
        //      Open Banking Security Profile;

        // Open Banking Brasil Financial-grade API Security Profile 1.0 Implementers Draft 1
        // 6.1.1. Encryption algorithm considerations
        //
        //   For JWE, both clients and Authorization Servers
        //
        //     1. shall use RSA-OAEP with A256GCM

        // OpenID Connect Dynamic Client Registration 1.0
        // 2. Client Metadata
        //
        //   request_object_encryption_alg
        //     OPTIONAL. JWE [JWE] alg algorithm [JWA] the RP is declaring that
        //     it may use for encrypting Request Objects sent to the OP. This
        //     parameter SHOULD be included when symmetric encryption will be
        //     used, since this signals to the OP that a client_secret value
        //     needs to be returned from which the symmetric key will be
        //     derived, that might not otherwise be returned. The RP MAY still
        //     use other supported encryption algorithms or send unencrypted
        //     Request Objects, even when this parameter is present. If both
        //     signing and encryption are requested, the Request Object will
        //     be signed then encrypted, with the result being a Nested JWT,
        //     as defined in [JWT]. The default, if omitted, is that the RP
        //     is not declaring whether it might encrypt any Request Objects.
        //
        //   request_object_encryption_enc
        //     OPTIONAL. JWE enc algorithm [JWA] the RP is declaring that it
        //     may use for encrypting Request Objects sent to the OP. If
        //     request_object_encryption_alg is specified, the default for this
        //     value is A128CBC-HS256. When request_object_encryption_enc is
        //     included, request_object_encryption_alg MUST also be provided.

        // In short, "request_object_encryption_alg" must be "RSA-OAEP" and
        // "request_object_encryption_enc" must be "A256GCM".

        // If "request_object_encryption_alg" is included in the client
        // registration request, its value is checked in validateJweAlg().
        // If the metadata is not included, "RSA-OAEP" will be set later
        // as the default value.

        // If "request_object_encryption_enc" is included in the client
        // registration request, its value is checked in validateJweEnc().
        // If the metadata is not included, "A256GCM" will be set later
        // as the default value.

        // As a result, there is nothing to do here.
    }


    private void validateScopesWithRoles(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   9. shall validate that requested scopes are appropriate for the
        //      softwares authorized regulatory roles;

        // Extract "scope".
        String scope = obtainAsString(requestParams, ssClaims, "scope");

        // If the metadata does not contain 'scope' or its value is empty.
        if (scope == null || scope.length() == 0)
        {
            // Nothing to validate here.
            return;
        }

        // True if the scope originates from the software statement.
        boolean fromSS = ssClaims.containsKey("scope");

        // The value of 'scope' is space-separated scope names.
        String[] requestedScopes = scope.split(" +");

        // Extract "software_roles" from the software statement.
        List<String> roles = extractAsStringList(
                ssClaims, "software_roles", REQUIRED, NOT_NULL, FROM_SS);

        // For each requested scope.
        for (String requestedScope : requestedScopes)
        {
            // Check if the requested scope is allowed for the roles.
            validateScopeWithRoles(requestedScope, roles, fromSS);
        }

        // Okay. All the requested scopes are allowed.
    }


    private void validateScopeWithRoles(
            String requestedScope, List<String> roles, boolean fromSS)
    {
        // For each role.
        for (String role : roles)
        {
            // The scopes allowed for the role.
            Set<String> allowedScopes = getAllowedScopesForRole(role);

            // If the set of allowed scopes contains the requested scope.
            if (allowedScopes.contains(requestedScope))
            {
                // Okay. The requested scopes is allowed by the role.
                return;
            }
        }

        throw this.invalidClientMetadata(fromSS,
                "'%s' in the 'scope' claim in the software statement is not allowed by any role in 'software_roles'.",
                "'%s' in the 'scope' parameter in the request body is not allowed by any role in 'software_roles'.",
                requestedScope);
    }


    private Set<String> getAllowedScopesForRole(String role)
    {
        // The scopes allowed for the role.
        Set<String> allowedScopes = ROLE_TO_SCOPES.get(role);

        // If allowed scopes for the role are not available.
        if (allowedScopes == null)
        {
            // This means that the role is unknown to this implementation.
            throw invalidSoftwareStatement(
                    "The role '%s' included in 'software_roles' is unknown.", role);
        }

        return allowedScopes;
    }


    private void validateClientAuthSubject(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   12. if supporting tls_client_auth client authentication mechanism
        //       as defined in RFC8705 shall only accept tls_client_auth_subject_dn
        //       as an indication of the certificate subject value as defined
        //       in clause 2.1.2 RFC8705;

        // RFC 8705 OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens
        // 2.1.2. Client Registration Metadata
        //
        //   ... A client using the tls_client_auth authentication method MUST
        //   use exactly one of the below metadata parameters to indicate the
        //   certificate subject value that the authorization server is to
        //   expect when authenticating the respective client.
        //
        //     tls_client_auth_subject_dn
        //       ...
        //     tls_client_auth_san_dns
        //       ...
        //     tls_client_auth_san_uri
        //       ...
        //     tls_client_auth_san_ip
        //       ...
        //     tls_client_auth_san_email
        //       ...

        // In summary, tls_client_auth_san_* client metadata are not allowed.

        // For each "tls_client_auth_san_*" client metadata defined in RFC 8705
        for (String metadata : TLS_CLIENT_AUTH_SAN_CLIENT_METADATA)
        {
            // If the software statement contains the metadata.
            if (ssClaims.containsKey(metadata))
            {
                throw invalidClientMetadata(
                        "The software statement contains a '%s' claim.", metadata);
            }

            // If the request body contains the metadata.
            if (requestParams.containsKey(metadata))
            {
                throw invalidClientMetadata(
                        "The request body contains a '%s' parameter.", metadata);
            }
        }
    }


    private void validateJwsAlg(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Security Profile 1.0
        // 6.1. Algorithm considerations
        //
        //   For JWS, both clients and Authorization Servers
        //
        //     1. shall use PS256 algorithm;

        // For each client metadata whose value is JWS alg.
        for (String metadata : JWS_ALG_CLIENT_METADATA)
        {
            String alg = obtainAsString(requestParams, ssClaims, metadata);

            if (alg == null || alg.equals("PS256"))
            {
                continue;
            }

            throw invalidClientMetadata(
                    "The value of '%s' must be 'PS256' if specified.", metadata);
        }
    }


    private void validateJweAlg(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Security Profile 1.0
        // 6.1.1. Encryption algorithm considerations
        //
        //   For JWE, both clients and Authorization Servers
        //
        //     1. shall use RSA-OAEP with A256GCM

        // For each client metadata whose value is JWE alg.
        for (String metadata : JWE_ALG_CLIENT_METADATA)
        {
            String alg = obtainAsString(requestParams, ssClaims, metadata);

            if (alg == null || alg.equals("RSA-OAEP"))
            {
                continue;
            }

            throw invalidClientMetadata(
                    "The value of '%s' must be 'RSA-OAEP' if specified.", metadata);
        }
    }


    private void validateJweEnc(
            Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Security Profile 1.0
        // 6.1.1. Encryption algorithm considerations
        //
        //   For JWE, both clients and Authorization Servers
        //
        //     1. shall use RSA-OAEP with A256GCM

        // For each client metadata whose value is JWE enc.
        for (String metadata : JWE_ENC_CLIENT_METADATA)
        {
            String enc = obtainAsString(requestParams, ssClaims, metadata);

            if (enc == null || enc.equals("A256GCM"))
            {
                continue;
            }

            throw invalidClientMetadata(
                    "The value of '%s' must be 'A256GCM' if specified.", metadata);
        }
    }


    private Map<String, Object> mergeClientMetadata(Map<String, Object> requestParams, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   10. should where possible validate client asserted metadata
        //       against metadata provided in the software_statement;

        // This implementation does not check consistency between the sets of
        // metadata. In any case, metadata in the software statement take
        // precedence as RFC 7591 requires so.

        Map<String, Object> merged = new HashMap<String, Object>();

        // For each recognized client metadata.
        for (String metadata : RECOGNIZED_CLIENT_METADATA)
        {
            // Get the value of the metadata from the software statement or
            // the request body. The value in the software statement takes
            // precedence.
            Object value = obtainAsObject(requestParams, ssClaims, metadata);

            if (value != null)
            {
                merged.put(metadata, value);
            }
        }

        // Adjust client metadata.
        adjustClientMetadata(merged, ssClaims);

        return merged;
    }


    private void adjustClientMetadata(Map<String, Object> merged, Map<String, Object> ssClaims)
    {
        // Open Banking Brasil requires that JWS alg be always "PS256".

        // By definition, ID Tokens are always signed.
        merged.putIfAbsent("id_token_signed_response_alg", "PS256");

        // FAPI 1.0 Advanced requires that a Request Object be always used
        // and signed. The "require_signed_request_object" client metadata
        // is defined in JWT Secured Authorization Request (JAR).
        //
        // Setting true to "require_signed_request_object" will require
        // that the authorization server process Request Objects based on
        // the rules defined in JAR. See the following article for details.
        //
        //   Implementer’s note about JAR (JWT Secured Authorization Request)
        //   https://darutk.medium.com/implementers-note-about-jar-fff4cbd158fe
        //
        merged.putIfAbsent("request_object_signing_alg", "PS256");
        merged.putIfAbsent("require_signed_request_object", Boolean.TRUE);

        // Open Banking Brasil requires that Request Objects be encrypted
        // with "RSA-OAEP" and "A256GCM".
        merged.putIfAbsent("request_object_encryption_alg", "RSA-OAEP");
        merged.putIfAbsent("request_object_encryption_enc", "A256GCM");

        // The explanation of the "request_object_encryption_alg" client
        // metadata in "OpenID Connect Dynamic Client Registration 1.0"
        // states as follows:
        //
        //   request_object_encryption_alg
        //
        //     OPTIONAL. JWE [JWE] alg algorithm [JWA] the RP is declaring
        //     that it may use for encrypting Request Objects sent to the OP.
        //     This parameter SHOULD be included when symmetric encryption
        //     will be used, since this signals to the OP that a client_secret
        //     value needs to be returned from which the symmetric key will be
        //     derived, that might not otherwise be returned. The RP MAY still
        //     use other supported encryption algorithms or send unencrypted
        //     Request Objects, even when this parameter is present. If both
        //     signing and encryption are requested, the Request Object will
        //     be signed then encrypted, with the result being a Nested JWT,
        //     as defined in [JWT]. The default, if omitted, is that the RP is
        //     not declaring whether it might encrypt any Request Objects.
        //
        // According to this explanation, setting the client metadata does not
        // mean forcing the client to use the specified algorithm. It cannot
        // even force the client to encrypt request objects.
        //
        // Therefore, to meet the following requirement of Open Banking Brasil,
        //
        //   Open Banking Brasil Financial-grade API Security Profile 1.0
        //   5.2.2. Authorization server
        //
        //     1. shall support a signed and encrypted JWE request object passed
        //        by value or shall require pushed authorization requests PAR;
        //
        // non-standard mechanisms are needed. Authlete fulfills the requirement
        // by Authlete-specific client properties. See the JavaDoc of the Client
        // class for details.
        //
        //   JavaDoc of authlete-java-common library
        //   https://authlete.github.io/authlete-java-common/
        //
        merged.putIfAbsent("authlete:frontChannelRequestObjectEncryptionRequired", Boolean.TRUE);
        merged.putIfAbsent("authlete:requestObjectEncryptionAlgMatchRequired", Boolean.TRUE);
        merged.putIfAbsent("authlete:requestObjectEncryptionEncMatchRequired", Boolean.TRUE);

        // The "token_endpoint_auth_signing_alg" client metadata has a meaning
        // only when a client assertion is used for client authentication.
        merged.putIfAbsent("token_endpoint_auth_signing_alg", "PS256");

        // The "backchannel_authentication_request_signing_alg" client metadata
        // has a meaning only when a backchannel authentication request contains
        // the "request" request parameter.
        merged.putIfAbsent("backchannel_authentication_request_signing_alg", "PS256");

        // The "authorization_signed_response_alg" client metadata has a meaning
        // only when "response_mode=[[query|fragment|form_post].]jwt" is given.
        merged.putIfAbsent("authorization_signed_response_alg", "PS256");

        // Note that the default value is not set for "userinfo_signed_response_alg".
        // It's because setting an algorithm to the client metadata would change
        // the format of responses from the UserInfo endpoint.

        // Open Banking Brasil Financial-grade API is based on
        // "FAPI 1.0 Advanced" which requires certificate-bound access tokens.
        merged.putIfAbsent("tls_client_certificate_bound_access_tokens", Boolean.TRUE);

        // the latest security profile ("v2") requires that id tokens are always encrypted
        merged.putIfAbsent("id_token_encrypted_response_alg", "RSA-OAEP");
        merged.putIfAbsent("id_token_encrypted_response_enc", "A256GCM");
        // and that an acr value is always returned
        merged.putIfAbsent("default_acr_values", Arrays.asList("urn:brasil:openbanking:loa3"));

        // Use some claims in the software statement as default values
        // for some standard claims. See also:
        //
        //   [OpenBanking-Brasil/specs-seguranca] Issue 114
        //   Question: software_* in SSA as defaults for standard client metadata
        //
        //     https://github.com/OpenBanking-Brasil/specs-seguranca/issues/114
        //
        useAsDefault(merged, ssClaims, "software_client_name",        "client_name");
        useAsDefault(merged, ssClaims, "software_tos_uri",            "tos_uri");
        useAsDefault(merged, ssClaims, "software_client_description", "client_description");
        useAsDefault(merged, ssClaims, "software_policy_uri",         "policy_uri");
        useAsDefault(merged, ssClaims, "software_client_uri",         "client_uri");
        useAsDefault(merged, ssClaims, "software_logo_uri",           "logo_uri");

        // Adjust "scope".
        adjustScope(merged);
    }


    private void useAsDefault(
            Map<String, Object> merged, Map<String, Object> ssClaims,
            String sourceKey, String targetKey)
    {
        // If the target key already exists in the merged set of client metadata.
        if (merged.containsKey(targetKey))
        {
            return;
        }

        // If the source key does not exist in the software statement.
        if (!ssClaims.containsKey(sourceKey))
        {
            return;
        }

        // Use the value in the software statement as the default value.
        merged.put(targetKey, ssClaims.get(sourceKey));
    }


    private void adjustScope(Map<String, Object> merged)
    {
        // Extract "software_roles". The software statement must include it.
        List<String> roles = extractAsStringList(
                merged, "software_roles", REQUIRED, NOT_NULL, FROM_SS);

        // The "scope" in the merged client metadata.
        String scope = (String)merged.get("scope");

        if (scope == null)
        {
            // Prepare scopes based on the regulatory roles which are
            // listed in the "software_roles" claim.
            scope = prepareScopeByRoles(roles);
            merged.put("scope", scope);
        }

        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // Regulatory Roles to dynamic OAuth 2.0 scope Mappings
        //
        //   -----------------------------------------
        //   | Regulatory Role | Allowed Scopes      |
        //   |-----------------+---------------------|
        //   | DADOS           | consent:{ConsentId} |
        //   | PAGTO           | consent:{ConsentId} |
        //   -----------------------------------------
        //
        // For Authlete customers,
        //
        // To support the "Dynamic Consent Scope" defined in OBB FAPI, the
        // "consents" scope of your authorization server must have a scope
        // attribute whose name is "regex" and whose value is a regular
        // expression that matches "consent:{ConsentId}". For example,
        // "^consent:.+$".
        //
        // The "scope attribute" feature is specific to Authlete. Other solutions
        // provide different approaches for the "Dynamic Consent Scope".
        //
        // See the following articles for details about Authlete's approach for
        // dynamic scopes.
        //
        //   [Blog] Implementer’s note about Open Banking Brasil
        //     https://darutk.medium.com/implementers-note-about-open-banking-brasil-78d3d612dfaf
        //
        //   [Authlete Knowledge Base] Using “parameterized scopes”
        //     https://kb.authlete.com/en/s/oauth-and-openid-connect/a/parameterized-scopes
        //
    }


    private String prepareScopeByRoles(List<String> roles)
    {
        Set<String> scopes = new HashSet<String>();

        // For each role listed in "software_roles".
        for (String role : roles)
        {
            // The scopes allowed for the role.
            Set<String> allowedScopes = getAllowedScopesForRole(role);

            // Accumulate the allowed scopes without duplicates.
            scopes.addAll(allowedScopes);
        }

        // Concatenate the scopes with spaces.
        return String.join(" ", scopes);
    }


    private Object extractAsObject(
            Map<String, Object> map, String key, boolean optional, boolean nullable, boolean isSoftwareStatement)
    {
        // If the map does not include the key.
        if (!map.containsKey(key))
        {
            if (optional)
            {
                // The map does not include the key, but it is allowed.
                return null;
            }

            throw invalidClientMetadata(isSoftwareStatement,
                    "The software statement does not include the '%s' claim.",
                    "The request body does not include the '%s' parameter.", key);
        }

        // Get the value from the map.
        Object value = map.get(key);

        if (value == null)
        {
            if (nullable)
            {
                // The value of the entry is null, but it is allowed.
                return null;
            }

            throw invalidClientMetadata(isSoftwareStatement,
                    "The value of the '%s' claim in the software statement is null.",
                    "The value of the '%s' parameter in the request body is null.", key);
        }

        // The map includes an entry for the key and its value is not null.
        return value;
    }


    private String extractAsString(
            Map<String, Object> map, String key, boolean optional, boolean nullable, boolean isSoftwareStatement)
    {
        // Extract the object from the map.
        Object value = extractAsObject(map, key, optional, nullable, isSoftwareStatement);
        if (value == null)
        {
            // The existence of the key is optional or null is allowed.
            return null;
        }

        // If the type of the value is not a string.
        if (!(value instanceof String))
        {
            throw invalidClientMetadata(isSoftwareStatement,
                    "The value of the '%s' claim in the software statement is not a string: class=%s",
                    "The value of the '%s' parameter in the request body is not a string: class=%s",
                    key, value.getClass().getName());
        }

        // The map includes an entry for the key and its value is a string.
        return (String)value;
    }


    @SuppressWarnings("unused")
    private Long extractAsLong(
            Map<String, Object> map, String key, boolean optional, boolean nullable, boolean isSoftwareStatement)
    {
        // Extract the object from the map
        Object value = extractAsObject(map, key, optional, nullable, isSoftwareStatement);
        if (value == null)
        {
            // The existence of the key is optional or null is allowed.
            return null;
        }

        // If the type of the value is not a number.
        if (!(value instanceof Number))
        {
            throw invalidClientMetadata(isSoftwareStatement,
                    "The value of the '%s' claim in the software statement is not a number: class=%s",
                    "The value of the '%s' parameter in the request body is not a number: class=%s",
                    key, value.getClass().getName());
        }

        // The map includes an entry for the key and its value can be interpreted as Long.
        return ((Number)value).longValue();
    }


    private Date extractAsDate(
            Map<String, Object> map, String key, boolean optional, boolean nullable, boolean isSoftwareStatement)
    {
        // Extract the object from the map
        Object value = extractAsObject(map, key, optional, nullable, isSoftwareStatement);
        if (value == null)
        {
            // The existence of the key is optional or null is allowed.
            return null;
        }

        // If the type of the value is not a Date.
        if (!(value instanceof Date))
        {
            throw invalidClientMetadata(isSoftwareStatement,
                    "The value of the '%s' claim in the software statement is not a date: class=%s",
                    "The value of the '%s' parameter in the request body is not a date: class=%s",
                    key, value.getClass().getName());
        }

        // The map includes an entry for the key and its value can be interpreted as Date.
        return (Date)value;
    }


    private List<String> extractAsStringList(
            Map<String, Object> map, String key, boolean optional, boolean nullable, boolean isSoftwareStatement)
    {
        // Extract the object from the map
        Object value = extractAsObject(map, key, optional, nullable, isSoftwareStatement);
        if (value == null)
        {
            // The existence of the key is optional or null is allowed.
            return null;
        }

        // If the type of the value is not a list.
        if (!(value instanceof List))
        {
            throw invalidClientMetadata(isSoftwareStatement,
                    "The value of the '%s' claim in the software statement is not an array: class=%s",
                    "The value of the '%s' parameter in the request body is not an array: class=%s",
                    key, value.getClass().getName());
        }

        List<?> list = (List<?>)value;
        int     size = list.size();

        List<String> result = new ArrayList<String>(size);

        for (int i = 0; i < size; i++)
        {
            Object element = list.get(i);

            // If the element is null or a string.
            if (element == null || element instanceof String)
            {
                result.add((String)element);
                continue;
            }

            throw invalidClientMetadata(isSoftwareStatement,
                    "The value at the index '%d' of the '%s' claim in the software statement is not a string: class=%s",
                    "The value at the index '%d' of the '%s' parameter in the request body is not a string: class=%s",
                    i, key, element.getClass().getName());
        }

        return result;
    }


    private Object obtainAsObject(Map<String, Object> requestParams, Map<String, Object> ssClaims, String key)
    {
        // If the software statement contains the key.
        if (ssClaims.containsKey(key))
        {
            // Extract from the software statement.
            return extractAsObject(ssClaims, key, OPTIONAL, NULLABLE, FROM_SS);
        }

        // Extract from the request body.
        return extractAsObject(requestParams, key, OPTIONAL, NULLABLE, FROM_BODY);
    }


    private String obtainAsString(Map<String, Object> requestParams, Map<String, Object> ssClaims, String key)
    {
        // If the software statement contains the key.
        if (ssClaims.containsKey(key))
        {
            // Extract from the software statement.
            return extractAsString(ssClaims, key, OPTIONAL, NULLABLE, FROM_SS);
        }

        // Extract from the request body.
        return extractAsString(requestParams, key, OPTIONAL, NULLABLE, FROM_BODY);
    }


    public static WebApplicationException errorResponse(Status status, String code, String description)
    {
        String body = String.format(
                "{\n" +
                "  \"error\": \"%s\",\n" +
                "  \"error_description\": \"%s\"\n" +
                "}\n",
                code, description)
                ;

        Response response = Response
                .status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(body)
                .build()
                ;

        return new WebApplicationException(response);
    }


    private static WebApplicationException badRequest(String code, String description)
    {
        // RFC 7591 OAuth 2.0 Dynamic Client Registration Protocol
        // 3.2.2. Client Registration Error Response
        //
        //   When a registration error condition occurs, the authorization
        //   server returns an HTTP 400 status code (unless otherwise specified)
        //   with content type "application/json" consisting of a JSON object
        //   [RFC7159] describing the error in the response body.
        //
        return errorResponse(Status.BAD_REQUEST, code, description);
    }


    private static WebApplicationException invalidRequest(String format, Object... args)
    {
        return badRequest("invalid_request", String.format(format, args));
    }


    private WebApplicationException invalidRedirectUri(String format, Object... args)
    {
        // RFC 7591, 3.2.2. Client Registration Error Response
        //
        //   invalid_redirect_uri
        //     The value of one or more redirection URIs is invalid.
        //
        return badRequest("invalid_redirect_uri", String.format(format, args));
    }


    private WebApplicationException invalidClientMetadata(String format, Object... args)
    {
        // RFC 7591, 3.2.2. Client Registration Error Response
        //
        //   invalid_client_metadata
        //     The value of one of the client metadata fields is invalid and
        //     the server has rejected this request.  Note that an authorization
        //     server MAY choose to substitute a valid value for any requested
        //     parameter of a client's metadata.
        //
        return badRequest("invalid_client_metadata", String.format(format, args));
    }


    private WebApplicationException invalidClientMetadata(
            boolean isSoftwareStatement, String formatForSS, String format, Object... args)
    {
        return invalidClientMetadata(isSoftwareStatement ? formatForSS : format, args);
    }


    private WebApplicationException invalidSoftwareStatement(String format, Object... args)
    {
        // RFC 7591, 3.2.2. Client Registration Error Response
        //
        //   invalid_software_statement
        //     The software statement presented is invalid.
        //
        return badRequest("invalid_software_statement", String.format(format, args));
    }


    private WebApplicationException serverError(String format, Object... args)
    {
        // Arguable on the HTTP status code in the case of "error":"server_error".
        return badRequest("server_error", String.format(format, args));
    }
}
