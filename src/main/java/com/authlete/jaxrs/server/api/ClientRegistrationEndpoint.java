/*
 * Copyright (C) 2019-2021 Authlete, Inc.
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


import java.security.GeneralSecurityException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.util.Utils;
import com.authlete.jaxrs.BaseClientRegistrationEndpoint;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


/**
 * An implementation of the dynamic client registration and
 * dynamic client registration management endpoints. This implementation
 * takes registration requests via POST to {@code /api/register} and
 * returns the resulting registered client as JSON. This implementation
 * takes client management requests via GET, PUT, and DELETE to
 * {@code /api/register/client_id}, where {@code client_id} is the
 * client ID of the registered client. This implementation will parse the
 * client ID from the incoming URL and pass it to the Authlete API.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7591">RFC 7591</a>
 *
 * @see <a href="https://tools.ietf.org/html/rfc7592">RFC 7592</a>
 *
 * @see <a href="https://openid.net/specs/openid-connect-registration-1_0.html"
 *      >OpenID Connect Dynamic Client Registration</a>
 */
@Path("/api/register")
public class ClientRegistrationEndpoint extends BaseClientRegistrationEndpoint
{
    /**
     * Dynamic client registration endpoint.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            String json,
            @Context HttpServletRequest httpServletRequest)
    {
        // The interface of Authlete APIs.
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Pre-process the request body as necessary.
        json = preprocessRequestBody(httpServletRequest, json);

        // Execute the "register" operation.
        return handleRegister(api, json, authorization);
    }


    /**
     * Dynamic client registration management endpoint, "read" functionality.
     */
    @GET
    @Path("/{id}")
    public Response read(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String clientId,
            @Context HttpServletRequest httpServletRequest)
    {
        // The interface of Authlete APIs.
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Extra process before executing the "read" operation.
        preprocessClient(httpServletRequest, api, clientId);

        // Execute the "read" operation.
        return handleGet(api, clientId, authorization);
    }


    /**
     * Dynamic client registration management endpoint, "update" functionality.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String clientId,
            String json,
            @Context HttpServletRequest httpServletRequest)
    {
        // The interface of Authlete APIs.
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Pre-process the request body as necessary.
        json = preprocessRequestBody(httpServletRequest, json);

        // Execute the "update" operation.
        return handleUpdate(api, clientId, json, authorization);
    }


    /**
     * Dynamic client registration management endpoint, "delete" functionality.
     */
    @DELETE
    @Path("/{id}")
    public Response delete(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String clientId,
            @Context HttpServletRequest httpServletRequest)
    {
        // The interface of Authlete APIs.
        AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        // Extra process before executing the "delete" operation.
        preprocessClient(httpServletRequest, api, clientId);

        // Execute the "delete" operation.
        return handleDelete(api, clientId, authorization);
    }


    private static void preprocessClient(
            HttpServletRequest request, AuthleteApi api, String clientId)
    {
        // If the client identified by the client ID seems a client
        // that has been dynamically registered for Open Banking Brasil.
        if (ObbUtils.isObbDynamicClient(api, clientId))
        {
            // Validate the client certificate.
            validateCertificate(request);
        }
    }


    private static String preprocessRequestBody(HttpServletRequest request, String requestBody)
    {
        // If the request body seems a Dynamic Client Registration request
        // for Open Banking Brasil or if the request includes a client
        // certificate for Open Banking Brasil.
        if (ObbUtils.isObbDcr(requestBody) ||
            ObbUtils.includesObbCertificate(request))
        {
            // Validate the client certificate.
            validateCertificate(request);

            // Perform validation specific to Open Banking Brasil.
            // The resultant map holds client metadata.
            Map<String, Object> metadata =
                    new OBBDCRProcessor().process(request, requestBody);

            return Utils.toJson(metadata);
        }
        else
        {
            // No pre-processing.
            return requestBody;
        }
    }


    private static void validateCertificate(HttpServletRequest request)
    {
        // Open Banking Brasil Financial-grade API Dynamic Client Registration 1.0
        // 7.1. Authorization server
        //
        //   1. shall reject dynamic client registration requests not performed
        //      over a connection secured with mutual tls using certificates
        //      issued by Brazil ICP (production) or the Directory of Participants
        //      (sandbox);

        try
        {
            // Validate the client certificate.
            OBBCertValidator.getInstance().validate(request);
        }
        catch (GeneralSecurityException e)
        {
            throw OBBDCRProcessor.errorResponse(Status.UNAUTHORIZED,
                    "invalid_client",
                    String.format("Client certificate validation failed: %s", e.getMessage()));
        }
    }
}
