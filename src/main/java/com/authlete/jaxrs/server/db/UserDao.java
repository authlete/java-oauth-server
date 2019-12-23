/*
 * Copyright (C) 2016-2019 Authlete, Inc.
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


import com.authlete.common.dto.Address;
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
            new UserEntity("1001", "john", "john", "John Smith", "john@example.com",
                    new Address().setCountry("USA"), "+1 (425) 555-1212", "675325"),
            new UserEntity("1002", "jane", "jane", "Jane Smith", "jane@example.com",
                    new Address().setCountry("Chile"), "+56 (2) 687 2400", "264209"),
            new UserEntity("1003", "max", "max", "Max Meier", "max@example.com",
                    new Address().setCountry("Germany"), "+49 (30) 210 94-0", "12344"),
    };


    /**
     * Condition for user search.
     */
    private static interface SearchCondition
    {
        boolean check(UserEntity ue);
    }


    /**
     * Get a user who meets the condition.
     *
     * @param condition
     *         The condition for searching a user.
     *
     * @return
     *         A user who meets the condition.
     */
    private static User get(SearchCondition condition)
    {
        // For each user.
        for (UserEntity ue : sUserDB)
        {
            // If the condition is satisfied.
            if (condition.check(ue))
            {
                // Found the user who meets the condition.
                return ue;
            }
        }

        // Not found any user who meets the condition.
        return null;
    }


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
    public static User getByCredentials(final String loginId, final String password)
    {
        return get(new SearchCondition() {
            @Override
            public boolean check(UserEntity ue)
            {
                // Check if the user's credentials are the target ones.
                return ue.getLoginId().equals(loginId) && ue.getPassword().equals(password);
            }
        });
    }


    /**
     * Get a user by a subject.
     *
     * @param subject
     *         The subject of a user.
     *
     * @return
     *         A user entity that has the subject.
     *         {@code null} is returned if there is no user who has
     *         the subject.
     */
    public static User getBySubject(final String subject)
    {
        return get(new SearchCondition() {
            @Override
            public boolean check(UserEntity ue)
            {
                // Check if the user's subject is the target one.
                return ue.getSubject().equals(subject);
            }
        });
    }


    /**
     * Get a user by an email address.
     *
     * @param email
     *         An email address.
     *
     * @return
     *         A user entity that has the email address.
     *         {@code null} is returned if there is no user who has
     *         the email address.
     */
    public static User getByEmail(final String email)
    {
        return get(new SearchCondition() {
            @Override
            public boolean check(UserEntity ue)
            {
                // Get the user's "email" claim.
                String e = (String)ue.getClaim("email", null);

                // Check if the user's email is the target one.
                return e != null && e.equals(email);
            }
        });
    }


    /**
     * Get a user by a phone number.
     *
     * @param phoneNumber
     *         A phone number.
     *
     * @return
     *         A user entity that has the phone number.
     *         {@code null} is returned if there is no user who has
     *         the phone number.
     */
    public static User getByPhoneNumber(final String phoneNumber)
    {
        return get(new SearchCondition() {
            @Override
            public boolean check(UserEntity ue)
            {
                // Get the user's "phone_number" claim.
                String ph = (String)ue.getClaim("phone_number", null);

                // Check if the user's phone number is the target one.
                return ph != null && ph.equals(phoneNumber);
            }
        });
    }
}
