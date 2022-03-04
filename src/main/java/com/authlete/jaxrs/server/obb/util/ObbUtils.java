/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.obb.util;


import static com.authlete.common.util.FapiUtils.X_FAPI_INTERACTION_ID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiException;
import com.authlete.common.dto.Client;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.dto.IntrospectionResponse.Action;
import com.authlete.common.util.FapiUtils;
import com.authlete.common.util.Utils;
import com.authlete.common.web.BearerToken;
import com.authlete.common.web.DpopToken;
import com.authlete.jaxrs.server.api.OBBCertValidator;
import com.authlete.jaxrs.server.obb.model.ResponseError;
import com.authlete.jaxrs.util.CertificateUtils;
import com.nimbusds.jwt.SignedJWT;


public class ObbUtils
{
    private static final SimpleDateFormat sDateFormat;


    static
    {
        sDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public static String formatDate(Date date)
    {
        return sDateFormat.format(date);
    }


    public static String formatNow()
    {
        return formatDate(new Date());
    }


    public static String computeOutgoingInteractionId(
            String code, String incomingInteractionId) throws WebApplicationException
    {
        try
        {
            // Compute the value for the 'x-fapi-interaction-id' HTTP response header.
            return FapiUtils.computeOutgoingInteractionId(incomingInteractionId);
        }
        catch (IllegalArgumentException e)
        {
            // The format of the incoming interaction ID is wrong.
        }

        throw badRequestException(null, code,
                "The format of the incoming 'x-fapi-interaction-id' is wrong.");
    }


    public static IntrospectionResponse validateAccessToken(
            String outgoingInteractionId, String code,
            AuthleteApi authleteApi, HttpServletRequest request,
            String... requiredScopes)
    {
        // Extract the access token from the Authorization header.
        String accessToken = extractAccessToken(request);

        // Extract the client certificate.
        String clientCertificate = CertificateUtils.extract(request);

        // Extract information required to validate any DPoP proof
        String dpop = request.getHeader("DPoP");
        String htm = request.getMethod();
        // This assumes that jetty has the correct incoming url; if running behind a reverse proxy it is important that
        // the jetty ForwardedRequestCustomizer is enabled and that the reverse proxy sets the relevants headers so
        // that jetty can determine the original url - e.g. in apache "RequestHeader set X-Forwarded-Proto https" is
        // required
        String htu = request.getRequestURL().toString();

        IntrospectionResponse response;

        try
        {
            // Call Authlete's /api/auth/introspection API.
            response = callIntrospection(
                    authleteApi, accessToken, requiredScopes, dpop, htm, htu, clientCertificate);
        }
        catch (AuthleteApiException e)
        {
            // Failed to call Authlete's /api/auth/interaction API.
            e.printStackTrace();

            throw internalServerErrorException(
                    outgoingInteractionId, code, e.getMessage());
        }

        // 'action' in the response denotes the next action which
        // this service implementation should take.
        Action action = response.getAction();

        // If the protected resource endpoint conforms to RFC 6750,
        // response.getResponseContent() can be used. However, the
        // protected resource endpoints of Open Banking Brasil behave
        // differently in error cases.
        String detail = response.getResultMessage();

        // Dispatch according to the action.
        switch (action)
        {
            case INTERNAL_SERVER_ERROR:
                // 500 Internal Server Error
                throw internalServerErrorException(
                        outgoingInteractionId, code, detail);

            case BAD_REQUEST:
                // 400 Bad Request
                throw badRequestException(
                        outgoingInteractionId, code, detail);

            case UNAUTHORIZED:
                // 401 Unauthorized
                throw unauthorizedException(
                        outgoingInteractionId, code, detail);

            case FORBIDDEN:
                // 403 Forbidden
                throw forbiddenException(
                        outgoingInteractionId, code, detail);

            case OK:
                // Return access token information.
                return response;

            default:
                // Unknown action. This never happens.
                throw unknownAction(
                        outgoingInteractionId, code, action);
        }
    }


    private static String extractAccessToken(HttpServletRequest request)
    {
        // The value of the "Authorization" header.
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Extract a DPoP access token from the value of Authorization header.
        String accessToken = DpopToken.parse(authorization);

        if (accessToken == null)
        {
            // if a DPoP token wasn't found, look for a Bearer in the authorization header
            accessToken = BearerToken.parse(authorization);
        }

        return accessToken;
    }


    private static IntrospectionResponse callIntrospection(
            AuthleteApi authleteApi, String accessToken,
            String[] requiredScopes, String dpop, String htm, String htu, String clientCertificate) throws AuthleteApiException
    {
        // Create a request to Authlete's /api/auth/introspection API.
        IntrospectionRequest request = new IntrospectionRequest()
                .setToken(accessToken)
                .setScopes(requiredScopes)
                .setDpop(dpop)
                .setHtm(htm)
                .setHtu(htu)
                .setClientCertificate(clientCertificate)
                ;

        // Call Authlete's /api/auth/introspection API.
        return authleteApi.introspection(request);
    }


    public static Response generateResponse(
            Status status, String outgoingInteractionId, Object entity)
    {
        ResponseBuilder builder = Response.status(status);

        if (outgoingInteractionId != null)
        {
            builder.header(X_FAPI_INTERACTION_ID, outgoingInteractionId);
        }

        if (entity != null)
        {
            builder.type(MediaType.APPLICATION_JSON_TYPE);
            builder.entity(entity instanceof String ? entity : Utils.toJson(entity, true));
        }

        return builder.build();
    }


    public static Response ok(String outgoingInteractionId, Object entity)
    {
        return generateResponse(Status.OK, outgoingInteractionId, entity);
    }


    public static Response created(String outgoingInteractionId, Object entity)
    {
        return generateResponse(Status.CREATED, outgoingInteractionId, entity);
    }


    public static Response noContent(String outgoingInteractionId)
    {
        return generateResponse(Status.NO_CONTENT, outgoingInteractionId, null);
    }


    public static Response generateErrorResponse(
            Status status, String outgoingInteractionId,
            String code, String title, String detail)
    {
        ResponseError entity = ResponseError.create(code, title, detail);

        return generateResponse(status, outgoingInteractionId, entity);
    }


    public static Response badRequest(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.BAD_REQUEST, outgoingInteractionId,
                code, "Bad Request", detail);
    }


    public static WebApplicationException badRequestException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                badRequest(outgoingInteractionId, code, detail));
    }


    public static Response unauthorized(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.UNAUTHORIZED, outgoingInteractionId,
                code, "Unauthorized", detail);
    }


    public static WebApplicationException unauthorizedException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                unauthorized(outgoingInteractionId, code, detail));
    }


    public static Response forbidden(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.FORBIDDEN, outgoingInteractionId,
                code, "Forbidden", detail);
    }


    public static WebApplicationException forbiddenException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                forbidden(outgoingInteractionId, code, detail));
    }


    public static Response notFound(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.NOT_FOUND, outgoingInteractionId,
                code, "Not Found", detail);
    }


    public static WebApplicationException notFoundException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                notFound(outgoingInteractionId, code, detail));
    }


    public static Response internalServerError(
            String outgoingInteractionId, String code, String detail)
    {
        return generateErrorResponse(
                Status.INTERNAL_SERVER_ERROR, outgoingInteractionId,
                code, "Internal Server Error", detail);
    }


    public static WebApplicationException internalServerErrorException(
            String outgoingInteractionId, String code, String detail)
    {
        return new WebApplicationException(
                internalServerError(outgoingInteractionId, code, detail));
    }


    private static WebApplicationException unknownAction(
            String outgoingInteractionId, String code,
            IntrospectionResponse.Action action)
    {
        String detail = String.format(
                "Unknow action '%s' from Authlete's introspection API.", action);

        return internalServerErrorException(
                outgoingInteractionId, code, detail);
    }


    public static String extractConsentScope(IntrospectionResponse response)
    {
        if (response == null)
        {
            return null;
        }

        return extractConsentScope(response.getScopes());
    }


    public static String extractConsentScope(String[] scopes)
    {
        if (scopes == null)
        {
            return null;
        }

        for (String scope : scopes)
        {
            if (scope == null)
            {
                continue;
            }

            if (scope.startsWith("consent:"))
            {
                return scope;
            }
        }

        return null;
    }


    /**
     * Judge whether the given request body represents a Dynamic Client
     * Registration request for Open Banking Brasil.
     *
     * @param requestBody
     *         The request body of an HTTP request.
     *
     * @return
     *         {@code true} if the given request body seems a Dynamic
     *         Client Registration request for Open Banking Brasil.
     *
     * @see <a href="https://openbanking-brasil.github.io/specs-seguranca/open-banking-brasil-dynamic-client-registration-1_ID1.html"
     *      >Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0 Implementers Draft 1</a>
     */
    @SuppressWarnings("unchecked")
    public static boolean isObbDcr(String requestBody)
    {
        // If the request does not have a body.
        if (requestBody == null)
        {
            return false;
        }

        Map<String, Object> params;

        try
        {
            // Try to parse the request body as JSON.
            params = Utils.fromJson(requestBody, Map.class);
        }
        catch (Exception e)
        {
            // Failed to parse the request body as JSON.
            return false;
        }

        if (params == null)
        {
            // Failed to create a Map instance from the request body?
            return false;
        }

        // If the request body does not contain "software_statement".
        if (!params.containsKey("software_statement"))
        {
            // A DCR request of Open Banking Brasil always contains a software statement.
            return false;
        }

        SignedJWT jwt;

        try
        {
            // Parse the value of "software_statement" as a signed JWT.
            jwt = SignedJWT.parse((String)params.get("software_statement"));
        }
        catch (Exception e)
        {
            // Failed to parse the "software_statement" as a signed JWT.
            return false;
        }

        String softwareJwksUri;

        try
        {
            // Get the value of the "software_jwks_uri" claim from the software statement.
            softwareJwksUri = jwt.getJWTClaimsSet().getStringClaim("software_jwks_uri");
        }
        catch (Exception e)
        {
            // Failed to retrieve the value of "software_jwks_uri".
            return false;
        }

        // If the software statement does not include the "software_jwks_uri" claim.
        if (softwareJwksUri == null)
        {
            // A software statement issued from the Directory of Open Banking Brasil
            // always contains "software_jwks_uri".
            return false;
        }

        // If the "software_jwks_uri" does not include "openbankingbrasil".
        if (softwareJwksUri.indexOf("openbankingbrasil") < 0)
        {
            // JWK Sets of Open Banking Brasil are hosted on
            // "https://keystore[.sandbox].directory.openbankingbrasil.org.br/"
            return false;
        }

        // The given request body seems to be a Dynamic Client Registration request
        // for Open Banking Brasil.
        return true;
    }


    /**
     * Judge whether the client identified by the client ID is a client that
     * has been dynamically registered for Open Banking Brasil.
     *
     * @param api
     *         An implementation of {@link AuthleteApi}.
     *
     * @param clientId
     *         A client ID.
     *
     * @return
     *         {@code true} if the client identified by the client ID is a
     *         client that has been dynamically registered for Open Banking
     *         Brasil.
     *
     * @see <a href="https://openbanking-brasil.github.io/specs-seguranca/open-banking-brasil-dynamic-client-registration-1_ID1.html"
     *      >Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0 Implementers Draft 1</a>
     */
    @SuppressWarnings("unchecked")
    public static boolean isObbDynamicClient(AuthleteApi api, String clientId)
    {
        Client client;

        try
        {
            // Get information about the client identified by the client ID
            // by calling Authlete's /api/client/get/{clientIdentifier} API.
            client = api.getClient(clientId);
        }
        catch (Exception e)
        {
            // The API call failed.
            return false;
        }

        if (client == null)
        {
            // Client information is not available.
            return false;
        }

        // Custom metadata of the client. This implementation assumes that
        // a client is registered with some client metadata.
        String json = client.getCustomMetadata();

        if (json == null)
        {
            // The client does not have custom metadata.
            return false;
        }

        Map<String, Object> metadata;

        try
        {
            // Parse the string as JSON.
            metadata = Utils.fromJson(json, Map.class);
        }
        catch (Exception e)
        {
            // Failed to parse the string as JSON.
            return false;
        }

        // This implementation assumes that the client's custom metadata
        // contains "software_roles". This assumption requires that the
        // configuration of "Supported Custom Client Metadata" property
        // of your Authlete 'Service' has been properly set up. The
        // property must include "software_roles" so that it can be
        // registered as custom metadata on dynamic client registration.

        // If the custom metadata does not contain "software_roles".
        if (!metadata.containsKey("software_roles"))
        {
            return false;
        }

        // The current implementation regards the client as a client
        // of Open Banking Brasil if the client's custom metadata
        // contains "software_roles". However, if other open banking
        // ecosystems start to use "software_roles", further logic
        // needs to be added here. For example, checking whether the
        // "software_roles" array contains an OBB-specific value such
        // as "DADOS" and "PAGTO". But, checking the presence of
        // "software_roles" is enough for now.

        return true;
    }


    /**
     * Judge whether the root certificate of the certificate chain that
     * consists of the presented client certificate and intermediate
     * certificates is a certificate issued by the authority of Open
     * Banking Brasil.
     *
     * @param request
     *         An HTTP request.
     *
     * @return
     *         {@code true} if the request contains a client certificate
     *         for Open Banking Brasil.
     */
    public static boolean includesObbCertificate(HttpServletRequest request)
    {
        try
        {
            // Validate the certificate chain included in the request.
            OBBCertValidator.getInstance().validate(request);

            // The request contains a client certificate issued for OBB.
            return true;
        }
        catch (Exception e)
        {
            // The request does not contain a client certificate for OBB.
            return false;
        }
    }
}
