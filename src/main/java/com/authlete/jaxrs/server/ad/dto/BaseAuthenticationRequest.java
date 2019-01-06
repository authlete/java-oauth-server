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
 * A base class for request to Authlete's CIBA authentication device simlator's
 * APIs.
 *
 * @author Hideki Ikeda
 *
 * @param <T>
 *         Request type.
 */
public class BaseAuthenticationRequest<T extends BaseAuthenticationRequest<T>> implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String workspace;
    private String user;
    private String message;
    private int timeout;


    /**
     * Get the workspace on the authentication device simulator.
     *
     * @return
     *         The workspace on the authentication device simulator.
     */
    public String getWorkspace()
    {
        return workspace;
    }


    /**
     * Set the workspace on the authentication device simulator.
     *
     * @param workspace
     *         The workspace on the authentication device simulator.
     *
     * @return
     *         {@code this} object.
     */
    @SuppressWarnings("unchecked")
    public T setWorkspace(String workspace)
    {
        this.workspace = workspace;

        return (T)this;
    }


    /**
     * Get the ID of a user to be authenticated and asked to authorize the client
     * application.
     *
     * @return
     *         The ID of a user to be authenticated and asked to authorize the
     *         client application.
     */
    public String getUser()
    {
        return user;
    }


    /**
     * Set the ID of a user to be authenticated and asked to authorize the client
     * application.
     *
     * @param user
     *         The ID of a user to be authenticated and asked to authorize the
     *         client application.
     *
     * @return
     *         {@code this} object.
     */
    @SuppressWarnings("unchecked")
    public T setUser(String user)
    {
        this.user = user;

        return (T)this;
    }


    /**
     * Get a message to be shown to the end-user on the authentication device.
     *
     * @return
     *         A message to be shown to the end-user on the authentication device.
     */
    public String getMessage()
    {
        return message;
    }


    /**
     * Set a message to be shown to the end-user on the authentication device.
     *
     * @param message
     *         A message to be shown to the end-user on the authentication device.
     *
     * @return
     *         {@code this} object.
     */
    @SuppressWarnings("unchecked")
    public T setMessage(String message)
    {
        this.message = message;

        return (T)this;
    }


    /**
     * Get the value of timeout for end-user authentication and authorization.
     *
     * @return
     *         The value of timeout for end-user authentication and authorization.
     */
    public int getTimeout()
    {
        return timeout;
    }


    /**
     * Set the value of timeout for end-user authentication and authorization.
     *
     * @param message
     *         The value of timeout for end-user authentication and authorization.
     *
     * @return
     *         {@code this} object.
     */
    @SuppressWarnings("unchecked")
    public T setTimeout(int timeout)
    {
        this.timeout = timeout;

        return (T)this;
    }
}
