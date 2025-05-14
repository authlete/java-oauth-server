/*
 * Copyright (C) 2016-2025 Authlete, Inc.
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.Client;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationDecisionHandler.Params;
import com.authlete.jaxrs.BaseAuthorizationDecisionEndpoint;
import com.authlete.jaxrs.server.util.ProcessingUtil;
import com.authlete.jaxrs.spi.AuthorizationDecisionHandlerSpi;


/**
 * The endpoint that receives a request from the form in the authorization page.
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/authorization/decision")
public class AuthorizationDecisionEndpoint extends BaseAuthorizationDecisionEndpoint
{
    private static void addTxnToClaimNames(Params params) {
        // txn claim is always required by ConnectID Australia
        // https://cdn.connectid.com.au/specifications/digitalid-identity-assurance-profile-06.html
        String[] claimNames = params.getClaimNames();
        if (claimNames == null) {
            // if no claims were requested it can't be a connectid au request
            return;
        }
        // txn will now be returned for any requests that request oidc claims - as our AS is multipurpose there's no
        // real good way to identify the ecosystem variant being tested and returning an random uuid is harmless
        ArrayList<String> claimNamesArray = new ArrayList<>(Arrays.asList(claimNames));
        claimNamesArray.add("txn");

        params.setClaimNames(claimNamesArray.toArray(new String[0]));
    }

    /**
     * Process a request from the form in the authorization page.
     *
     * <p>
     * NOTE:
     * A better implementation would re-display the authorization page
     * when the pair of login ID and password is wrong, but this
     * implementation does not do it for brevity. A much better
     * implementation would check the login credentials by Ajax.
     * </p>
     *
     * @param request
     *         A request from the form in the authorization page.
     *
     * @param parameters
     *         Request parameters.
     *
     * @return
     *         A response to the user agent. Basically, the response
     *         will trigger redirection to the client's redirect
     *         endpoint.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @Context HttpServletRequest request,
            MultivaluedMap<String, String> parameters)
    {
        // Get the existing session.
        HttpSession session = ProcessingUtil.getSession(request);

        // Retrieve some variables from the session. See the implementation
        // of AuthorizationRequestHandlerSpiImpl.getAuthorizationPage().
        Params params = (Params)  takeAttribute(session, "params");
        String[] acrs = (String[])takeAttribute(session, "acrs");
        Client client = (Client)  takeAttribute(session, "client");
        User user     = ProcessingUtil.getUser(session, parameters);
        Date authTime = (Date)    session.getAttribute("authTime");

        addTxnToClaimNames(params);

        // Claims requested to be embedded in the ID token.
        String idTokenClaims = (params != null) ? params.getIdTokenClaims() : null;

        // Implementation of AuthorizationDecisionHandlerSpi.
        AuthorizationDecisionHandlerSpi spi =
            new AuthorizationDecisionHandlerSpiImpl(
                parameters, user, authTime, idTokenClaims, acrs, client,
                session.getId());

        // Handle the end-user's decision.
        return handle(AuthleteApiFactory.getDefaultApi(), spi, params);
    }

}
