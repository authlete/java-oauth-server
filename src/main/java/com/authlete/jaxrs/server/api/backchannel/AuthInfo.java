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


import com.authlete.common.types.User;


/**
 * Information required to complete processes that are executed in {@link AsyncAuthenticationDeviceProcessor}
 *
 * @see AsyncAuthenticationDeviceProcessor
 *
 * @author Hideki Ikeda
 */
public class AuthInfo
{
    String mTicket;
    User mUser;
    String[] mClaimNames;
    String[] mAcrs;


    /**
     * Construct an information to complete processes that are executed in {@link
     * AsyncAuthenticationDeviceProcessor}
     *
     * @param ticket
     *         A ticket that was issued by Authlete's {@code /api/backchannel/authentication}
     *         API.
     *
     * @param user
     *         The end-user who was requested to authorize the client application.
     *
     * @param claimNames
     *         The names of the requested claims.
     *
     * @param acrs
     *         The requested ACRs.
     */
    public AuthInfo(String ticket, User user, String[] claimNames, String[] acrs)
    {
        mTicket     = ticket;
        mUser       = user;
        mClaimNames = claimNames;
        mAcrs       = acrs;
    }


    /**
     * Get the ticket that was issued by Authlete's {@code /api/backchannel/authentication}
     * API.
     *
     * @return
     *         The ticket that was issued by Authlete's {@code /api/backchannel/authentication}
     *         API.
     */
    public String getTicket()
    {
        return mTicket;
    }


    /**
     * Get The end-user who was requested to authorize the client application.
     *
     * @return
     *         The end-user who was requested to authorize the client application.
     */
    public User getUser()
    {
        return mUser;
    }


    /**
     * Get the names of the requested claims.
     *
     * @return
     *         The names of the requested claims.
     */
    public String[] getClaimNames()
    {
        return mClaimNames;
    }


    /**
     * Get the requested ACRs.
     *
     * @return
     *         The requested ACRs.
     */
    public String[] getAcrs()
    {
        return mAcrs;
    }
}
