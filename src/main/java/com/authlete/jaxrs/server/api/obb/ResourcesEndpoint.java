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
package com.authlete.jaxrs.server.api.obb;


import static com.authlete.common.util.FapiUtils.X_FAPI_INTERACTION_ID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.obb.model.Links;
import com.authlete.jaxrs.server.obb.model.Meta;
import com.authlete.jaxrs.server.obb.model.Resource;
import com.authlete.jaxrs.server.obb.model.ResponseResourceList;
import com.authlete.jaxrs.server.obb.util.ObbUtils;


/**
 * Sample implementation of Resources API of Open Banking Brasil.
 */
@Path("/api/obb/resources")
public class ResourcesEndpoint
{
    @GET
    public Response read(
            @Context HttpServletRequest request,
            @HeaderParam(X_FAPI_INTERACTION_ID) String incomingInteractionId)
    {
        String code = "Resources Read";

        // Compute a value for the "x-fapi-interaction-id" HTTP response header.
        String outgoingInteractionId =
                ObbUtils.computeOutgoingInteractionId(code, incomingInteractionId);

        // Validate the access token.
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();
        IntrospectionResponse info = ObbUtils.validateAccessToken(
                outgoingInteractionId, code, authleteApi, request, "resources");

        // Make sure that the access token has a "consent:{consentId}" scope.
        ensureConsentScope(outgoingInteractionId, code, info);

        // Build a response body.
        ResponseResourceList body = buildResponseBody();

        // Build a successful response.
        return ObbUtils.ok(outgoingInteractionId, body);
    }


    private static void ensureConsentScope(
            String outgoingInteractionId, String code, IntrospectionResponse info)
    {
        // Extract a "consent:{consentId}" scope from the scope list of
        // the access token.
        String consentScope = ObbUtils.extractConsentScope(info);

        if (consentScope != null)
        {
            // Okay. The access token has a consent scope.
            return;
        }

        // The access token does not have a consent scope.
        throw ObbUtils.forbiddenException(outgoingInteractionId, code,
                "The access token does not have a consent scope.");
    }


    private static ResponseResourceList buildResponseBody()
    {
        // Build dummy resources.
        Resource resource = new Resource("resourceId", "type", "status");
        Resource[] data   = new Resource[] { resource };
        Links links       = new Links().setSelf("/");
        Meta meta         = new Meta(1, 1, ObbUtils.formatNow());

        return new ResponseResourceList(data, links, meta);
    }
}
