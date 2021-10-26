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


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class OBBDCRConstants
{
    // Client authentication methods allowed in the context of FAPI 1.0 Advanced.
    public static final Set<String> CLIENT_AUTHENTICATION_METHODS = toSet(
            "private_key_jwt",
            "tls_client_auth",
            "self_signed_tls_client_auth"
    );


    // A list of tls_client_auth_san_* client metadata defined in RFC 8705.
    public static final List<String> TLS_CLIENT_AUTH_SAN_CLIENT_METADATA = toList(
            "tls_client_auth_san_dns",
            "tls_client_auth_san_uri",
            "tls_client_auth_san_ip",
            "tls_client_auth_san_email"
    );


    // A list of client metadata whose value is JWS alg.
    public static final List<String> JWS_ALG_CLIENT_METADATA = toList(
            // OpenID Connect Dynamic Client Registration 1.0
            "id_token_signed_response_alg",
            "userinfo_signed_response_alg",
            "request_object_signing_alg",
            "token_endpoint_auth_signing_alg",

            // OpenID Connect Client-Initiated Backchannel Authentication Flow - Core 1.0
            "backchannel_authentication_request_signing_alg",

            // JWT Secured Authorization Response Mode for OAuth 2.0 (JARM)
            "authorization_signed_response_alg"
    );


    // A list of client metadata whose value is JWE alg.
    public static final List<String> JWE_ALG_CLIENT_METADATA = toList(
            // OpenID Connect Dynamic Client Registration 1.0
            "id_token_encrypted_response_alg",
            "userinfo_encrypted_response_alg",
            "request_object_encryption_alg",

            // JWT Secured Authorization Response Mode for OAuth 2.0 (JARM)
            "authorization_encrypted_response_alg"
    );


    // A list of client metadata whose value is JWE enc.
    public static final List<String> JWE_ENC_CLIENT_METADATA = toList(
            // OpenID Connect Dynamic Client Registration 1.0
            "id_token_encrypted_response_enc",
            "userinfo_encrypted_response_enc",
            "request_object_encryption_enc",

            // JWT Secured Authorization Response Mode for OAuth 2.0 (JARM)
            "authorization_encrypted_response_enc"
    );


    // A list of claims (in a software statement) and parameters (in a request
    // body) that this implementation remember as client metadata.
    //
    // There are no clear criteria on this yet. See also:
    //
    //   [OpenBanking-Brasil/specs-seguranca] Issue 84
    //   Question: Which claims in SSA should be kept as client metadata?
    //
    //     https://github.com/OpenBanking-Brasil/specs-seguranca/issues/84
    //
    public static final Set<String> RECOGNIZED_CLIENT_METADATA = toSet(
            //-----------------------------------------------------------------------------------------
            // OpenID Connect Dynamic Client Registration 1.0
            //-----------------------------------------------------------------------------------------
            "redirect_uris",
            "response_types",
            "grant_types",
            "application_type",
            "contacts",
            "client_name",
            "logo_uri",
            "client_uri",
            "policy_uri",
            "tos_uri",
            "jwks_uri",
            // "jwks",                          // Prohibited by Open Banking Brasil
            "sector_identifier_uri",
            "subject_type",
            "id_token_signed_response_alg",
            "id_token_encrypted_response_alg",
            "id_token_encrypted_response_enc",
            "userinfo_signed_response_alg",
            "userinfo_encrypted_response_alg",
            "userinfo_encrypted_response_enc",
            "request_object_signing_alg",
            "request_object_encryption_alg",
            "request_object_encryption_enc",
            "token_endpoint_auth_method",
            "token_endpoint_auth_signing_alg",
            "default_max_age",
            "require_auth_time",
            "default_acr_values",
            "initiate_login_uri",
            "request_uris",

            //-----------------------------------------------------------------------------------------
            // RFC 7591 OAuth 2.0 Dynamic Client Registration Protocol
            //-----------------------------------------------------------------------------------------
            // "redirect_uri",                  // Duplicate
            // "token_endpoint_auth_method",    // Duplicate
            // "grant_types",                   // Duplicate
            // "response_types",                // Duplicate
            // "client_name",                   // Duplicate
            // "client_uri",                    // Duplicate
            // "logo_uri",                      // Duplicate
            "scope",
            // "contacts",                      // Duplicate
            // "tos_uri",                       // Duplicate
            // "policy_uri",                    // Duplicate
            // "jwks_uri",                      // Duplicate
            // "jwks",                          // Duplicate & Prohibited by Open Banking Brasil
            "software_id",
            "software_version",

            //-----------------------------------------------------------------------------------------
            // RFC 7592 OAuth 2.0 Dynamic Client Registration Management Protocol
            //-----------------------------------------------------------------------------------------
            "client_id",
            "client_secret",

            //-----------------------------------------------------------------------------------------
            // RFC 8705 OAuth 2.0 Mutual-TLS Client Authentication and Certificate-Bound Access Tokens
            //-----------------------------------------------------------------------------------------
            "tls_client_certificate_bound_access_tokens",
            "tls_client_auth_subject_dn",
            // "tls_client_auth_san_dns",       // Prohibited by Open Banking Brasil
            // "tls_client_auth_san_uri",       // Prohibited by Open Banking Brasil
            // "tls_client_auth_san_ip",        // Prohibited by Open Banking Brasil
            // "tls_client_auth_san_email",     // Prohibited by Open Banking Brasil

            //-----------------------------------------------------------------------------------------
            // OpenID Connect Client-Initiated Backchannel Authentication Flow - Core 1.0
            //-----------------------------------------------------------------------------------------
            "backchannel_token_delivery_mode",
            "backchannel_client_notification_endpoint",
            "backchannel_authentication_request_signing_alg",
            "backchannel_user_code_parameter",

            //-----------------------------------------------------------------------------------------
            // JWT Secured Authorization Request (JAR)
            //-----------------------------------------------------------------------------------------
            "require_signed_request_object",

            //-----------------------------------------------------------------------------------------
            // JWT Secured Authorization Response Mode for OAuth 2.0 (JARM)
            //-----------------------------------------------------------------------------------------
            "authorization_signed_response_alg",
            "authorization_encrypted_response_alg",
            "authorization_encrypted_response_enc",

            //-----------------------------------------------------------------------------------------
            // OAuth 2.0 Pushed Authorization Requests (PAR)
            //-----------------------------------------------------------------------------------------
            "require_pushed_authorization_requests",

            //-----------------------------------------------------------------------------------------
            // OAuth 2.0 Rich Authorization Requests (RAR)
            //-----------------------------------------------------------------------------------------
            "authorization_details_types",

            //-----------------------------------------------------------------------------------------
            // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
            //-----------------------------------------------------------------------------------------

            // NOTE:
            // There are no clear criteria for inclusion and exclusion. See also:
            //
            //   [OpenBanking-Brasil/specs-seguranca] Issue 84
            //   Question: Which claims in SSA should be kept as client metadata?
            //
            //     https://github.com/OpenBanking-Brasil/specs-seguranca/issues/84
            //

            // "software_mode",
            // "software_redirect_uris",
            // "software_statement_roles",
            "software_client_name",
            // "org_status",
            "software_client_id",
            // "iss",
            "software_tos_uri",
            "software_client_description",
            "software_jwks_uri",
            "software_policy_uri",
            // "software_id",                             // Duplicate
            "software_client_uri",
            "software_jwks_inactive_uri",
            "software_jwks_transport_inactive_uri",
            "software_logo_uri",
            "org_id",
            "org_number",
            "software_environment",
            // "software_version",                        // Duplicate
            "software_roles",
            "org_name"
            // "iat",
            // "organisation_competent_authority_claims"
    );


    // Mapping from a role to scopes.
    //
    //   Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
    //   7.2. Regulatory Roles to OpenID and OAuth 2.0 Mappings
    //
    //     ---------------------------------------------------------------------
    //     | Regulatory Roles | Allowed Scopes                                 |
    //     |------------------+------------------------------------------------|
    //     | DADOS            | openid accounts credit-cards-accounts consents |
    //     |                  | customers invoice-financings financings loans  |
    //     |                  | unarranged-accounts-overdraft resources        |
    //     |------------------+------------------------------------------------|
    //     | PAGTO            | openid payments consents resources             |
    //     |------------------+------------------------------------------------|
    //     | CONTA            | openid                                         |
    //     |------------------+------------------------------------------------|
    //     | CCORR            | openid                                         |
    //     ---------------------------------------------------------------------
    //
    public static final Map<String, Set<String>> ROLE_TO_SCOPES = toMap(
            "DADOS", toSet(
                         "openid", "accounts", "credit-cards-accounts", "consents",
                         "customers", "invoice-financings", "financings", "loans",
                         "unarranged-accounts-overdraft", "resources"
                     ),
            "PAGTO", toSet("openid", "payments", "consents", "resources"),
            "CONTA", toSet("openid"),
            "CCORR", toSet("openid")
    );


    @SuppressWarnings("unchecked")
    private static <T> List<T> toList(T... elements)
    {
        return Arrays.asList(elements);
    }


    @SuppressWarnings("unchecked")
    private static <T> Set<T> toSet(T... elements)
    {
        return new HashSet<T>(Arrays.asList(elements));
    }


    @SuppressWarnings("unchecked")
    private static <TKey, TValue> Map<TKey, TValue> toMap(Object... elements)
    {
        Map<TKey, TValue> map = new HashMap<TKey, TValue>();

        for (int i = 0; i < elements.length; i += 2)
        {
            map.put((TKey)elements[i], (TValue)elements[i+1]);
        }

        return map;
    }
}
