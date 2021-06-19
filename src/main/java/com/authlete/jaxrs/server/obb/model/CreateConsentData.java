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
 * CreateConsent&#x2e;data
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_CreateConsent"
 *      >CreateConsent</a>
 */
public class CreateConsentData implements Serializable
{
    private static final long serialVersionUID = 1L;


    private LoggedUser loggedUser;
    private BusinessEntity businessEntity;
    private String[] permissions;
    private String expirationDateTime;
    private String transactionFromDateTime;
    private String transactionToDateTime;


    public LoggedUser getLoggedUser()
    {
        return loggedUser;
    }


    public CreateConsentData setLoggedUser(LoggedUser loggedUser)
    {
        this.loggedUser = loggedUser;

        return this;
    }


    public BusinessEntity getBusinessEntity()
    {
        return businessEntity;
    }


    public CreateConsentData setBusinessEntity(BusinessEntity businessEntity)
    {
        this.businessEntity = businessEntity;

        return this;
    }


    public String[] getPermissions()
    {
        return permissions;
    }


    public CreateConsentData setPermissions(String[] permissions)
    {
        this.permissions = permissions;

        return this;
    }


    public String getExpirationDateTime()
    {
        return expirationDateTime;
    }


    public CreateConsentData setExpirationDateTime(String datetime)
    {
        this.expirationDateTime = datetime;

        return this;
    }


    public String getTransactionFromDateTime()
    {
        return transactionFromDateTime;
    }


    public CreateConsentData setTransactionFromDateTime(String datetime)
    {
        this.transactionFromDateTime = datetime;

        return this;
    }


    public String getTransactionToDateTime()
    {
        return transactionToDateTime;
    }


    public CreateConsentData setTransactionToDateTime(String datetime)
    {
        this.transactionToDateTime = datetime;

        return this;
    }
}
