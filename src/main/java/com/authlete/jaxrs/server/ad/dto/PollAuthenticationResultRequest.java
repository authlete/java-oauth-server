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
import javax.xml.bind.annotation.XmlElement;


/**
 * A class representing a request to <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_result">
 * /api/authenticate/result API</a> of <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a>.
 *
 * <p>
 * Note that it is assumed that the authorization server has made a request to the
 * authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
 * /api/authenticate/poll API</a> for end-user authentication and authorization
 * before the authorization server makes this request to the authentication device
 * simulator.
 * </p>
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication
 *      device simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_result">
 *      /api/authenticate/result API</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
 *      /api/authenticate/poll API</a>
 *
 * @author Hideki Ikeda
 */
public class PollAuthenticationResultRequest implements Serializable
{
    private static final long serialVersionUID = 1L;


    @XmlElement(name = "request_id")
    private String requestId;


    /**
     * Get the ID of a request that the authorization server has made to the authentication
     * device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     * /api/authenticate/poll API</a> before the authorization server makes this
     * request to the authentication device simulator.
     *
     * @return
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     *         /api/authenticate/poll API</a> before the authorization server
     *         makes this request to the authentication device simulator.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     *      /api/authenticate/poll API</a>
     */
    public String getRequestId()
    {
        return requestId;
    }


    /**
     * Set the ID of a request that the authorization server has made to the authentication
     * device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     * /api/authenticate/poll API</a> before the authorization server makes this
     * request to the authentication device simulator.
     *
     * @param requestId
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     *         /api/authenticate/poll API</a> before the authorization server
     *         makes this request to the authentication device simulator.
     *
     * @return
     *         {@code this} object.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_poll">
     *      /api/authenticate/poll API</a>
     */
    public PollAuthenticationResultRequest setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }
}
