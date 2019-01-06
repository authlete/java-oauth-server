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
import com.authlete.jaxrs.server.ad.type.Result;


/**
 * The class representing a callback request that is made from Authlete's CIBA authentication
 * device simulator when it is used in asynchronous mode.
 *
 * <p>
 * Note that, before the authorization server receives this callback request from the authentication
 * device simulator, the authorization server has made a request to the authentication
 * device simulator's {@code /api/authenticate/async} API for end-user authentication
 * and authorization. This callback request contains the result of the end-user
 * authentication and authorization.
 * </p>
 *
 * @author Hideki Ikeda
 */
public class AsyncAuthenticationCallbackRequest implements Serializable
{
    private static final long serialVersionUID = 1L;


    @XmlElement(name = "request_id")
    private String requestId;
    private Result result;
    private String state;


    /**
     * Get the ID of a request that the authorization server had made to the authentication
     * device simulator's {@code /api/authenticate/async} API before the authorization
     * server received this callback request from the authentication device simulator.
     *
     * @return
     *         The ID of a request that the authorization server had made to the
     *         authentication device simulator's {@code /api/authenticate/async}
     *         API before the authorization server received this callback request
     *         from the authentication device simulator.
     */
    public String getRequestId()
    {
        return requestId;
    }


    /**
     * Set the ID of a request that the authorization server had made to the authentication
     * device simulator's {@code /api/authenticate/async} API before the authorization
     * server received this callback request from the authentication device simulator.
     *
     * @param requestId
     *         The ID of a request that the authorization server had made to the
     *         authentication device simulator's {@code /api/authenticate/async}
     *         API before the authorization server received this callback request
     *         from the authentication device simulator.
     *
     * @return
     *         {@code this} object.
     */
    public AsyncAuthenticationCallbackRequest setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }


    /**
     * Get the result of end-user authentication and authorization.
     *
     * @return
     *         The result of end-user authentication and authorization
     */
    public Result getResult()
    {
        return result;
    }


    /**
     * Set the result of end-user authentication and authorization.
     *
     * @param result
     *         The result of end-user authentication and authorization
     *
     * @return
     *         {@code this} object.
     */
    public AsyncAuthenticationCallbackRequest setResult(Result result)
    {
        this.result = result;

        return this;
    }


    /**
     * Get the value of the {@code state} parameter that was included in a request
     * that the authorization server made to the authentication device simulator's
     * {@code /api/authenticate/async} API before the authorization server received
     * this callback request from the authentication device simulator.
     *
     * @return
     *         The value of the {@code state} parameter that was included in a
     *         request that the authorization server made to the authentication
     *         device simulator's {@code /api/authenticate/async} API before the
     *         authorization server received this callback request from the authentication
     *         device simulator.
     */
    public String getState()
    {
        return state;
    }


    /**
     * Set the value of the {@code state} parameter that was included in a request
     * that the authorization server made to the authentication device simulator's
     * {@code /api/authenticate/async} API before the authorization server received
     * this callback request from the authentication device simulator.
     *
     * @param state
     *         The value of the {@code state} parameter that was included in a
     *         request that the authorization server made to the authentication
     *         device simulator's {@code /api/authenticate/async} API before the
     *         authorization server received this callback request from the authentication
     *         device simulator.
     *
     * @return
     *         {@code this} object.
     */
    public AsyncAuthenticationCallbackRequest setState(String state)
    {
        this.state = state;

        return this;
    }
}
