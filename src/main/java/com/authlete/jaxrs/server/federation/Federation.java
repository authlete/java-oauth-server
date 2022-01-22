/*
 * Copyright (C) 2022 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.federation;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AbstractRequest;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ErrorResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;


/**
 * Utility for ID federation.
 *
 * <p>
 * This utility class provides public methods for ID federation. The discovery
 * document and the JWK set document of the OpenID Provider will be cached
 * in an instance of this class.
 * </p>
 *
 * @see FederationConfig
 */
public class Federation
{
    private interface ResponseParser<T>
    {
        T parse(HTTPResponse response) throws ParseException;
    }

    private static final boolean REQUIRED = true;
    private static final boolean OPTIONAL = false;


    private final FederationConfig config;
    private final Logger logger;
    private OIDCProviderMetadata serverMetadata;
    private IDTokenValidator idTokenValidator;


    public Federation(FederationConfig config)
    {
        this.config = config;
        this.logger = LoggerFactory.getLogger(getClass());
    }


    //------------------------------------------------------------
    // Federation Configuration
    //------------------------------------------------------------


    public FederationConfig getConfiguration()
    {
        return config;
    }


    private <T> T fromFederationConfig(
            String path, boolean required,
            Function<FederationConfig, T> func) throws IOException
    {
        T value = null;

        try
        {
            value = func.apply(config);
        }
        catch (Exception e)
        {
        }

        if (value == null && required)
        {
            throw ioexception("''{0}'' is not found in the federation configuration.", path);
        }

        return value;
    }


    private Issuer issuer() throws IOException
    {
        String value = fromFederationConfig(
                "server/issuer", REQUIRED,
                conf -> conf.getServer().getIssuer());

        return new Issuer(value);
    }


    private ClientID clientId() throws IOException
    {
        String value = fromFederationConfig(
                "client/clientId", REQUIRED,
                conf -> conf.getClient().getClientId());

        return new ClientID(value);
    }


    private Secret clientSecret() throws IOException
    {
        String value = fromFederationConfig(
                "client/clientSecret", OPTIONAL,
                conf -> conf.getClient().getClientSecret());

        if (value == null)
        {
            return null;
        }

        return new Secret(value);
    }


    private URI redirectUri() throws IOException
    {
        String value = fromFederationConfig(
                "client/redirectUri", REQUIRED,
                conf -> conf.getClient().getRedirectUri());

        try
        {
            return new URI(value);
        }
        catch (URISyntaxException e)
        {
            throw ioexception(e, "The value of ''client/redirectUri'' is malformed: {0}", value);
        }
    }


    private JWSAlgorithm idTokenSignedResponseAlg() throws IOException
    {
        String value = fromFederationConfig(
                "client/idTokenSignedResponseAlg", OPTIONAL,
                conf -> conf.getClient().getIdTokenSignedResponseAlg());

        if (value == null)
        {
            return null;
        }

        return JWSAlgorithm.parse(value);
    }


    //------------------------------------------------------------
    // Server Metadata
    //------------------------------------------------------------


    private OIDCProviderMetadata getServerMetadata() throws IOException
    {
        // If the server metadata is not cached.
        if (serverMetadata == null)
        {
            // Fetch the server metadata from the discovery endpoint.
            serverMetadata = fetchServerMetadata();
        }

        return serverMetadata;
    }


    private OIDCProviderMetadata fetchServerMetadata() throws IOException
    {
        // The issuer identifier of the OpenID provider.
        Issuer issuer = issuer();

        // Prepare a request to the discovery endpoint.
        OIDCProviderConfigurationRequest request =
                new OIDCProviderConfigurationRequest(issuer);

        // Send the request and receive a response.
        HTTPResponse response = sendRequest(request);

        // Parse the response.
        OIDCProviderMetadata metadata = parseResponse(request, response,
                res -> OIDCProviderMetadata.parse(res.getContentAsJSONObject()));

        // Validate the discovery document.
        validateDiscoveryDocument(issuer, metadata);

        return metadata;
    }


    private void validateDiscoveryDocument(
            Issuer issuer, OIDCProviderMetadata metadata) throws IOException
    {
        if (!issuer.equals(metadata.getIssuer()))
        {
            // 'issuer' in the discovery document is wrong.
            throw ioexception(
                    "''issuer'' in the discovery document is wrong: expected={0}, actual={1}",
                    issuer, metadata.getIssuer());
        }
    }


    private <T> T fromServerMetadata(
            String path, boolean required,
            Function<OIDCProviderMetadata, T> func) throws IOException
    {
        OIDCProviderMetadata metadata;

        try
        {
            // Get the server metadata.
            metadata = getServerMetadata();
        }
        catch (IOException e)
        {
            throw e;
        }

        T value = null;

        try
        {
            value = func.apply(metadata);
        }
        catch (Exception e)
        {
        }

        if (value == null && required)
        {
            throw ioexception("''{0}'' is not found in the server metadata.", path);
        }

        return value;
    }


    private URI authorizationEndpoint() throws IOException
    {
        return fromServerMetadata(
                "authorization_endpoint", REQUIRED,
                metadata -> metadata.getAuthorizationEndpointURI());
    }


    private URI tokenEndpoint() throws IOException
    {
        return fromServerMetadata(
                "token_endpoint", REQUIRED,
                metadata -> metadata.getTokenEndpointURI());
    }


    private URI userInfoEndpoint() throws IOException
    {
        return fromServerMetadata(
                "userinfo_endpoint", REQUIRED,
                metadata -> metadata.getUserInfoEndpointURI());
    }


    private URI jwksUri() throws IOException
    {
        return fromServerMetadata(
                "jwks_uri", REQUIRED,
                metadata -> metadata.getJWKSetURI());
    }


    private Boolean authorizationResponseIssParameterSupported() throws IOException
    {
        String key = "authorization_response_iss_parameter_supported";

        Object value = fromServerMetadata(
                key, OPTIONAL, metadata -> metadata.getCustomParameter(key));

        if (value == null)
        {
            return null;
        }

        return Boolean.parseBoolean(value.toString());
    }


    //------------------------------------------------------------
    // Common Methods
    //------------------------------------------------------------


    private HTTPResponse sendRequest(AbstractRequest request) throws IOException
    {
        try
        {
            // Send the request to the endpoint.
            return request.toHTTPRequest().send();
        }
        catch (IOException e)
        {
            // The request to the endpoint failed.
            throw ioexception(e, "The request to ''{0}'' failed: {1}",
                    request.getEndpointURI(), e.getMessage());
        }
    }


    private <T> T parseResponse(
            AbstractRequest request, HTTPResponse response, ResponseParser<T> func) throws IOException
    {
        try
        {
            // Parse the response.
            return func.parse(response);
        }
        catch (ParseException e)
        {
            // Failed to parse the response.
            throw ioexception(e, "Failed to parse the response from ''{0}'': {1}",
                    request.getEndpointURI(), e.getMessage());
        }
    }


    private IOException processErrorResponse(URI endpoint, ErrorResponse response)
    {
        ErrorObject err = response.getErrorObject();

        // Log the error and create an IOException.
        return ioexception(
                "The response from ''{0}'' indicates an error: error={1}, error_description={2}",
                endpoint, err.getCode(), err.getDescription());
    }


    private IOException ioexception(String pattern, Object... arguments)
    {
        return ioexception((Throwable)null, pattern, arguments);
    }


    private IOException ioexception(Throwable cause, String pattern, Object... arguments)
    {
        String message = MessageFormat.format(pattern, arguments);

        if (cause != null)
        {
            logger.error(message, cause);

            return new IOException(message, cause);
        }
        else
        {
            logger.error(message);

            return new IOException(message);
        }
    }


    //------------------------------------------------------------
    // Authentication Request
    //------------------------------------------------------------


    private AuthenticationRequest buildAuthenticationRequest(
            State state, CodeVerifier verifier, CodeChallengeMethod method) throws IOException
    {
        // The authorization endpoint of the OpenID provider.
        URI endpoint = authorizationEndpoint();

        // response_type
        ResponseType responseType = new ResponseType("code");

        // scope
        Scope scope = buildAuthenticationRequestScope();

        // client_id (from federation configuration)
        ClientID clientId = clientId();

        // redirect_uri (from federation configuration)
        URI redirectUri = redirectUri();

        // Start to build an authentication request.
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(responseType, scope, clientId, redirectUri)
                .endpointURI(endpoint)
                ;

        // state
        if (state != null)
        {
            builder.state(state);
        }

        // nonce
        //
        //   Optional unless "response_type" includes "id_token".
        //   See OIDC Core 1.0 Section 3.1.2.1 & Section 3.2.2.1.
        //
        //   But in the Financial-grade API context, "nonce" is mandatory
        //   when "scope" includes "openid". "response_type" does not matter.
        //   See FAPI 1.0 Baseline Section 5.2.2.2.

        // code_challenge & code_challenge_method
        if (verifier != null && method != null)
        {
            // code_challenge is computed from the verifier and the method.
            builder.codeChallenge(verifier, method);
        }

        return builder.build();
    }


    private static Scope buildAuthenticationRequestScope()
    {
        return new Scope(
                OIDCScopeValue.ADDRESS,
                OIDCScopeValue.EMAIL,
                OIDCScopeValue.OPENID,
                OIDCScopeValue.PHONE,
                OIDCScopeValue.PROFILE
        );
    }


    //------------------------------------------------------------
    // Authentication Response
    //------------------------------------------------------------


    private AuthorizationCode extractAuthorizationCode(
            URI response, State state) throws IOException
    {
        AuthenticationResponse authenticationResponse;

        try
        {
            // Parse the authentication response.
            authenticationResponse = AuthenticationResponseParser.parse(response);
        }
        catch (ParseException e)
        {
            throw ioexception(e, "Failed to parse the response from ''{0}'': {1}",
                    authorizationEndpoint(), e.getMessage());
        }

        // Validate the authentication response.
        validateAuthenticationResponse(authenticationResponse, state);

        // Extract the authorization code from the response.
        return authenticationResponse.toSuccessResponse().getAuthorizationCode();
    }


    private void validateAuthenticationResponse(
            AuthenticationResponse response, State state) throws IOException
    {
        // If the 'state' included in the authentication response is
        // different from the expected state.
        if (state != null && !state.equals(response.getState()))
        {
            throw ioexception("Unexpected authentication response.");
        }

        // If "authorization_response_iss_parameter_supported" is true.
        if (authorizationResponseIssParameterSupported() == Boolean.TRUE)
        {
            // TODO
            // Confirm that the "iss" response parameter is identical to
            // the issuer identifier in the discovery document.
            // See "OAuth 2.0 Authorization Server Issuer Identification".
        }

        // If the authentication response indicates an error.
        if (response instanceof AuthenticationErrorResponse)
        {
            // Process the error response.
            throw processErrorResponse(
                    authorizationEndpoint(), response.toErrorResponse());
        }
    }


    //------------------------------------------------------------
    // Token Request
    //------------------------------------------------------------


    private OIDCTokenResponse makeTokenRequest(
            AuthorizationCode code, CodeVerifier verifier) throws IOException
    {
        // Prepare a token request.
        TokenRequest request = buildTokenRequest(code, verifier);

        // Send the request and receive a response.
        HTTPResponse response = sendRequest(request);

        // Parse the response.
        TokenResponse tokenResponse = parseResponse(request, response,
                res -> OIDCTokenResponseParser.parse(res));

        // If the token response indicates an error.
        if (!tokenResponse.indicatesSuccess())
        {
            // Process the error response.
            throw processErrorResponse(
                    request.getEndpointURI(), tokenResponse.toErrorResponse());
        }

        return (OIDCTokenResponse)tokenResponse.toSuccessResponse();
    }


    private TokenRequest buildTokenRequest(
            AuthorizationCode code, CodeVerifier verifier) throws IOException
    {
        // Client credentials from the federation configuration.
        // The client secret may be null.
        ClientID clientId   = clientId();
        Secret clientSecret = clientSecret();

        // grant_type=authorization_code
        //
        //   Mandatory, specifying "authorization code grant".
        //
        // code
        //
        //   Mandatory in the case of grant_type=authorization_code.
        //
        // redirect_uri
        //
        //   Mandatory when the authorization request included "redirect_uri".
        //
        // code_verifier
        //
        //   Mandatory when the authorization request included "code_challenge".
        //
        AuthorizationGrant grant = new AuthorizationCodeGrant(code, redirectUri(), verifier);

        // The token endpoint of the OpenID provider.
        URI endpoint = tokenEndpoint();

        if (clientSecret != null)
        {
            // Client authentication at the token endpoint, assuming that
            // the endpoint supports "client_secret_basic.
            ClientAuthentication clientAuth = new ClientSecretBasic(clientId, clientSecret);

            // A token request with client authentication.
            return new TokenRequest(endpoint, clientAuth, grant);
        }
        else
        {
            // A token request without client authentication.
            return new TokenRequest(endpoint, clientId, grant);
        }
    }


    //------------------------------------------------------------
    // ID Token Validation
    //------------------------------------------------------------


    private IDTokenValidator getIdTokenValidator() throws IOException
    {
        // If an ID token validator is not cached.
        if (idTokenValidator == null)
        {
            // Create an ID token validator.
            idTokenValidator = createIdTokenValidator();
        }

        return idTokenValidator;
    }


    private IDTokenValidator createIdTokenValidator() throws IOException
    {
        // id_token_signed_response_alg from the federation configuration.
        JWSAlgorithm alg = idTokenSignedResponseAlg();
        if (alg == null)
        {
            alg = JWSAlgorithm.RS256;
        }

        // jwks_uri from the server metadata.
        URL jwksLocation = jwksUri().toURL();

        // From "How to validate an OpenID Connect ID token"
        // https://connect2id.com/blog/how-to-validate-an-openid-connect-id-token
        //
        //   This ID token validator will automatically download the JWK set
        //   from the IdP and cache the keys to speed up processing. OpenID
        //   Providers may rotate keys (Google does it once per day), which
        //   will be detected by the validator, so you don't have to worry
        //   about this.
        //
        return new IDTokenValidator(issuer(), clientId(), alg, jwksLocation);
    }


    private IDTokenClaimsSet validateIdToken(JWT idToken) throws IOException
    {
        // ID token validator
        IDTokenValidator validator = getIdTokenValidator();

        try
        {
            // Validate the ID token.
            return validator.validate(idToken, null);
        }
        catch (BadJOSEException e)
        {
            // Invalid signature or claims (iss, aud, exp...)
            throw ioexception(e, "The ID token issued from ''{0}'' is invalid: {1}",
                    issuer(), e.getMessage());
        }
        catch (JOSEException e)
        {
            // Internal processing exception
            throw ioexception(e, "Failed to validate the ID token issued from ''{0}'': {1}",
                    issuer(), e.getMessage());
        }
    }


    //------------------------------------------------------------
    // UserInfo Request
    //------------------------------------------------------------


    private UserInfo makeUserInfoRequest(AccessToken accessToken) throws IOException
    {
        // Prepare a userinfo request.
        UserInfoRequest request = new UserInfoRequest(userInfoEndpoint(), accessToken);

        // Send the request and receive a response.
        HTTPResponse response = sendRequest(request);

        // Parse the response.
        UserInfoResponse userInfoResponse = parseResponse(request, response,
                res -> UserInfoResponse.parse(response));

        // If the userinfo response indicates an error.
        if (!userInfoResponse.indicatesSuccess())
        {
            // Process the error response.
            throw processErrorResponse(
                    request.getEndpointURI(), userInfoResponse.toErrorResponse());
        }

        // Extract the user information from the response.
        return userInfoResponse.toSuccessResponse().getUserInfo();
    }


    private void validateUserInfo(UserInfo userInfo, Subject expectedSubject) throws IOException
    {
        // OpenID Connect Core 1.0, 5.3.2. Successful UserInfo Response
        //
        //   NOTE: Due to the possibility of token substitution attacks
        //   (see Section 16.11), the UserInfo Response is not guaranteed
        //   to be about the End-User identified by the sub (subject)
        //   element of the ID Token. The sub Claim in the UserInfo
        //   Response MUST be verified to exactly match the sub Claim in
        //   the ID Token; if they do not match, the UserInfo Response
        //   values MUST NOT be used.

        // If the subject in the ID token does not match the subject in
        // the userinfo response.
        if (!expectedSubject.equals(userInfo.getSubject()))
        {
            throw ioexception(
                    "The subject in the ID token does not match the subject in the userinfo response.");
        }
    }


    //------------------------------------------------------------
    // Federation Flow
    //------------------------------------------------------------


    /**
     * Create an authentication request that is to be sent to the authorization
     * endpoint of the OpenID Provider.
     */
    public URI createFederationRequest(
            String state, String codeVerifier) throws IOException
    {
        // state
        State st = (state != null) ? new State(state) : null;

        // Code verifier that is to be used to calculate code_challenge.
        CodeVerifier verifier = (codeVerifier != null)
                ? new CodeVerifier(codeVerifier) : null;

        // code_challenge_method
        CodeChallengeMethod method = (verifier != null)
                ? CodeChallengeMethod.S256 : null;

        // Create an authentication request that is to be sent to
        // the authorization endpoint.
        AuthenticationRequest request =
                buildAuthenticationRequest(st, verifier, method);

        return request.toURI();
    }


    /**
     * Process the authentication response from the authorization endpoint of
     * the OpenID Provider and retrieve user information from the userinfo
     * endpoint of the OpenID Provider.
     */
    public UserInfo processFederationResponse(
            URI authenticationResponse, String state, String codeVerifier) throws IOException
    {
        // state
        State st = (state != null) ? new State(state) : null;

        // code_verifier
        CodeVerifier verifier = (codeVerifier != null)
                ? new CodeVerifier(codeVerifier) : null;

        // Extract the authorization code from the authentication response.
        AuthorizationCode authorizationCode =
                extractAuthorizationCode(authenticationResponse, st);

        // Send a token request to the token endpoint and receive a response.
        OIDCTokenResponse tokenResponse =
                makeTokenRequest(authorizationCode, verifier);

        // ID token issued from the token endpoint.
        JWT idToken = tokenResponse.getOIDCTokens().getIDToken();

        // Validate the ID token.
        IDTokenClaimsSet idTokenClaims = validateIdToken(idToken);

        // Access token issued from the token endpoint.
        AccessToken accessToken = tokenResponse.getOIDCTokens().getAccessToken();

        // Send a request to the userinfo endpoint and receive a response.
        UserInfo userInfo = makeUserInfoRequest(accessToken);

        // Validate the userinfo.
        validateUserInfo(userInfo, idTokenClaims.getSubject());

        // User information obtained from the OpenID Provider.
        return userInfo;
    }
}
