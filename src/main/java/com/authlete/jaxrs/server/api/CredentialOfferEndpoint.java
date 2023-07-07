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
package com.authlete.jaxrs.server.api;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialOfferInfoRequest;
import com.authlete.common.dto.CredentialOfferInfoResponse;
import com.authlete.jaxrs.BaseEndpoint;
import com.authlete.jaxrs.server.util.ExceptionUtil;


@Path("/api/offer/{identifier}")
public class CredentialOfferEndpoint extends BaseEndpoint
{
    @GET
    public Response get(
            @PathParam("identifier") String identifier)
    {
        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();

        final CredentialOfferInfoRequest infoRequest = new CredentialOfferInfoRequest()
                .setIdentifier(identifier);
        final CredentialOfferInfoResponse response = api.credentialOfferInfo(infoRequest);

        switch(response.getAction())
        {
            default:
                throw ExceptionUtil.badRequestException("An exception occured: " + response.getResultMessage());
            case OK:
                final CredentialOfferPageModel model = new CredentialOfferPageModel();
                model.setInfo(response.getInfo());

                // Create a Viewable instance that represents the credential offer page.
                // Viewable is a class provided by Jersey for MVC.
                final Viewable viewable = new Viewable("/credential-offer", model);

                // Create a response that has the viewable as its content.
                return Response.ok(viewable, MediaType.TEXT_HTML_TYPE.withCharset("UTF-8")).build();
        }
    }
}
