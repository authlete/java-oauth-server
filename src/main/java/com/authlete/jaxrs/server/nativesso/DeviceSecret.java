/*
 * Copyright (C) 2025 Authlete, Inc.
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
package com.authlete.jaxrs.server.nativesso;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


/**
 * Device Secret.
 *
 * <p>
 * This class represents the concept of a "<a href=
 * "https://openid.net/specs/openid-connect-native-sso-1_0.html#name-device-secret"
 * >Device Secret</a>" introduced by the "<a href=
 * "https://openid.net/specs/openid-connect-native-sso-1_0.html">OpenID Connect
 * Native SSO for Mobile Apps 1.0</a>" specification ("Native SSO").
 * </p>
 *
 * <p>
 * The following is an excerpt from the specification describing the concept:
 * </p>
 *
 * <blockquote>
 * <p><i>
 * The device secret contains relevant data to the device and the current users
 * authenticated with the device. The device secret is completely opaque to the
 * client and as such the AS MUST adequately protect the value such as using a
 * JWE if the AS is not maintaining state on the backend.
 * </i></p>
 * </blockquote>
 *
 * @see <a href="https://openid.net/specs/openid-connect-native-sso-1_0.html#name-device-secret"
 *      >OpenID Connect Native SSO for Mobile Apps 1.0, Section 3.2. Device Secret</a>
 */
public class DeviceSecret
{
    /**
     * The value of this device secret.
     */
    private String value;


    /**
     * The value of the hash of this device secret.
     */
    private String hash;


    /**
     * The identifier of the user's authentication session associated with
     * this device secret.
     */
    private String sessionId;


    /**
     * The identifier of the device associated with this device secret.
     */
    private String deviceId;


    /**
     * Get the value of this device secret. This corresponds to the value
     * of the {@code device_secret} parameter in token responses.
     *
     * @return
     *         The value of this device secret.
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Set the value of this device secret. This corresponds to the value
     * of the {@code device_secret} parameter in token responses.
     *
     * @param value
     *         The value of this device secret.
     *
     * @return
     *         {@code this} object.
     */
    public DeviceSecret setValue(String value)
    {
        this.value = value;

        return this;
    }


    /**
     * Get the value of the hash of this device secret. This corresponds to
     * the value of the {@code ds_hash} claim in the Native SSO-compliant
     * ID token.
     *
     * @return
     *         The value of the hash of this device secret.
     */
    public String getHash()
    {
        return hash;
    }


    /**
     * Set the value of the hash of this device secret. This corresponds to
     * the value of the {@code ds_hash} claim in the Native SSO-compliant
     * ID token.
     *
     * @param hash
     *         The value of the hash of this device secret.
     *
     * @return
     *         {@code this} object.
     */
    public DeviceSecret setHash(String hash)
    {
        this.hash = hash;

        return this;
    }


    /**
     * Get the identifier of the user's authentication session associated with
     * this device secret. This corresponds to the {@code sid} claim in the
     * Native SSO-compliant ID token.
     *
     * @return
     *         The identifier of the user's authentication session.
     */
    public String getSessionId()
    {
        return sessionId;
    }


    /**
     * Set the identifier of the user's authentication session associated with
     * this device secret. This corresponds to the {@code sid} claim in the
     * Native SSO-compliant ID token.
     *
     * @param sessionId
     *         The identifier of the user's authentication session.
     *
     * @return
     *         {@code this} object.
     */
    public DeviceSecret setSessionId(String sessionId)
    {
        this.sessionId = sessionId;

        return this;
    }


    /**
     * Get the identifier of the device associated with this device secret.
     *
     * @return
     *         The identifier of the device.
     */
    public String getDeviceId()
    {
        return deviceId;
    }


    /**
     * Set the identifier of the device associated with this device secret.
     *
     * @param deviceId
     *         The identifier of the device.
     *
     * @return
     *         {@code this} object.
     */
    public DeviceSecret setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;

        return this;
    }


    /**
     * Compute the hash of the specified device secret value.
     *
     * @param deviceSecretValue
     *         A device secret value.
     *
     * @return
     *         The hash of the specified device secret value.
     */
    public static String computeHash(String deviceSecretValue)
    {
        if (deviceSecretValue == null)
        {
            return null;
        }

        // The Native SSO specification does not define any logic for computing
        // the hash from a device secret. It explicitly states as follows:
        //
        //   The exact binding between the ds_hash and device_secret is not
        //   specified by this profile. As this binding is managed solely by
        //   the Authorization Server, the AS can choose how to protect the
        //   relationship between the id_token and device_secret.
        //

        // The following logic is specific to this implementation.

        // BASE64URL( SHA-256( deviceSecretValue ) )
        return toBase64Url(sha256(deviceSecretValue));
    }


    /**
     * Convert the input data into a base64url string without padding.
     */
    private static String toBase64Url(byte[] input)
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }


    /**
     * Compute the digest of the input data with the specified hash algorithm.
     */
    private static byte[] digest(String algorithm, byte[] input) throws NoSuchAlgorithmException
    {
        return MessageDigest.getInstance(algorithm).digest(input);
    }


    /**
     * Compute the digest of the input data with the SHA-256 algorithm.
     */
    private static byte[] sha256(byte[] input)
    {
        try
        {
            return digest("SHA-256", input);
        }
        catch (NoSuchAlgorithmException cause)
        {
            // This error will never happen because every Java platform
            // must support "SHA-256".
            throw new UnsupportedOperationException(
                    "This Java platform does not support 'SHA-256' for message digest: "
                    + cause.getMessage(), cause);
        }
    }


    /**
     * Compute the digest of the input data with the SHA-256 algorithm.
     */
    private static byte[] sha256(String input)
    {
        return sha256(input.getBytes(StandardCharsets.UTF_8));
    }
}
