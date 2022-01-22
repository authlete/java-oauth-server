/*
 * Copyright (C) 2022 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.api;


import com.authlete.common.dto.AuthorizationResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.AuthorizationPageModel;
import com.authlete.jaxrs.server.federation.FederationConfig;


/**
 * Data used to render the authorization page.
 */
public class AuthzPageModel extends AuthorizationPageModel
{
    private static final long serialVersionUID = 1L;


    private FederationConfig[] federations;
    private String federationMessage;


    public AuthzPageModel(
            AuthorizationResponse info, User user, FederationConfig[] federations)
    {
        super(info, user);

        this.federations = federations;
    }


    /**
     * Get the configurations of ID federations.
     *
     * <p>
     * If this method returns a non-empty array, links for ID federation
     * will be displayed in the authorization page.
     * </p>
     */
    public FederationConfig[] getFederations()
    {
        return federations;
    }


    /**
     * Set the configurations of ID federations.
     */
    public AuthzPageModel setFederations(FederationConfig[] federations)
    {
        this.federations = federations;

        return this;
    }


    /**
     * Get the feedback message from the process of ID federation.
     *
     * <p>
     * If this method returns a non-null value, the message will be displayed
     * in the authorization page.
     * </p>
     */
    public String getFederationMessage()
    {
        return federationMessage;
    }


    /**
     * Set the feedback message from the process of ID federation.
     */
    public AuthzPageModel setFederationMessage(String message)
    {
        this.federationMessage = message;

        return this;
    }
}
