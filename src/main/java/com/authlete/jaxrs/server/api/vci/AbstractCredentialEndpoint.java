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
package com.authlete.jaxrs.server.api.vci;


import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialIssuerMetadataRequest;
import com.authlete.common.dto.CredentialIssuerMetadataResponse;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.ErrorCode;
import com.authlete.jaxrs.BaseResourceEndpoint;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.vc.InvalidCredentialRequestException;
import com.authlete.jaxrs.server.vc.OrderContext;
import com.authlete.jaxrs.server.vc.OrderFormat;
import com.authlete.jaxrs.server.vc.UnsupportedCredentialFormatException;
import com.authlete.jaxrs.server.vc.UnsupportedCredentialTypeException;
import com.google.gson.Gson;


public abstract class AbstractCredentialEndpoint extends BaseResourceEndpoint
{
    /**
     * Get the configured value of the endpoint of the credential issuer.
     * The value is used as the expected value of the {@code htu} claim
     * in the DPoP proof JWT.
     *
     * <p>
     * When {@code dpop} is null, this method returns null. Otherwise, this
     * method calls the {@code /vci/metadata} API to get the metadata of the
     * credential issuer, and extracts the value of the specified endpoint
     * from the metadata.
     * </p>
     *
     * @param api
     *         An instance of the {@link AuthleteApi} instance.
     *
     * @param dpop
     *         A DPoP proof JWT, specified by the {@code DPoP} HTTP header.
     *
     * @param endpointName
     *         The name of an endpoint, such as "{@code credential_endpoint}".
     *
     * @return
     *         The configured value of the endpoint. If {@code dpop} is null,
     *         this method returns null.
     */
    protected String computeHtu(AuthleteApi api, String dpop, String endpointName)
    {
        if (dpop == null)
        {
            // When a DPoP proof JWT is not available, computing the value
            // of "htu" is meaningless. We skip the computation to avoid
            // making a call to the /vci/metadata API.
            return null;
        }

        // Get the credential issuer metadata and extract the value of the
        // endpoint from the metadata.
        return (String)getCredentialIssuerMetadata(api).get(endpointName);
    }


    /**
     * Get the credential issuer metadata by calling the {@code /vci/metadata} API.
     *
     * @param api
     *         An instance of the {@link AuthleteApi} instance.
     *
     * @return
     *         The credential issuer metadata.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getCredentialIssuerMetadata(AuthleteApi api)
    {
        // Call the /vci/metadata API to get the metadata of the credential issuer.
        CredentialIssuerMetadataResponse response =
                api.credentialIssuerMetadata(new CredentialIssuerMetadataRequest());

        // The response content.
        String content = response.getResponseContent();

        // If something wrong was reported by the /vci/metadata API.
        if (response.getAction() != CredentialIssuerMetadataResponse.Action.OK)
        {
            // 500 Internal Server Error + application/json
            throw ExceptionUtil.internalServerErrorExceptionJson(content);
        }

        // Convert the credential issuer metadata into a Map instance.
        return new Gson().fromJson(content, Map.class);
    }


    /**
     * Validate the access token and get the information about it.
     *
     * @param req
     *         The HTTP request that this endpoint has received.
     *
     * @param api
     *         An instance of the {@link AuthleteApi} interface.
     *
     * @param at
     *         The access token.
     *
     * @param dpop
     *         A DPoP proof JWT, specified by the {@code DPoP} HTTP header.
     *
     * @param htu
     *         The URL of this endpoint, the expected value of the {@code htu}
     *         claim in the DPoP proof JWT.
     *
     * @return
     *         The response from the {@code /auth/introspection} API.
     */
    protected IntrospectionResponse introspect(
            HttpServletRequest req, AuthleteApi api,
            String at, String dpop, String htu)
    {
        // The client certificate. This is needed for certificate-bound
        // access tokens. See RFC 8705 for details.
        String certificate = extractClientCertificate(req);

        // The request to the /auth/introspection API.
        IntrospectionRequest request =
            new IntrospectionRequest()
                .setToken(at)
                .setClientCertificate(certificate)
                .setDpop(dpop)
                .setHtm("POST")
                .setHtu(htu)
                ;

        // Validate the access token.
        return validateAccessToken(api, request);
    }


    /**
     * Prepare additional HTTP headers that the response from this endpoint
     * should include.
     *
     * @param introspection
     *         The response from the {@code /auth/introspection} API.
     *
     * @return
     *         A map including pairs of a header name and a header value.
     */
    protected Map<String, Object> prepareHeaders(IntrospectionResponse introspection)
    {
        Map<String, Object> headers = new LinkedHashMap<>();

        // The expected nonce value for DPoP proof JWT.
        String dpopNonce = introspection.getDpopNonce();
        if (dpopNonce != null)
        {
            headers.put("DPoP-Nonce", dpopNonce);
        }

        return headers;
    }


    /**
     * Prepare a credential issuance order.
     *
     * @param context
     *         The context in which this method is called.
     *
     * @param introspection
     *         The response from the {@code /auth/introspection} API.
     *
     * @param info
     *         The information about the credential request.
     *
     * @param headers
     *         The additional headers that should be included in the response
     *         from this endpoint.
     *
     * @return
     *         A credential issuance order.
     */
    protected CredentialIssuanceOrder prepareOrder(
            OrderContext context,
            IntrospectionResponse introspection, CredentialRequestInfo info,
            Map<String, Object> headers)
    {
        try
        {
            // Get an OrderFormat instance corresponding to the credential format.
            OrderFormat format = getOrderFormat(info);

            // Let the processor for the format create a credential issuance
            // order based on the credential request.
            return format.getProcessor().toOrder(context, introspection, info);
        }
        catch (UnsupportedCredentialFormatException cause)
        {
            // 400 Bad Request + "error":"unsupported_credential_format"
            throw ExceptionUtil.badRequestExceptionJson(
                    errorJson(ErrorCode.unsupported_credential_format, cause), headers);
        }
        catch (UnsupportedCredentialTypeException cause)
        {
            // 400 Bad Request + "error":"unsupported_credential_type"
            throw ExceptionUtil.badRequestExceptionJson(
                    errorJson(ErrorCode.unsupported_credential_type, cause), headers);
        }
        catch (InvalidCredentialRequestException cause)
        {
            // 400 Bad Request + "error":"invalid_credential_request"
            throw ExceptionUtil.badRequestExceptionJson(
                    errorJson(ErrorCode.invalid_credential_request, cause), headers);
        }
        catch (WebApplicationException cause)
        {
            throw cause;
        }
        catch (Exception cause)
        {
            // 500 Internal Server Error + "error":"server_error"
            throw ExceptionUtil.internalServerErrorExceptionJson(
                    errorJson(ErrorCode.server_error, cause), headers);
        }
    }


    /**
     * Prepare credential issuance orders. The method is supposed to be called
     * from the implementation of the batch credential endpoint.
     *
     * @param introspection
     *         The response from the {@code /auth/introspection} API.
     *
     * @param infos
     *         The list of credential requests.
     *
     * @param headers
     *         The additional headers that should be included in the response
     *         from this endpoint.
     *
     * @return
     *         The list of credential issuance orders.
     */
    protected CredentialIssuanceOrder[] prepareOrders(
            IntrospectionResponse introspection, CredentialRequestInfo[] infos,
            Map<String, Object> headers)
    {
        // Convert the array of CredentialRequestInfo instances
        // into an array of CredentialIssuanceOrder instances.
        return Arrays.stream(infos)
                .map(info -> prepareOrder(OrderContext.BATCH, introspection, info, headers))
                .collect(Collectors.toList())
                .toArray(new CredentialIssuanceOrder[infos.length]);
    }


    private OrderFormat getOrderFormat(CredentialRequestInfo info) throws UnsupportedCredentialFormatException
    {
        // Get an OrderFormat instance that corresponds to the credential format.
        OrderFormat format = OrderFormat.byId(info.getFormat());

        // If the format is not supported.
        if (format == null)
        {
            throw new UnsupportedCredentialFormatException(String.format(
                    "The credential format '%s' is not supported.", info.getFormat()));
        }

        return format;
    }


    protected String errorJson(ErrorCode errorCode, Throwable cause)
    {
        if (cause == null)
        {
            return String.format(
                    "{%n  \"error\": \"%s\"%n}%n", errorCode.name());
        }

        return String.format(
                "{%n  \"error\": \"%s\",%n  \"error_description\": \"%s\"%n}%n",
                errorCode.name(), cause.getMessage());
    }
}
