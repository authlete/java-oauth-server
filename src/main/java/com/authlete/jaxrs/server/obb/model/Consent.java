/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.obb.model;


import java.io.Serializable;


public class Consent implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String consentId;
    private String[] permissions;
    private String status;
    private String creationDateTime;
    private String expirationDateTime;
    private String statusUpdateDateTime;
    private long clientId;
    private String refreshToken;


    public String getConsentId()
    {
        return consentId;
    }


    public Consent setConsentId(String consentId)
    {
        this.consentId = consentId;

        return this;
    }


    public String[] getPermissions()
    {
        return permissions;
    }


    public Consent setPermissions(String[] permissions)
    {
        this.permissions = permissions;

        return this;
    }


    public String getStatus()
    {
        return status;
    }


    public Consent setStatus(String status)
    {
        this.status = status;

        return this;
    }


    public String getCreationDateTime()
    {
        return creationDateTime;
    }


    public Consent setCreationDateTime(String creationDateTime)
    {
        this.creationDateTime = creationDateTime;

        return this;
    }


    public String getExpirationDateTime()
    {
        return expirationDateTime;
    }


    public Consent setExpirationDateTime(String expirationDateTime)
    {
        this.expirationDateTime = expirationDateTime;

        return this;
    }


    public String getStatusUpdateDateTime()
    {
        return statusUpdateDateTime;
    }


    public Consent setStatusUpdateDateTime(String statusUpdateDateTime)
    {
        this.statusUpdateDateTime = statusUpdateDateTime;

        return this;
    }


    public long getClientId()
    {
        return clientId;
    }


    public Consent setClientId(long clientId)
    {
        this.clientId = clientId;

        return this;
    }


    public String getRefreshToken()
    {
        return refreshToken;
    }


    public Consent setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;

        return this;
    }
}
