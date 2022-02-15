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


import com.authlete.common.api.AuthleteApi;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.obb.model.AccountData;
import com.authlete.jaxrs.server.obb.model.Links;
import com.authlete.jaxrs.server.obb.model.Meta;
import com.authlete.jaxrs.server.obb.model.ResponseAccountList;
import com.authlete.jaxrs.server.obb.util.ObbUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static com.authlete.common.util.FapiUtils.X_FAPI_INTERACTION_ID;


/**
 * Sample implementation of Accounts API of Open Banking Brasil.
 *
 * This is an alternative for FAPI2Baseline - it expects the 'fapi2base-accounts' scope, which has been set to
 * fapi2baseline.
 */
@Path("/api/obb/fapi2base-accounts")
public class FAPI2BaseAccountsEndpoint
{
    @GET
    public Response read(
            @Context HttpServletRequest request,
            @HeaderParam(X_FAPI_INTERACTION_ID) String incomingInteractionId)
    {
        String code = "Accounts Read";

        // Compute a value for the "x-fapi-interaction-id" HTTP response header.
        String outgoingInteractionId =
                ObbUtils.computeOutgoingInteractionId(code, incomingInteractionId);

        // Validate the access token.
        AuthleteApi authleteApi = AuthleteApiFactory.getDefaultApi();
        IntrospectionResponse info = ObbUtils.validateAccessToken(
                outgoingInteractionId, code, authleteApi, request, "fapi2base-accounts");

        // Make sure that the access token has a "consent:{consentId}" scope.
        ensureConsentScope(outgoingInteractionId, code, info);

        // Build a response body.
        ResponseAccountList body = buildResponseBody();

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


    private static ResponseAccountList buildResponseBody()
    {
        // Build dummy accounts..
        AccountData account = buildAccount();
        AccountData[] data  = new AccountData[] { account };
        Links links         = new Links().setSelf("/");
        Meta meta           = new Meta(1, 1, ObbUtils.formatNow());

        return new ResponseAccountList(data, links, meta);
    }


    private static AccountData buildAccount()
    {
        // Build a dummy account.
        return new AccountData()
                .setBrandName("Authlete Bank")
                .setCompanyCnpj("40156018000100")
                .setType("CONTA_DEPOSITO_A_VISTA")
                .setCompeCode("123")
                .setBranchCode("6272")
                .setNumber("94088392")
                .setCheckDigit("4")
                .setAccountId("291e5a29-49ed-401f-a583-193caa7aceee")
                ;
    }
}
