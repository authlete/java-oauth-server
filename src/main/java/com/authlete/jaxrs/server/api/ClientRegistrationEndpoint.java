/*
 * Copyright (C) 2019 Authlete, Inc.
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


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseClientRegistrationEndpoint;


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
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            String json,
            @Context HttpServletRequest httpServletRequest)
    {
        return handleRegister(AuthleteApiFactory.getDefaultApi(), json, authorization);
    }


    /**
     * Dynamic client registration management endpoint, "get" functionality.
     */
    @GET
    @Path("/{id}")
    public Response get(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String clientId,
            @Context HttpServletRequest httpServletRequest)
    {
        return handleGet(AuthleteApiFactory.getDefaultApi(), clientId, authorization);
    }


    /**
     * Dynamic client registration management endpoint, "update" functionality.
     */
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String clientId,
            String json,
            @Context HttpServletRequest httpServletRequest)
    {
        return handleUpdate(AuthleteApiFactory.getDefaultApi(), clientId, json, authorization);
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
        return handleDelete(AuthleteApiFactory.getDefaultApi(), clientId, authorization);
    }
}
