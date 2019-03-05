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
 * A base class for a request to an <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">
 * Authlete CIBA authentication device simulator API</a>.
 *
 * @param <T>
 *         Request type.
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">Authlete
 *      CIBA authentication device simulator API</a>
 *
 * @author Hideki Ikeda
 */
public class BaseAuthenticationRequest<T extends BaseAuthenticationRequest<T>> implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String workspace;
    private String user;
    private String message;
    private int timeout;

    @XmlElement(name = "actionize_token")
    private String actionizeToken;


    /**
     * Get the workspace for which end-user authentication and authorization is
     * performed.
     *
     * @return
     *         The workspace for which end-user authentication and authorization
     *         is performed.
     */
    public String getWorkspace()
    {
        return workspace;
    }


    /**
     * Set the workspace for which end-user authentication and authorization is
     * performed.
     *
     * @param workspace
     *         The workspace for which end-user authentication and authorization
     *         is performed.
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
     * Get the ID of an end-user to be authenticated and asked to authorize the
     * client application.
     *
     * @return
     *         The ID of an end-user to be authenticated and asked to authorize
     *         the client application.
     */
    public String getUser()
    {
        return user;
    }


    /**
     * Set the ID of an end-user to be authenticated and asked to authorize the
     * client application.
     *
     * @param user
     *         The ID of an end-user to be authenticated and asked to authorize
     *         the client application.
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
     * Get the authentication/authorization timeout value in seconds.
     *
     * <p>
     * The authentication device waits for this timeout value to get authorization
     * decision from an end-user.
     * </p>
     *
     * @return
     *         The authentication/authorization timeout value in seconds.
     */
    public int getTimeout()
    {
        return timeout;
    }


    /**
     * Set the authentication/authorization timeout value in seconds.
     *
     * <p>
     * The authentication device waits for this timeout value to get authorization
     * decision from an end-user.
     * </p>
     *
     * @param timeout
     *         The authentication/authorization timeout value in seconds.
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


    /**
     * Get a token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     * to automate authentication device responses.
     *
     * @return
     *         A token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     *         to automate authentication device responses.
     */
    public String getActionizeToken()
    {
        return actionizeToken;
    }


    /**
     * Set a token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     * to automate authentication device responses.
     *
     * @param actionizeToken
     *         A token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     *         to automate authentication device responses.
     *
     * @return
     *         {@code this} object.
     */
    @SuppressWarnings("unchecked")
    public T setActionizeToken(String actionizeToken)
    {
        this.actionizeToken = actionizeToken;

        return (T)this;
    }
}
