/*
 * Copyright (C) 2023 Authlete, Inc.
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
import com.authlete.common.types.JWEAlg;
import com.authlete.common.types.JWEEnc;
import com.authlete.common.types.JWSAlg;


/**
 * Dummy resource server entity that represents a resource server record.
 */
public class ResourceServerEntity implements Serializable
{
    private static final long serialVersionUID = 1L;


    /**
     * The ID of the resource server.
     */
    private String id;


    /**
     * The secret of the resource server.
     */
    private String secret;


    /**
     * The URI of the resource server.
     */
    private URI uri;


    /**
     * The JWS alg algorithm for signing introspection responses.
     */
    private JWSAlg introspectionSignAlg;


    /**
     * The JWE alg algorithm for encrypting introspection responses.
     */
    private JWEAlg introspectionEncryptionAlg;


    /**
     * The JWE enc algorithm for encrypting introspection responses.
     */
    private JWEEnc introspectionEncryptionEnc;


    /**
     * The shared key for signing introspection responses.
     */
    private String sharedKeyForIntrospectionResponseSign;


    /**
     * The shared key for encrypting introspection responses.
     */
    private String sharedKeyForIntrospectionResponseEncryption;


    /**
     * The public key for signing introspection responses.
     */
    private String publicKeyForIntrospectionResponseEncryption;


    /**
     * Constructor with initial values.
     */
    public ResourceServerEntity(
            String id,
            String secret,
            URI uri,
            JWSAlg introspectionSignAlg,
            JWEAlg introspectionEncryptionAlg,
            JWEEnc introspectionEncryptionEnc,
            String sharedKeyForIntrospectionResponseSign,
            String sharedKeyForIntrospectionResponseEncryption,
            String publicKeyForIntrospectionResponseEncryption)
    {
        this.id                                          = id;
        this.secret                                      = secret;
        this.uri                                         = uri;
        this.introspectionSignAlg                        = introspectionSignAlg;
        this.introspectionEncryptionAlg                  = introspectionEncryptionAlg;
        this.introspectionEncryptionEnc                  = introspectionEncryptionEnc;
        this.sharedKeyForIntrospectionResponseSign       = sharedKeyForIntrospectionResponseSign;
        this.sharedKeyForIntrospectionResponseEncryption = sharedKeyForIntrospectionResponseEncryption;
    }


    /**
     * Get the ID of the resource server.
     *
     * @return
     *         The ID of the resource server.
     */
    public String getId()
    {
        return id;
    }


    /**
     * Get the secret of the resource server.
     *
     * @return
     *         The secret of the resource server.
     */
    public String getSecret()
    {
        return secret;
    }


    /**
     * Get the URI of the resource server.
     *
     * @return
     *         The URI of the resource server.
     */
    public URI getUri()
    {
        return uri;
    }


    /**
     * Get the JWS <code>alg</code> algorithm for signing introspection
     * responses.
     *
     * @return
     *         The JWS <code>alg</code> algorithm for signing introspection
     *         responses.
     */
    public JWSAlg getIntrospectionSignAlg()
    {
        return introspectionSignAlg;
    }


    /**
     * Get the JWE <code>alg</code> algorithm for encrypting introspection
     * responses.
     *
     * @return
     *         The JWE <code>alg</code> algorithm for encrypting introspection
     *         responses.
     */
    public JWEAlg getIntrospectionEncryptionAlg()
    {
        return introspectionEncryptionAlg;
    }


    /**
     * Get the JWE <code>enc</code> algorithm for encrypting the introspection
     * response.
     *
     * @return
     *         The JWE <code>enc</code> algorithm for encrypting the
     *         introspection response.
     */
    public JWEEnc getIntrospectionEncryptionEnc()
    {
        return introspectionEncryptionEnc;
    }


    /**
     * Get the shared key for signing introspection responses.
     *
     * @return
     *         The shared key for signing introspection responses.
     */
    public String getSharedKeyForIntrospectionResponseSign()
    {
        return sharedKeyForIntrospectionResponseSign;
    }


    /**
     * Get the shared key for encrypting introspection responses.
     *
     * @return
     *         The shared key for encrypting introspection responses.
     */
    public String getSharedKeyForIntrospectionResponseEncryption()
    {
        return sharedKeyForIntrospectionResponseEncryption;
    }


    /**
     * Get the public key for signing introspection responses.
     *
     * @return
     *         The public key for signing introspection responses.
     */
    public String getPublicKeyForIntrospectionResponseEncryption()
    {
        return publicKeyForIntrospectionResponseEncryption;
    }
}
