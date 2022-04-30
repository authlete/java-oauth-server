/*
 * Copyright (C) 2016-2022 Authlete, Inc.
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


import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.authlete.common.dto.Address;
import com.authlete.common.types.User;


/**
 * Operations to access the user database.
 */
public class UserDao
{
    /**
     * Dummy user database.
     */
    private static final Map<String, UserEntity> sUserDB = new HashMap<>();

    static
    {
        addAll(
            new UserEntity("1001", "john", "john", "John Flibble Smith", "john@example.com",
                    new Address().setCountry("USA Flibble"), "+1 (425) 555-1212", "675325",
                    "John", "Smith", "Doe", "Johnny",
                    "https://example.com/john/profile", "https://example.com/john/me.jpg",
                    "https://example.com/john/", "male", "Europe/London",
                    "en-US", "john", "0000-03-22", toDate("2020-01-01")),

            new UserEntity("1002", "jane", "jane", "Jane Smith", "jane@example.com",
                    new Address().setCountry("Chile"), "+56 (2) 687 2400", "264209"),

            new UserEntity("1003", "max", "max", "Max Meier", "max@example.com",
                    new Address().setCountry("Germany").setRegion("Bavaria").setLocality("Augsburg"),
                    "+49 (30) 210 94-0", "12344",
                    "Max", "Meier", null, null,
                    "https://example.com/max/profile", "https://example.com/max/me.jpg",
                    "https://example.com/max/", "male", "Europe/Berlin", "de",
                    "max", "1956-01-28", toDate("2021-11-28"))
                    .setNationalities(Arrays.asList("USA", "DEU")),

            new UserEntity("1004", "inga", "inga", "Inga Silverstone", "inga@example.com",
                    new Address()
                        .setCountry("USA")
                        .setLocality("Shoshone")
                        .setStreetAddress("114 0ld State Hwy 127")
                        .setPostaCode("CA 92384"),
                    null, null, "Inga", "Silverstone", null, null,
                    "https://example.com/inga/profile", "https://example.com/inga/me.jpg",
                    "https://example.com/inga/", "female", "America/Toronto", "en-US",
                    "inga", "1991-11-06", toDate("2022-04-30")
            )
        );
    };


    private static Date toDate(String input)
    {
        return Date.from(LocalDate.parse(input).atStartOfDay().toInstant(ZoneOffset.UTC));
    }


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
        for (UserEntity ue : sUserDB.values())
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
                String registeredLoginId  = ue.getLoginId();
                String registeredPassword = ue.getPassword();

                // Check if the user's credentials are the target ones.
                return ((registeredLoginId  != null) && registeredLoginId .equals(loginId )) &&
                       ((registeredPassword != null) && registeredPassword.equals(password));
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


    /**
     * Add a user.
     */
    public static void add(UserEntity entity)
    {
        sUserDB.put(entity.getSubject(), entity);
    }


    private static void addAll(UserEntity... entities)
    {
        for (UserEntity entity : entities)
        {
            add(entity);
        }
    }
}
