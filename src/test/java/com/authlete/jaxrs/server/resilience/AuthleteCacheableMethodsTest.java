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


import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.lang.reflect.Method;
import java.net.URI;
import org.junit.Test;
import com.authlete.common.api.AuthleteApi;
import com.authlete.common.dto.IntrospectionRequest;
import com.authlete.common.dto.StandardIntrospectionRequest;
import com.authlete.jaxrs.server.resilience.AuthleteCacheableMethods.CachePolicy;


public class AuthleteCacheableMethodsTest
{
    private final AuthleteCacheableMethods cacheable =
            new AuthleteCacheableMethods(new ResilienceConfig());


    private CachePolicy introspectionPolicy(IntrospectionRequest req) throws Exception
    {
        Method method = AuthleteApi.class.getMethod(
                "introspection", IntrospectionRequest.class);

        return cacheable.policyFor(method, new Object[] { req });
    }


    private CachePolicy standardIntrospectionPolicy(StandardIntrospectionRequest req) throws Exception
    {
        Method method = AuthleteApi.class.getMethod(
                "standardIntrospection", StandardIntrospectionRequest.class);

        return cacheable.policyFor(method, new Object[] { req });
    }


    @Test
    public void dpopIntrospectionIsNeverCached() throws Exception
    {
        IntrospectionRequest req = new IntrospectionRequest()
                .setToken("token")
                .setDpop("dpop-proof")
                .setHtm("GET")
                .setHtu("https://rs.example.com/resource");

        assertNull("DPoP-bound requests must not be cached",
                introspectionPolicy(req));
    }


    @Test
    public void messageSignatureIntrospectionIsNeverCached() throws Exception
    {
        IntrospectionRequest req = new IntrospectionRequest()
                .setToken("token")
                .setMessage("signed-message");

        assertNull("message-signature requests must not be cached",
                introspectionPolicy(req));
    }


    @Test
    public void clientCertificateParticipatesInIntrospectionKey() throws Exception
    {
        IntrospectionRequest base = new IntrospectionRequest().setToken("token");

        CachePolicy noCert    = introspectionPolicy(base);
        CachePolicy withCert  = introspectionPolicy(
                new IntrospectionRequest().setToken("token").setClientCertificate("CERT-A"));
        CachePolicy otherCert = introspectionPolicy(
                new IntrospectionRequest().setToken("token").setClientCertificate("CERT-B"));

        assertNotNull(noCert);
        assertNotNull(withCert);
        assertNotNull(otherCert);
        assertNotEquals("same token, different certs must not share an entry",
                withCert.key, otherCert.key);
        assertNotEquals(noCert.key, withCert.key);
    }


    @Test
    public void resourcesParticipateInIntrospectionKey() throws Exception
    {
        CachePolicy a = introspectionPolicy(new IntrospectionRequest()
                .setToken("token")
                .setResources(new URI[] { URI.create("https://rs-a.example.com") }));
        CachePolicy b = introspectionPolicy(new IntrospectionRequest()
                .setToken("token")
                .setResources(new URI[] { URI.create("https://rs-b.example.com") }));

        assertNotEquals(a.key, b.key);
    }


    @Test
    public void rsUriParticipatesInStandardIntrospectionKey() throws Exception
    {
        CachePolicy a = standardIntrospectionPolicy(new StandardIntrospectionRequest()
                .setParameters("token=abc")
                .setRsUri(URI.create("https://rs-a.example.com")));
        CachePolicy b = standardIntrospectionPolicy(new StandardIntrospectionRequest()
                .setParameters("token=abc")
                .setRsUri(URI.create("https://rs-b.example.com")));

        assertNotEquals("different resource servers must not share an entry",
                a.key, b.key);
    }


    @Test
    public void acceptHeaderParticipatesInStandardIntrospectionKey() throws Exception
    {
        CachePolicy json = standardIntrospectionPolicy(new StandardIntrospectionRequest()
                .setParameters("token=abc")
                .setHttpAcceptHeader("application/json"));
        CachePolicy jwt  = standardIntrospectionPolicy(new StandardIntrospectionRequest()
                .setParameters("token=abc")
                .setHttpAcceptHeader("application/token-introspection+jwt"));

        assertNotEquals(json.key, jwt.key);
    }
}
