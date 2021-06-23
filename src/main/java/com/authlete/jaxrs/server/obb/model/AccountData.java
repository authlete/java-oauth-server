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
 * AccountData
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_AccountData"
 *      >AccountData</a>
 */
public class AccountData implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String brandName;
    private String companyCnpj;
    private String type;
    private String compeCode;
    private String branchCode;
    private String number;
    private String checkDigit;
    private String accountId;


    public String getBrandName()
    {
        return brandName;
    }


    public AccountData setBrandName(String brandName)
    {
        this.brandName = brandName;

        return this;
    }


    public String getCompanyCnpj()
    {
        return companyCnpj;
    }


    public AccountData setCompanyCnpj(String companyCnpj)
    {
        this.companyCnpj = companyCnpj;

        return this;
    }


    public String getType()
    {
        return type;
    }


    public AccountData setType(String type)
    {
        this.type = type;

        return this;
    }


    public String getCompeCode()
    {
        return compeCode;
    }


    public AccountData setCompeCode(String compeCode)
    {
        this.compeCode = compeCode;

        return this;
    }


    public String getBranchCode()
    {
        return branchCode;
    }


    public AccountData setBranchCode(String branchCode)
    {
        this.branchCode = branchCode;

        return this;
    }


    public String getNumber()
    {
        return number;
    }


    public AccountData setNumber(String number)
    {
        this.number = number;

        return this;
    }


    public String getCheckDigit()
    {
        return checkDigit;
    }


    public AccountData setCheckDigit(String checkDigit)
    {
        this.checkDigit = checkDigit;

        return this;
    }


    public String getAccountId()
    {
        return accountId;
    }


    public AccountData setAccountId(String accountId)
    {
        this.accountId = accountId;

        return this;
    }
}
