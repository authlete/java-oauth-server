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


import com.authlete.common.types.User;


/**
 * Operations to access the user database.
 *
 * @author Takahiko Kawasaki
 */
public class UserDao
{
    /**
     * Dummy user database.
     */
    private static final UserEntity[] sUserDB = {
            new UserEntity("1001", "john", "john", "John Smith"),
            new UserEntity("1002", "jane", "jane", "Jane Smith")
    };


    /**
     * Get a user entity by a pair of login ID and password.
     *
     * @param loginId
     *         Login ID.
     *
     * @param password
     *         Login password.
     *
     * @return
     *         A user entity that has the login ID and the password.
     *         {@code null} is returned if there is no user who has
     *         the login credentials.
     */
    public static User getByCredentials(String loginId, String password)
    {
        // For each user.
        for (UserEntity ue : sUserDB)
        {
            // If the login credentials are valid.
            if (ue.getLoginId().equals(loginId) && ue.getPassword().equals(password))
            {
                // Found the user who has the login credentials.
                return ue;
            }
        }

        // Not found any user who has the login credentials.
        return null;
    }
}
