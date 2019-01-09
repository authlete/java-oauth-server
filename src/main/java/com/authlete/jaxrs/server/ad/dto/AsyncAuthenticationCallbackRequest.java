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
 * The class representing a callback request that is made from <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a> when it is used in asynchronous
 * mode.
 *
 * <p>
 * Note that, before the authorization server receives this callback request from
 * the authentication device simulator, it is assumed that the authorization server
 * has made a request to the authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
 * /api/authenticate/async API</a> for end-user authentication and authorization.
 * This callback request contains the result of the end-user authentication and
 * authorization.
 * </p>
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication
 *      device simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
 *      /api/authenticate/async API</a>
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
     * Get the ID of a request that the authorization server has made to the authentication
     * device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     * /api/authenticate/async API</a> before the authorization server receives
     * this callback request from the authentication device simulator.
     *
     * @return
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *         /api/authenticate/async API</a> before the authorization server
     *         receives this callback request from the authentication device simulator.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *      /api/authenticate/async API</a>
     */
    public String getRequestId()
    {
        return requestId;
    }


    /**
     * Set the ID of a request that the authorization server has made to the authentication
     * device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     * /api/authenticate/async API</a> before the authorization server receives
     * this callback request from the authentication device simulator.
     *
     * @param requestId
     *         The ID of a request that the authorization server has made to the
     *         authentication device simulator's <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *         /api/authenticate/async API</a> before the authorization server
     *         receives this callback request from the authentication device simulator.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *      /api/authenticate/async API</a>
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
     * <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     * /api/authenticate/async API</a> before the authorization server receives
     * this callback request from the authentication device simulator.
     *
     * @return
     *         The value of the {@code state} parameter that was included in a request
     *         that the authorization server made to the authentication device simulator's
     *         <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *         /api/authenticate/async API</a> before the authorization server
     *         receives this callback request from the authentication device simulator.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *      /api/authenticate/async API</a>
     */
    public String getState()
    {
        return state;
    }


    /**
     * Set the value of the {@code state} parameter that was included in a request
     * that the authorization server made to the authentication device simulator's
     * <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     * /api/authenticate/async API</a> before the authorization server receives
     * this callback request from the authentication device simulator.
     *
     * @param state
     *         The value of the {@code state} parameter that was included in a request
     *         that the authorization server made to the authentication device simulator's
     *         <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *         /api/authenticate/async API</a> before the authorization server
     *         receives this callback request from the authentication device simulator.
     *
     * @return
     *         {@code this} object.
     *
     * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_async">
     *      /api/authenticate/async API</a>
     */
    public AsyncAuthenticationCallbackRequest setState(String state)
    {
        this.state = state;

        return this;
    }
}
