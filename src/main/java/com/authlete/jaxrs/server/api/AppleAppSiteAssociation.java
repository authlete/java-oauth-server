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


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Allow our mobile app to claim the authorization endpoint
 *
 * See:
 *
 * https://openid.net/2019/10/21/guest-blog-implementing-app-to-app-authorisation-in-oauth2-openid-connect/
 *
 * https://developer.apple.com/documentation/uikit/inter-process_communication/allowing_apps_and_websites_to_link_to_your_content/enabling_universal_links
 */
@Path("/.well-known/apple-app-site-association")
public class AppleAppSiteAssociation
{
    /**
     * OpenID Provider configuration endpoint.
     */
    @GET
    public Response get()
    {
        String json =
                "{\n" +
                "    \"applinks\": {\n" +
                "        \"apps\": [],\n" +
                "        \"details\": [{\n" +
                "            \"appID\": \"337ZW7BQW9.com.authlete.fapidev-app2app\",\n" +
                "            \"paths\": [\"/api/authorization\"]\n" +
                "        }]\n" +
                "    }\n" +
                "}\n";
        return Response
                .status(Response.Status.OK)
                .entity(json).type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
