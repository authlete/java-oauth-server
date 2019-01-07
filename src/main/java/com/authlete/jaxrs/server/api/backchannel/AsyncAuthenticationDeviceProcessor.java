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
package com.authlete.jaxrs.server.api.backchannel;


import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.ad.AuthenticationDevice;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationResponse;


/**
 * A processor that communicates with Authlete's CIBA authentication device simulator
 * in asynchronous mode.
 *
 * <p>
 * Note that this processor does not receive the result of end-user authentication
 * and authorization in {@link #process()} method. Instead, the result is obtained
 * in the {@link com.authlete.jaxrs.server.api.backchannel.BackchannelAuthenticationCallbackEndpoint
 * BackchannelAuthenticationCallbackEndpoint} when the endpoint is called back by
 * the authentication device simulator.
 * </p>
 *
 * @see com.authlete.jaxrs.server.api.backchannel.BackchannelAuthenticationCallbackEndpoint
 * BackchannelAuthenticationCallbackEndpoint
 *
 * @author Hideki Ikeda
 */
public class AsyncAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    /**
     * Construct a processor that communicates with Authlete's CIBA authentication
     * device simulator in asynchronous mode.
     *
     * @param ticket
     *         A ticket that was issued by Authlete's {@code /api/backchannel/authentication}
     *         API.
     *
     * @param user
     *         An end-user to be authenticated and asked to authorize the client
     *         application.
     *
     * @param clientName
     *         The name of the client application.
     *
     * @param acrs
     *         The requested ACRs.
     *
     * @param scopes
     *         The requested scopes.
     *
     * @param claimNames
     *         The names of the requested claims.
     *
     * @param bindingMessage
     *         The binding message to be shown to the end-user on the authentication
     *         device.
     *
     * @return
     *         A processor that communicates with Authlete's CIBA authentication
     *         device simulator in asynchronous mode.
     */
    public AsyncAuthenticationDeviceProcessor(String ticket, User user, String clientName, String[] acrs, Scope[] scopes, String[] claimNames, String bindingMessage)
    {
        super(ticket, user, clientName, acrs, scopes, claimNames, bindingMessage);
    }


    @Override
    public void process()
    {
        // The response to be returned from the authentication device.
        AsyncAuthenticationResponse response;

        try
        {
            // Communicate with the authentication device to authenticate the user and
            // authorize the client's request.
            response = AuthenticationDevice.asyncAuth(mUser.getSubject(), buildMessage());
        }
        catch (Throwable t)
        {
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithError();
            return;
        }

        // OK. The communication between this authorization server and the authentication
        // device has been successfully done.

        // The ID of the request that was sent to the authentication device.
        String requestId = response.getRequestId();

        System.out.println("obtained request ID: " + requestId);

        // Check the request ID.
        if (requestId == null || requestId.length() == 0)
        {
            // The request ID was invalid. This should never happen.
            completeWithError();
            return;
        }

        // OK. The request ID returned from the authentication device is valid.
        // In this case, the process does not complete here. Instead, the result
        // of end-user authentication and authorization will be returned from the
        // authentication device to the BackchannelAuthenticationCallbackEndpoint
        // of this authorization server later and then the authentication/authorization
        // process will complete there. Then, we need to store some information
        // required to complete the process (e.g. ticket, claim names, etc...) at
        // the BackchannelAuthenticationCallbackEndpoint.

        // Information about this process.
        AuthInfo info = new AuthInfo(mTicket, mUser, mClaimNames, mAcrs);

        // Store the information into the holder for later use.
        // See BackchannelAuthenticationCallbackEndpoint for more details.
        AuthInfoHolder.put(requestId, info);
    }
}
