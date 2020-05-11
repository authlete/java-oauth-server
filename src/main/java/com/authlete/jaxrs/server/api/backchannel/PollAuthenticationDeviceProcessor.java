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
import com.authlete.jaxrs.server.ServerConfig;
import com.authlete.jaxrs.server.ad.AuthenticationDevice;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResponse;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResultResponse;


/**
 * A processor that communicates with <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a> for end-user authentication
 * and authorization in poll mode.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication device
 *      simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">Authlete
 *      CIBA authentication device simulator API</a>
 *
 * @author Hideki Ikeda
 */
public class PollAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    /**
     * The maximum number of polling trials.
     */
    private static final int POLL_MAX_COUNT = ServerConfig.getAuthleteAdPollMaxCount();


    /**
     * The period of time in milliseconds for which this authorization server waits
     * between polling trials.
     */
    private static final int POLL_INTERVAL  = ServerConfig.getAuthleteAdPollInterval();


    /**
     * Construct a processor that communicates with the authentication device
     * simulator for end-user authentication and authorization in poll mode.
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
     * @param authReqId
     *         The authentication request ID ({@code auth_req_id}) issued to the
     *         client.
     *
     * @param expiresIn
     *         The duration of the issued authentication request ID ({@code auth_req_id})
     *         in seconds.
     *
     * @return
     *         A processor that communicates with the authentication device simulator
     *         for end-user authentication and authorization in poll mode.
     */
    public PollAuthenticationDeviceProcessor(String ticket, User user, String clientName,
            String[] acrs, Scope[] scopes, String[] claimNames, String bindingMessage,
            String authReqId, int expiresIn)
    {
        super(ticket, user, clientName, acrs, scopes, claimNames, bindingMessage,
                authReqId, expiresIn);
    }


    @Override
    public void process()
    {
        // The response to be returned from the authentication device.
        PollAuthenticationResponse response;

        try
        {
            // Communicate with the authentication device for end-user authentication
            // and authorization.
            response = AuthenticationDevice.poll(mUser.getSubject(), buildMessage(),
                    computeAuthTimeout(), mAuthReqId);
        }
        catch (Throwable t)
        {
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithTransactionFailed(
                    "Failed to communicate with the authentication device in poll mode.");
            return;
        }

        // OK. The communication between this authorization server and the authentication
        // device has been successfully done.

        // The ID of the request sent to the authentication device above.
        String requestId = response.getRequestId();

        // Check the request ID.
        if (requestId == null || requestId.length() == 0)
        {
            // The request ID was invalid. This should never happen.
            completeWithTransactionFailed(
                    "The request ID returned from the authentication device is invalid.");
            return;
        }

        // Start polling against the authentication device to fetch the result of
        // the end-user authentication and authorization.
        poll(requestId);
    }


    private void poll(String requestId)
    {
        PollAuthenticationResultResponse response = null;

        for (int count = 1; count <= POLL_MAX_COUNT; count++)
        {
            try
            {
                // Get the result of the end-user authentication and authorization
                // from the authentication device in poll mode.
                response = AuthenticationDevice.pollResult(requestId);
            }
            catch (Throwable t)
            {
                // Failed to fetch the result.
                completeWithTransactionFailed(
                        "Failed to fetch the result of the end-user authentication "
                      + "and authorization from the authentication device");
                return;
            }

            // The status of the end-user authentication authorization on the
            // authentication device.
            com.authlete.jaxrs.server.ad.type.Status status = response.getStatus();

            if (status == null)
            {
                // The status returned from the authentication device is empty.
                // This should never happen.
                completeWithTransactionFailed(
                        "The status returned from the authentication device is empty.");
                return;
            }

            switch (status)
            {
                //
                // When the end-user authentication and authorization has not
                // been done yet.
                //
                case active:
                    if (count == POLL_MAX_COUNT)
                    {
                        // The poll trial count reached the maximum count.
                        completeWithTransactionFailed(
                                "The authentication device returned status of 'active' "
                              + "but the authorization server gave up polling the "
                              + "result of the end-user authentication and authorization "
                              + "since polling count reached the maximum count.");
                        return;
                    }

                    // Retry to fetch the result after an interval.
                    sleepForInterval(POLL_INTERVAL);
                    break;

                //
                // When the end-user authentication and authorization was done.
                //
                case complete:
                    handleResult(response.getResult());
                    return;

                //
                // When the end-user authentication and authorization was timed
                // out.
                //
                case timeout:
                    completeWithTransactionFailed(
                            "The task delegated to the authentication device timed out.");
                    return;

                //
                // When an unknown result returned from the authentication device.
                //
                default:
                    completeWithTransactionFailed(
                            "The authentication device returned an unrecognizable status.");
                    return;
            }
        }
    }


    private void sleepForInterval(int interval)
    {
        try
        {
            Thread.sleep(interval);
        }
        catch (InterruptedException e)
        {
        }
    }
}
