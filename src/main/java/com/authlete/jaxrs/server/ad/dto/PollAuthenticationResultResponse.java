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
import com.authlete.jaxrs.server.ad.type.Result;
import com.authlete.jaxrs.server.ad.type.Status;


/**
 * A class representing a request from <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_result">
 * /api/authenticate/result API</a> of <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a>.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication
 *      device simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_result">
 *      /api/authenticate/result API</a>
 *
 * @author Hideki Ikeda
 */
public class PollAuthenticationResultResponse implements Serializable
{
    private static final long serialVersionUID = 1L;


    Status status;
    Result result;


    /**
     * Get the status of end-user authentication and authorization.
     *
     * @return
     *         The status of end-user authentication and authorization.
     */
    public Status getStatus()
    {
        return status;
    }


    /**
     * Set the status of end-user authentication and authorization.
     *
     * @param status
     *         The status of end-user authentication and authorization.
     */
    public PollAuthenticationResultResponse setStatus(Status status)
    {
        this.status = status;

        return this;
    }


    /**
     * Get the result of end-user authentication and authorization.
     *
     * @return
     *         The result of end-user authentication and authorization. {@code null}
     *         is returned if the end-user authentication and authorization has
     *         not completed yet.
     */
    public Result getResult()
    {
        return result;
    }


    /**
     * Set the result of end-user authentication and authorization.
     *
     * @param result
     *         The result of end-user authentication and authorization.
     *
     * @return
     *         {@code this} object.
     */
    public PollAuthenticationResultResponse setResult(Result result)
    {
        this.result = result;

        return this;
    }
}
