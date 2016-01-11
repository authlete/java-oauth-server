/*
 * Copyright (C) 2016 Authlete, Inc.
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
package com.authlete.jaxrs.server.db;


import com.authlete.common.types.StandardClaims;
import com.authlete.common.types.User;


/**
 * Dummy user entity that represents a user record.
 *
 * @author Takahiko Kawasaki
 */
public class UserEntity implements User
{
    /**
     * The subject (unique identifier) of the user.
     */
    private String subject;


    /**
     * The login ID.
     */
    private String loginId;


    /**
     * The login password.
     */
    private String password;


    /**
     * The name of the user.
     */
    private String name;


    /**
     * Constructor with initial values.
     */
    public UserEntity(String subject, String loginId, String password, String name)
    {
        this.subject  = subject;
        this.loginId  = loginId;
        this.password = password;
        this.name     = name;
    }


    /**
     * Get the login ID.
     *
     * @return
     *         The login ID.
     */
    public String getLoginId()
    {
        return loginId;
    }


    /**
     * Get the login password.
     *
     * @return
     *         The login password.
     */
    public String getPassword()
    {
        return password;
    }


    @Override
    public String getSubject()
    {
        return subject;
    }


    @Override
    public Object getClaim(String claimName, String languageTag)
    {
        if (claimName == null)
        {
            return null;
        }

        switch (claimName)
        {
            case StandardClaims.NAME:
                // "name" claim.
                return name;

            default:
                // Unsupported claim.
                return null;
        }
    }
}
