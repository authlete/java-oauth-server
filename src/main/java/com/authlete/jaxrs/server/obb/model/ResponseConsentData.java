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


/**
 * ResponseConsent&#x2e;data
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_ResponseConsent"
 *      >ResponseConsent</a>
 */
public class ResponseConsentData implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String consentId;
    private String creationDateTime;
    private String status;
    private String statusUpdateDateTime;
    private String[] permissions;
    private String expirationDateTime;
    private String transactionFromDateTime;
    private String transactionToDateTime;
    private Links links;
    private Meta meta;


    public String getConsentId()
    {
        return consentId;
    }


    public ResponseConsentData setConsentId(String consentId)
    {
        this.consentId = consentId;

        return this;
    }


    public String getCreationDateTime()
    {
        return creationDateTime;
    }


    public ResponseConsentData setCreationDateTime(String datetime)
    {
        this.creationDateTime = datetime;

        return this;
    }


    public String getStatus()
    {
        return status;
    }


    public ResponseConsentData setStatus(String status)
    {
        this.status = status;

        return this;
    }


    public String getStatusUpdateDateTime()
    {
        return statusUpdateDateTime;
    }


    public ResponseConsentData setStatusUpdateDateTime(String datetime)
    {
        this.statusUpdateDateTime = datetime;

        return this;
    }


    public String[] getPermissions()
    {
        return permissions;
    }


    public ResponseConsentData setPermissions(String[] permissions)
    {
        this.permissions = permissions;

        return this;
    }


    public String getExpirationDateTime()
    {
        return expirationDateTime;
    }


    public ResponseConsentData setExpirationDateTime(String datetime)
    {
        this.expirationDateTime = datetime;

        return this;
    }


    public String getTransactionFromDateTime()
    {
        return transactionFromDateTime;
    }


    public ResponseConsentData setTransactionFromDateTime(String datetime)
    {
        this.transactionFromDateTime = datetime;

        return this;
    }


    public String getTransactionToDateTime()
    {
        return transactionToDateTime;
    }


    public ResponseConsentData setTransactionToDateTime(String datetime)
    {
        this.transactionToDateTime = datetime;

        return this;
    }


    public Links getLinks()
    {
        return links;
    }


    public ResponseConsentData setLinks(Links links)
    {
        this.links = links;

        return this;
    }


    public Meta getMeta()
    {
        return meta;
    }


    public ResponseConsentData setMeta(Meta meta)
    {
        this.meta = meta;

        return this;
    }
}
