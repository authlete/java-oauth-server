/*
 * Copyright (C) 2017-2023 Authlete, Inc.
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
import javax.ws.rs.core.Response.Status;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.web.BasicCredentials;
import com.authlete.jaxrs.BaseIntrospectionEndpoint;
import com.authlete.jaxrs.IntrospectionRequestHandler.Params;
import com.authlete.jaxrs.server.db.ResourceServerDao;
import com.authlete.jaxrs.server.db.ResourceServerEntity;


/**
 * An implementation of introspection endpoint (<a href=
 * "http://tools.ietf.org/html/rfc7662">RFC 7662</a>).
 *
 * @see <a href="http://tools.ietf.org/html/rfc7662"
 *      >RFC 7662, OAuth 2.0 Token Introspection</a>
 *
 * @author Takahiko Kawasaki
 * @author Hideki Ikeda
 */
@Path("/api/introspection")
public class IntrospectionEndpoint extends BaseIntrospectionEndpoint
{
    /**
     * The introspection endpoint.
     *
     * @see <a href="http://tools.ietf.org/html/rfc7662#section-2.1"
     *      >RFC 7662, 2.1. Introspection Request</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam(HttpHeaders.ACCEPT) String accept,
            MultivaluedMap<String, String> parameters)
    {
        // "2.1. Introspection Request" in RFC 7662 says as follows:
        //
        //   To prevent token scanning attacks, the endpoint MUST also require
        //   some form of authorization to access this endpoint, such as client
        //   authentication as described in OAuth 2.0 [RFC6749] or a separate
        //   OAuth 2.0 access token such as the bearer token described in OAuth
        //   2.0 Bearer Token Usage [RFC6750].  The methods of managing and
        //   validating these authentication credentials are out of scope of this
        //   specification.
        //
        // Therefore, this API must be protected in some way or other.
        // Basic Authentication and Bearer Token are typical means, and
        // both use the value of the 'Authorization' header.

        BasicCredentials credentials = BasicCredentials.parse(authorization);

        // Fetch the information about the resource server from DB.
        ResourceServerEntity rsEntity = getResourceServer(credentials);

        // If failed to authenticate the resource server.
        if (authenticateResourceServer(rsEntity, credentials) == false)
        {
            // RFC 9701 mandates a "400 Bad Request" for unauthenticated introspection
            // requests as follows:
            //
            //   Note: An AS compliant with this specification MUST refuse to serve
            //   introspection requests that don't authenticate the caller and return
            //   an HTTP status code 400. This is done to ensure token data is released
            //   to legitimate recipients only and prevent downgrading to [RFC7662]
            //   behavior (see Section 8.2).
            //
            // However, we return "401 Unauthorized" instead here.
            // While RFC 7662 leaves authentication details out of scope, we consider
            // 401 the semantically correct HTTP status for API caller authentication
            // failures and the standard behavior for protected endpoints.

            // Return "401 Unauthorized".
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // Build a Param object to call the request handler.
        Params params = buildParams(parameters, accept, rsEntity);

        // Handle the introspection request.
        return handle(AuthleteApiFactory.getDefaultApi(), params);
    }


    private Params buildParams(
            MultivaluedMap<String, String> parameters, String accept, ResourceServerEntity rsEntity)
    {
        return new Params()
                .setParameters(parameters)
                .setHttpAcceptHeader(accept)
                .setRsUri(rsEntity.getUri())
                .setIntrospectionSignAlg(rsEntity.getIntrospectionSignAlg())
                .setIntrospectionEncryptionAlg(rsEntity.getIntrospectionEncryptionAlg())
                .setIntrospectionEncryptionEnc(rsEntity.getIntrospectionEncryptionEnc())
                .setPublicKeyForEncryption(rsEntity.getPublicKeyForIntrospectionResponseEncryption())
                .setSharedKeyForSign(rsEntity.getSharedKeyForIntrospectionResponseSign())
                .setSharedKeyForEncryption(rsEntity.getSharedKeyForIntrospectionResponseEncryption());
    }


    private ResourceServerEntity getResourceServer(BasicCredentials credentials)
    {
        if (credentials == null)
        {
            return null;
        }

        return ResourceServerDao.get(credentials.getUserId());
    }


    private boolean authenticateResourceServer(
            ResourceServerEntity rsEntity, BasicCredentials credentials)
    {
        return rsEntity != null &&
               rsEntity.getSecret().equals(credentials.getPassword());
    }
}
