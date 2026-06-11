/*
 * Copyright (C) 2016-2023 Authlete, Inc.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.authlete.common.dto.Address;
import com.authlete.common.types.User;
import com.authlete.mdoc.constants.MDLClaimNames;
import com.authlete.mdoc.constants.MDLConstants;


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
                        .setFormatted("114 Old State Hwy 127, Shoshone, CA 92384, USA")
                        .setCountry("USA")
                        .setLocality("Shoshone")
                        .setStreetAddress("114 Old State Hwy 127")
                        .setPostalCode("CA 92384"),
                    null, null, "Inga", "Silverstone", null, null,
                    "https://example.com/inga/profile", "https://example.com/inga/me.jpg",
                    "https://example.com/inga/", "female", "America/Toronto", "en-US",
                    "inga", "1991-11-06", toDate("2022-04-30"))
                    .setAttribute(MDLConstants.DOC_TYPE_MDL, createMDLData1004())

                    // POTENTIAL Interop Event Track 2
                    // https://gitlab.opencode.de/potential/interop-event
                    .addExtraClaim("age_equal_or_over", mapOf("18", Boolean.TRUE))
                    .addExtraClaim("place_of_birth", mapOf("locality", "Shoshone"))
                    .addExtraClaim("issuing_authority", "US")
                    .addExtraClaim("issuing_country", "US")
        );
    };


    /**
     * A substitute for {@code Map.of}, which is unavailable in Java 8.
     */
    private static Map<String, Object> mapOf(Object... keyValuePairs)
    {
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i < keyValuePairs.length; i += 2)
        {
            map.put((String)keyValuePairs[i], keyValuePairs[i+1]);
        }

        return map;
    }


    private static Map<String, Object> createMDLData1004()
    {
        // Some string claim values in the data below have the prefix "cbor:".
        // They are interpreted by Authlete server. See the JavaDoc of the
        // CredentialIssuanceOrder class in the authlete-java-common library
        // for details.
        //
        //   CredentialIssuerOrder JavaDoc
        //     https://authlete.github.io/authlete-java-common/com/authlete/common/dto/CredentialIssuanceOrder.html
        //

        // {
        //   "vehicle_category_code" : "A",
        //   "issue_date"            : "2023-01-01",
        //   "expiry_date"           : "2043-01-01"
        // }
        Map<String, Object> vehicleA = new LinkedHashMap<>();
        vehicleA.put("vehicle_category_code", "A");
        vehicleA.put("issue_date",            "cbor:1004(\"2023-01-01\")");
        vehicleA.put("expiry_date",           "cbor:1004(\"2043-01-01\")");

        // [
        //    vehicleA
        // ]
        List<Map<String, Object>> drivingPrivileges = new ArrayList<>();
        drivingPrivileges.add(vehicleA);

        // {
        //   "family_name"        : "Silverstone",
        //   "given_name"         : "Inga",
        //   "birth_date"         : "1991-11-06",
        //   "issuing_country"    : "US",
        //   "document_number"    : "12345678",
        //   "driving_privileges" : drivingPrivileges
        // }
        Map<String, Object> nameSpace = new LinkedHashMap<>();
        nameSpace.put(MDLClaimNames.FAMILY_NAME,        "Silverstone");
        nameSpace.put(MDLClaimNames.GIVEN_NAME,         "Inga");
        nameSpace.put(MDLClaimNames.BIRTH_DATE,         "cbor:1004(\"1991-11-06\")");
        nameSpace.put(MDLClaimNames.ISSUING_COUNTRY,    "US");
        nameSpace.put(MDLClaimNames.DOCUMENT_NUMBER,    "12345678");
        nameSpace.put(MDLClaimNames.DRIVING_PRIVILEGES, drivingPrivileges);

        // Additional mandatory mDL elements (ISO/IEC 18013-5).
        nameSpace.put(MDLClaimNames.ISSUING_AUTHORITY,      "US");
        nameSpace.put(MDLClaimNames.UN_DISTINGUISHING_SIGN, "USA");
        // "portrait": a small JPEG of the holder, as a CBOR byte string.
        nameSpace.put(MDLClaimNames.PORTRAIT,               "cbor:h'ffd8ffe000104a46494600010100000100010000ffdb0043000d090a0b0a080d0b0a0b0e0e0d0f13201513121213271c1e17202e2931302e292d2c333a4a3e333646372c2d405741464c4e525352323e5a615a50604a51524fffdb0043010e0e0e131113261515264f352d354f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4f4fffc00011080078006003012200021101031101ffc4001b00010003010101010000000000000000000004050607010302ffc4003f100001030204030406050b05000000000001000203041105122131064151132261a11432428191f0233571b2d1151624333643537382a2b1526292c1e1ffc4001801000301010000000000000000000000000003040201ffc4001f1100030002020301010000000000000000000102031131410412322122ffda000c03010002110311003f00dba22270b088880088880088880088880088880088880088b2bc598bc8c91d86d3bb28ca3b670dcdfd9fb2d6bf5bdbaac5d295b66a65d3d225625c534b4f9a3a26fa44a2e336cc075e7cf96da1eaa8e6e28c5657873258e116b65646083e3add532974986575600ea7a67b9a45c38f75a75b684e8a4acb4fb2b9c5289b171362d1c81ce9db201ecba36d8fc002ae30ee2d864b33108fb277f12304b79f2dc72ebee59da9c2311a56679a91e1b62496d9c001bded7b7bd415c9cb4bb078e5f4754639af635ec70735c2e0837042f562b85f17929aaa3a199d9a9e575997f61c76b7813cbc6fd6fb55645ab5b25b872f411116cc04444005cb6591f34cf964399ef71738dad72752ba7544cda7a69677825b130bc81bd80bae6b414fe955d053d9c448f0d765dc0bea7e1752f91d1460ecd070ee0714b08acae88bb31bc51bb4046f98f5bf43e775a9516b7b782901a1f458fb3dfb6b863580786dc957e0f8955e252676cf412c0c3693b212078d34d1df3ba9ca0ba5418fe0714f0c95747111520e67359fbceba75e7a6fe3752f19adaaa0689d92d1454e0004cf9cb8bae7401bbe9ff6beb84d4555553f6f3c949244f00c4ea7cdaef7bdfe77401cf974dc3e47cd87534b21ccf7c4c738dad72402560b1ea56d262f3c71b0b63243d808b0b117d3c2f71ee5b7c0e66cf82d23d8080220cd7ab7ba7cc27f8fcb119f844e444559304444015dc4333a0c0aadec0092cc9af4710d3e4563f86bebea6feafba56b38a3f67eabfa3ef8584a59bd1eae19f2e6ec9ed7e5bdaf637b28fc8fa2ac1f2740c5285b8961d351bde582403bc05ec4104798553c35c3d2e1334b5153331f2b9b91ad8ee5a1b706e6e37b8f9e57d148c9a264b19bb1ed0e69b6e0ecbf14f5505567ec5f98c672b8104169f10754adbd6877aedeca8c7f08763b4703e190453444e50e3dd20daf7d2fc87ced2f00c2ff002461c29dd2769239d9de46d98802c3c341f3a292e9a2a0a40eaa91ac6836bef724f25251b7ad07aadecc6f187d6b17f207de72bbe1099d2e0b91c05a295cc6db98d1dafbdc564b18aa656e293d4463b8e759be200b03efb5d6a782fea897f9e7eeb53307d8acdf26811115a4811110056f11c6f9701aa6c62e4343b7e40827c815cf1750ab87d2692683365ed6373335af6b8b5d72f52790bf5329c0ff001a355c1f533be39a94b73431f783f37aa4fb36e86c4fc7aab9aac329ea6613de48a71fbc89d95db5955f05d3fe87533e6f5e40cb5b6ca2f7feef25a0cc2e45f64972d24c72b4db48830e154f1d409e47cd512b6d95d33f365fb3e2a1715d4cf061a2389bdc99d91efcd6239dbdf63eebf55765c073553c594fda608e7e6b18646bed6dfd9b7f779214badb5d055a4d6cc32dc70746f8f06739e2c2495ce6ebb8b01fe415875d170087b0c129199b35e3cf7b5bd6ef5bcd3702fe85677fc960888ac250888800b138ee07583167be9609268ea1f99a5bad89dc1e9a9e7cb9eeb6c8b170ad699b8b72f688b85d1fa061b052e6cc58def1bdf526e6de17251febbbed51eaf1ec36941bd4095d6b86c5debebd76f355aee26a22e27b2a8d4ffa5bf8ace5c36e52946b16494dba65cafbd442da8a69607921b2b0b091bd88b2cf7e72d17f0aa3fe2dfc55852710e1b52434ca61713602519796f7dbcd730e1b9dfb23b9b24d6b4ccbd370ed73f1314b3c2e6c4d777e51ea96ff00b4db53d3cf9adeaf18e6bd81ec70735c2e0837042f56a31a8e0c5dbbe422226180888803e15b59050d33a7a876560d80ddc7a0f158ac571ca9c4af1feaa9cdbe881bdc8ea79ff8d97eb8871135f5e5ac7030424b63b5b5ea6fcee47c2caa5538f1a4b6c5556c222269908888027e178b54e1b27d13b3425d77c47677e07ff375b5c2f128312a6ed62eebc68f8c9d5a7f0f15ced4dc26bdf87573276eac3dd905af76df5b78a5de355fab93535a3a1a2f18e6bd81ec70735c2e0837042f54a342878c547a2e13532ddc08616b4b7704e80fc48445d9e51c7c1ced1115a24222200222200222200dd70c5476f82c6d25c5d138c64bbe22de16202b64451dfd31d3c1ffd9'");

        // {
        //   "org.iso.18013.5.1" : nameSpace
        // }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(MDLConstants.NAME_SPACE_MDL, nameSpace);

        return root;
    }


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
