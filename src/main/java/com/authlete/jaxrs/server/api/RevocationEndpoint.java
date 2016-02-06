/*
 * Copyright (C) 2016 Authlete, Inc.
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


import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseRevocationEndpoint;


/**
 * An implementation of revocation endpoint (<a href=
 * "http://tools.ietf.org/html/rfc7009">RFC 7009</a>).
 *
 * @see <a href="http://tools.ietf.org/html/rfc7009"
 *      >RFC 7009, OAuth 2.0 Token Revocation</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/revocation")
public class RevocationEndpoint extends BaseRevocationEndpoint
{
    /**
     * The revocation endpoint for {@code POST} method.
     *
     * @see <a href="http://tools.ietf.org/html/rfc7009#section-2.1"
     *      >RFC 7009, 2.1. Revocation Request</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            MultivaluedMap<String, String> parameters)
    {
        // Handle the revocation request.
        return handle(AuthleteApiFactory.getDefaultApi(), parameters, authorization);
    }
}
