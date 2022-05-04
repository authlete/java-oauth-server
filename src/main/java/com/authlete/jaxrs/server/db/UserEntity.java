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


import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.authlete.common.dto.Address;
import com.authlete.common.types.StandardClaims;
import com.authlete.common.types.User;
import com.google.gson.Gson;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;


/**
 * Dummy user entity that represents a user record.
 */
public class UserEntity implements User, Serializable
{
    private static final long serialVersionUID = 1L;


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
     * The email address of the user.
     */
    private String email;


    /**
     * The postal address of the user.
     */
    private Address address;


    /**
     * The phone number of the user.
     */
    private String phoneNumber;


    /**
     * The code of the user.
     */
    private String code;

    // Below are standard claims as defined in https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
    private boolean phoneNumberVerified;
    private boolean emailVerified;
    private String givenName;
    private String familyName;
    private String middleName;
    private String nickName;
    private String profile;
    private String picture;
    private String website;
    private String gender;
    private String zoneinfo;
    private String locale;
    private String preferredUsername;
    private String birthdate;
    private Date updatedAt;


    // Custom claims
    private List<String> nationalities;


    /**
     * Constructor with initial values.
     */
    public UserEntity(
            String subject, String loginId, String password, String name,
            String email, Address address, String phoneNumber, String code)
    {
        this.subject     = subject;
        this.loginId     = loginId;
        this.password    = password;
        this.name        = name;
        this.email       = email;
        this.address     = address;
        this.phoneNumber = phoneNumber;
        this.code        = code;
    }


    public UserEntity(
            String subject, String loginId, String password, String name,
            String email, Address address, String phoneNumber, String code,
            String givenName, String familyName, String middleName,
            String nickName, String profile, String picture, String website,
            String gender, String zoneinfo, String locale,
            String preferredUsername, String birthdate, Date updatedAt)
    {
        this.subject     = subject;
        this.loginId     = loginId;
        this.password    = password;
        this.name        = name;
        this.email       = email;
        this.address     = address;
        this.phoneNumber = phoneNumber;
        this.code        = code;
        this.givenName   = givenName;
        this.familyName  = familyName;
        this.middleName  = middleName;
        this.nickName    = nickName;
        this.profile     = profile;
        this.picture     = picture;
        this.website     = website;
        this.gender      = gender;
        this.zoneinfo    = zoneinfo;
        this.locale      = locale;
        this.preferredUsername = preferredUsername;
        this.birthdate   = birthdate;
        this.updatedAt   = updatedAt;
    }


    public UserEntity(UserInfo userInfo)
    {
        if (userInfo == null)
        {
            return;
        }

        this.subject           = userInfo.getSubject().getValue();
        this.loginId           = null;
        this.password          = null;
        this.name              = userInfo.getName();
        this.email             = userInfo.getEmailAddress();
        this.address           = extractAddress(userInfo);
        this.phoneNumber       = userInfo.getPhoneNumber();
        this.code              = null;
        this.givenName         = userInfo.getGivenName();
        this.familyName        = userInfo.getFamilyName();
        this.middleName        = userInfo.getMiddleName();
        this.nickName          = userInfo.getNickname();
        this.profile           = toString(userInfo.getProfile());
        this.picture           = toString(userInfo.getPicture());
        this.website           = toString(userInfo.getWebsite());
        this.gender            = extractGender(userInfo);
        this.zoneinfo          = userInfo.getZoneinfo();
        this.locale            = userInfo.getLocale();
        this.preferredUsername = userInfo.getPreferredUsername();
        this.birthdate         = userInfo.getBirthdate();
        this.updatedAt         = userInfo.getUpdatedTime();
    }


    private static Address extractAddress(UserInfo userInfo)
    {
        com.nimbusds.openid.connect.sdk.claims.Address addr = userInfo.getAddress();

        if (addr == null)
        {
            return null;
        }

        return new Address()
                .setCountry(addr.getCountry())
                .setFormatted(addr.getFormatted())
                .setLocality(addr.getLocality())
                .setPostaCode(addr.getPostalCode())
                .setRegion(addr.getRegion())
                .setStreetAddress(addr.getStreetAddress())
                ;
    }


    private static String extractGender(UserInfo userInfo)
    {
        Gender gender = userInfo.getGender();

        if (gender == null)
        {
            return null;
        }

        return gender.getValue();
    }


    private static String toString(URI uri)
    {
        if (uri == null)
        {
            return null;
        }

        return uri.toString();
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


    public UserEntity setSubject(String subject)
    {
        this.subject = subject;

        return this;
    }


    @Override
    public Object getClaim(String claimName, String languageTag)
    {
        if (claimName == null)
        {
            return null;
        }

        // See "OpenID Connect Core 1.0, 5. Claims".
        switch (claimName)
        {
            case StandardClaims.NAME:
                // "name" claim. This claim can be requested by including "profile"
                // in "scope" parameter of an authorization request.
                return name;

            case StandardClaims.EMAIL:
                // "email" claim. This claim can be requested by including "email"
                // in "scope" parameter of an authorization request.
                return email;

            case StandardClaims.ADDRESS:
                // "address" claim. This claim can be requested by including "address"
                // in "scope" parameter of an authorization request.
                return toMap(address);

            case StandardClaims.PHONE_NUMBER:
                // "phone_number" claim. This claim can be requested by including "phone"
                // in "scope" parameter of an authorization request.
                return phoneNumber;

            case StandardClaims.PHONE_NUMBER_VERIFIED:
                return phoneNumberVerified;

            case StandardClaims.EMAIL_VERIFIED:
                return emailVerified;

            case StandardClaims.BIRTHDATE:
                return birthdate;

            case StandardClaims.GIVEN_NAME:
                return givenName;

            case StandardClaims.FAMILY_NAME:
                return familyName;

            case StandardClaims.MIDDLE_NAME:
                return middleName;

            case StandardClaims.NICKNAME:
                return nickName;

            case StandardClaims.PROFILE:
                return profile;

            case StandardClaims.PICTURE:
                return picture;

            case StandardClaims.WEBSITE:
                return website;

            case StandardClaims.GENDER:
                return gender;

            case StandardClaims.ZONEINFO:
                return zoneinfo;

            case StandardClaims.LOCALE:
                return locale;

            case StandardClaims.UPDATED_AT:
                return updatedAt.getTime() / 1000l;

            case StandardClaims.PREFERRED_USERNAME:
                return preferredUsername;

            case "nationalities":
                return nationalities;

            default:
                // Unsupported claim.
                return null;
        }
    }


    @Override
    public Object getAttribute(String attributeName)
    {
        if (attributeName == null)
        {
            return null;
        }

        switch (attributeName)
        {
            case "code":
                // The code of the user.
                return code;

            default:
                // Unsupported attribute.
                return null;
        }
    }


    public List<String> getNationalities()
    {
        return nationalities;
    }


    public UserEntity setNationalities(List<String> nationalities)
    {
        this.nationalities = nationalities;

        return this;
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Address address)
    {
        if (address == null)
        {
            return null;
        }

        // This Gson instance does not serialize properties with null values.
        Gson gson = new Gson();

        return (Map<String, Object>)gson.fromJson(gson.toJson(address), Map.class);
    }
}
