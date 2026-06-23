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


import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jakarta.BaseGrantManagementEndpoint;


/**
 * An implementation of Grant Management Endpoint.
 *
 * @see <a href="https://openid.net/specs/fapi-grant-management.html"
 *      >Grant Management for OAuth 2.0</a>
 */
@Path("/api/gm")
public class GrantManagementEndpoint extends BaseGrantManagementEndpoint
{
    /**
     * The entry point for grant management 'query' requests.
     */
    @GET
    @Path("{grantId}")
    public Response query(
            @Context HttpServletRequest req,
            @PathParam("grantId") String grantId)
    {
        // Handle the grant management 'query' request.
        return handle(AuthleteApiFactory.getDefaultApi(), req, grantId);
    }


    /**
     * The entry point for grant management 'revoke' requests.
     */
    @DELETE
    @Path("{grantId}")
    public Response revoke(
            @Context HttpServletRequest req,
            @PathParam("grantId") String grantId)
    {
        // Handle the grant management 'revoke' request.
        return handle(AuthleteApiFactory.getDefaultApi(), req, grantId);
    }
}
