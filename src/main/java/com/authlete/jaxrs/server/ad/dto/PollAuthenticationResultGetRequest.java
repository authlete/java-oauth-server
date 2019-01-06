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
package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;


/**
 * The class representing a request to Authlete's CIBA authentication device
 * simulator's {@code /api/authenticate/result} API.
 *
 * <p>
 * Note that, before the authorization server makes this request to the authentication
 * device simulator, the authorization server has made a request to the authentication
 * device simulator's {@code /api/authenticate/poll} API for end-user authentication
 * and authorization.
 * </p>
 *
 * @author Hideki Ikeda
 */
public class PollAuthenticationResultGetRequest  implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String requestId;


    /**
     * Get the ID of a request that the authorization server has made to the authentication
     * device simulator's {@code /api/authenticate/poll} API before the authorization
     * server makes this request to the authentication device simulator.
     *
     * @return
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's {@code /api/authenticate/poll}
     *         API before the authorization server makes this request to the authentication
     *         device simulator.
     */
    public String getRequestId()
    {
        return requestId;
    }


    /**
     * Set the ID of a request that the authorization server has made to the authentication
     * device simulator's {@code /api/authenticate/poll} API before the authorization
     * server makes this request to the authentication device simulator.
     *
     * @param requestId
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's {@code /api/authenticate/poll}
     *         API before the authorization server makes this request to the authentication
     *         device simulator.
     *
     * @return
     *         {@code this} object.
     */
    public PollAuthenticationResultGetRequest setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }
}
