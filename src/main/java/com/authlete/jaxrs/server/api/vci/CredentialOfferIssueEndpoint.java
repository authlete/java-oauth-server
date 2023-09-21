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


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.server.mvc.Viewable;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.CredentialOfferCreateRequest;
import com.authlete.common.dto.CredentialOfferCreateResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BaseEndpoint;
import com.authlete.jaxrs.server.util.ExceptionUtil;
import com.authlete.jaxrs.server.util.ProcessingUtil;


@Path("/api/offer/issue")
public class CredentialOfferIssueEndpoint extends BaseEndpoint
{
    @GET
    public Response get()
    {
        // Create a Viewable instance that represents the credential offer page.
        // Viewable is a class provided by Jersey for MVC.
        final Viewable viewable = new Viewable("/credential-offer", new CredentialOfferPageModel());

        // Create a response that has the viewable as its content.
        return Response.ok(viewable, MediaType.TEXT_HTML_TYPE.withCharset("UTF-8")).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session.
        final HttpSession session = ProcessingUtil.getSession(request);

        // Read request
        final Map<String, String> flatMap = ProcessingUtil.flattenMultivaluedMap(parameters);
        final CredentialOfferPageModel model = new CredentialOfferPageModel()
                .setValues(flatMap);

        final AuthleteApi api = AuthleteApiFactory.getDefaultApi();
        final User user = ProcessingUtil.getUser(session, parameters);

        if (user == null)
        {
            throw ExceptionUtil.badRequestException("Bad authentication.");
        }

        final CredentialOfferCreateRequest createRequest = model.toRequest(user);
        final CredentialOfferCreateResponse response = api.credentialOfferCreate(createRequest);

        switch (response.getAction())
        {
            case CREATED:
                model.setInfo(response.getInfo());
                model.setUser(user);

                // Create a Viewable instance that represents the credential offer page.
                // Viewable is a class provided by Jersey for MVC.
                final Viewable viewable = new Viewable("/credential-offer", model);

                // Create a response that has the viewable as its content.
                return Response.ok(viewable, MediaType.TEXT_HTML_TYPE.withCharset("UTF-8")).build();

            default:
                throw ExceptionUtil.badRequestException("An exception occured: " + response.getResultMessage());
        }
    }
}
