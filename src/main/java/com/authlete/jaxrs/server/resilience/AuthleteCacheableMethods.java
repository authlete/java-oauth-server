/*
 * Copyright (C) 2026 Authlete, Inc.
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
package com.authlete.jaxrs.server.resilience;


import java.lang.reflect.Method;
import com.authlete.common.dto.CredentialIssuerJwksRequest;
import com.authlete.common.dto.CredentialIssuerMetadataRequest;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.ServiceConfigurationRequest;
import com.authlete.common.dto.StandardIntrospectionRequest;


/**
 * Decides which {@link com.authlete.common.api.AuthleteApi AuthleteApi} methods
 * are cacheable, and builds a stable cache key and TTL for each call.
 *
 * <p>
 * Only the idempotent read endpoints named in Authlete's "Rate Limit Best
 * Practices" guide are cached:
 * </p>
 * <ul>
 *   <li>{@code getServiceConfiguration} &mdash; service discovery document</li>
 *   <li>{@code getServiceJwks} &mdash; service JWK Set</li>
 *   <li>{@code getClient} &mdash; client metadata</li>
 *   <li>{@code credentialIssuerMetadata} &mdash; OID4VCI issuer metadata</li>
 *   <li>{@code credentialIssuerJwks} &mdash; OID4VCI issuer JWK Set</li>
 *   <li>{@code introspection} / {@code standardIntrospection} &mdash; token
 *       introspection (short TTL; see {@link CachePolicy#capByTokenExpiry})</li>
 * </ul>
 *
 * <p>
 * Any other method returns {@code null} from {@link #policyFor(Method, Object[])}
 * and is therefore never cached.
 * </p>
 */
class AuthleteCacheableMethods
{
    /**
     * The decision for a single cacheable call: the namespaced cache key and the
     * TTL to apply.
     */
    static final class CachePolicy
    {
        final String  key;
        final long    ttlMillis;

        /**
         * When true, the effective TTL must additionally be capped so the entry
         * never outlives the token's own expiry (introspection only).
         */
        final boolean capByTokenExpiry;

        CachePolicy(String key, long ttlMillis, boolean capByTokenExpiry)
        {
            this.key              = key;
            this.ttlMillis        = ttlMillis;
            this.capByTokenExpiry = capByTokenExpiry;
        }
    }


    private final ResilienceConfig config;


    AuthleteCacheableMethods(ResilienceConfig config)
    {
        this.config = config;
    }


    /**
     * Return the caching policy for the given method invocation, or {@code null}
     * if the method must not be cached.
     */
    CachePolicy policyFor(Method method, Object[] args)
    {
        String name = method.getName();
        int    argc = (args == null) ? 0 : args.length;

        switch (name)
        {
            case "getServiceConfiguration":
                return serviceConfiguration(args, argc);

            case "getServiceJwks":
                return key("getServiceJwks", joinArgs(args),
                        config.getCacheTtlServiceJwks(), false);

            case "getClient":
                // getClient(long) and getClient(String); both identify one client.
                return key("getClient", String.valueOf(args[0]),
                        config.getCacheTtlClient(), false);

            case "credentialIssuerMetadata":
                if (args[0] instanceof CredentialIssuerMetadataRequest)
                {
                    CredentialIssuerMetadataRequest req = (CredentialIssuerMetadataRequest) args[0];
                    return key("credentialIssuerMetadata", String.valueOf(req.isPretty()),
                            config.getCacheTtlCredentialIssuerMetadata(), false);
                }
                return null;

            case "credentialIssuerJwks":
                if (args[0] instanceof CredentialIssuerJwksRequest)
                {
                    CredentialIssuerJwksRequest req = (CredentialIssuerJwksRequest) args[0];
                    return key("credentialIssuerJwks", String.valueOf(req.isPretty()),
                            config.getCacheTtlCredentialIssuerJwks(), false);
                }
                return null;

            case "introspection":
                if (args[0] instanceof IntrospectionRequest)
                {
                    return introspection((IntrospectionRequest) args[0]);
                }
                return null;

            case "standardIntrospection":
                if (args[0] instanceof StandardIntrospectionRequest)
                {
                    return standardIntrospection((StandardIntrospectionRequest) args[0]);
                }
                return null;

            default:
                return null;
        }
    }


    private CachePolicy serviceConfiguration(Object[] args, int argc)
    {
        long ttl = config.getCacheTtlServiceConfiguration();

        if (argc == 1 && args[0] instanceof ServiceConfigurationRequest)
        {
            ServiceConfigurationRequest req = (ServiceConfigurationRequest) args[0];
            String detail = req.isPretty() + "|" + req.getPatch();
            return key("getServiceConfiguration", detail, ttl, false);
        }

        // getServiceConfiguration() or getServiceConfiguration(boolean).
        return key("getServiceConfiguration", joinArgs(args), ttl, false);
    }


    private CachePolicy introspection(IntrospectionRequest req)
    {
        // A DPoP proof is unique per request (jti/iat), and HTTP message
        // signature inputs vary per request too. A cached result could never
        // be legitimately reused for them, and reusing one would skip the
        // per-request proof validation, so such calls are never cached.
        if (req.getDpop() != null
                || req.getMessage() != null
                || req.getHeaders() != null
                || req.getRequiredComponents() != null)
        {
            return null;
        }

        // The key must capture every remaining input that influences the
        // introspection result, so that requests differing in any binding
        // (e.g. mTLS client certificate or target URI) never share an entry.
        StringBuilder detail = new StringBuilder();
        detail.append(req.getToken());
        detail.append('|').append(join(req.getScopes()));
        detail.append('|').append(req.getSubject());
        detail.append('|').append(req.getClientCertificate());
        detail.append('|').append(req.getHtm());
        detail.append('|').append(req.getHtu());
        detail.append('|').append(join(req.getResources()));
        detail.append('|').append(req.getUri());
        detail.append('|').append(req.getTargetUri());
        detail.append('|').append(join(req.getAcrValues()));
        detail.append('|').append(req.getMaxAge());
        detail.append('|').append(req.isRequestBodyContained());
        detail.append('|').append(req.isDpopNonceRequired());

        return key("introspection", detail.toString(),
                config.getCacheTtlIntrospection(), true);
    }


    private CachePolicy standardIntrospection(StandardIntrospectionRequest req)
    {
        // Besides the token parameters, the response depends on the resource
        // server's identity and the requested response format/protection, so
        // all of them participate in the key. Otherwise one resource server
        // could receive a response cached for another.
        StringBuilder detail = new StringBuilder();
        detail.append(req.getParameters());
        detail.append('|').append(req.isWithHiddenProperties());
        detail.append('|').append(req.getRsUri());
        detail.append('|').append(req.getHttpAcceptHeader());
        detail.append('|').append(req.getIntrospectionSignAlg());
        detail.append('|').append(req.getIntrospectionEncryptionAlg());
        detail.append('|').append(req.getIntrospectionEncryptionEnc());
        detail.append('|').append(req.getSharedKeyForSign());
        detail.append('|').append(req.getSharedKeyForEncryption());
        detail.append('|').append(req.getPublicKeyForEncryption());

        return key("standardIntrospection", detail.toString(),
                config.getCacheTtlStandardIntrospection(), true);
    }


    private static CachePolicy key(String namespace, String detail, long ttlMillis, boolean capByTokenExpiry)
    {
        // Token (and similar) values can be long; namespacing keeps lookups O(1)
        // in the shared map without risk of cross-method collisions.
        return new CachePolicy(namespace + "::" + detail, ttlMillis, capByTokenExpiry);
    }


    private static String joinArgs(Object[] args)
    {
        if (args == null || args.length == 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++)
        {
            if (i > 0)
            {
                sb.append('|');
            }
            sb.append(String.valueOf(args[i]));
        }

        return sb.toString();
    }


    private static String join(Object[] values)
    {
        if (values == null || values.length == 0)
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            sb.append(values[i]);
        }

        return sb.toString();
    }
}