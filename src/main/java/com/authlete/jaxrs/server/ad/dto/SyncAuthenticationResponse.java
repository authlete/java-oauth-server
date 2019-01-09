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


/**
 * A class representing a response from <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_sync">
 * /api/authenticate/sync API</a> of <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a>.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication
 *      device simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim/1.0.0#/default/post_api_authenticate_sync">
 *      /api/authenticate/sync API</a>
 *
 * @author Hideki Ikeda
 */
public class SyncAuthenticationResponse implements Serializable
{
    private static final long serialVersionUID = 1L;


    private Result result;


    /**
     * Get the result of end-user authentication and authorization.
     *
     * @return
     *         The result of end-user authentication and authorization.
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
    public SyncAuthenticationResponse setResult(Result result)
    {
        this.result = result;

        return this;
    }
}
