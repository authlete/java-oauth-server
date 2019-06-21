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
import com.authlete.jaxrs.server.ad.type.Mode;


/**
 * The factory class that creates a processor that communicates with
 * <a href="https://cibasim.authlete.com">Authlete CIBA authentication device simulator</a>
 * for end-user authentication and authorization.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication device
 *      simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/">Authlete
 *      CIBA authentication device simulator API</a>
 *
 * @author Hideki Ikeda
 */
public class AuthenticationDeviceProcessorFactory
{
    /**
     * Create a processor that communicates with the authentication device simulator
     * for end-user authentication and authorization.
     *
     * @param mode
     *         The mode communication with the authentication device simulator.
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
     *         for end-user authentication and authorization.
     */
    public static AuthenticationDeviceProcessor create(Mode mode, String ticket,
            User user, String clientName, String[] acrs, Scope[] scopes, String[] claimNames,
            String bindingMessage, String authReqId, int expiresIn)
    {
        if (mode == null)
        {
            throw new IllegalArgumentException("Mode must be specified.");
        }

        switch (mode)
        {
            case SYNC:
                // Create a processor that communicates with the authentication
                // device in synchronous mode.
                return new SyncAuthenticationDeviceProcessor(ticket, user, clientName,
                        acrs, scopes, claimNames, bindingMessage, authReqId, expiresIn);

            case ASYNC:
                // Create a processor that communicates with the authentication
                // device in asynchronous mode.
                return new AsyncAuthenticationDeviceProcessor(ticket, user, clientName,
                        acrs, scopes, claimNames, bindingMessage, authReqId, expiresIn);

            case POLL:
                // Create a processor that communicates with the authentication
                // device in poll mode.
                return new PollAuthenticationDeviceProcessor(ticket, user, clientName,
                        acrs, scopes, claimNames, bindingMessage, authReqId, expiresIn);

            default:
                // Undefined authentication device mode. This never happens.
                throw new RuntimeException("Undefined authentication device mode.");
        }
    }
}
