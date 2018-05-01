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
package com.authlete.jaxrs.server.api;


import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseTokenEndpoint;


/**
 * An implementation of OAuth 2.0 token endpoint with OpenID Connect support.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.2"
 *      >RFC 6749, 3.2. Token Endpoint</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#HybridTokenEndpoint"
 *      >OpenID Connect Core 1.0, 3.3.3. Token Endpoint</a>
 *
 * @author Takahiko Kawasaki
 */
@Path("/api/token")
public class TokenEndpoint extends BaseTokenEndpoint
{
    
    // headers for certificate path with proxy-forwarded certificate information; the first entry is the client's certificate itself
    private String[] clientCertificatePathHeaders = {"X-Ssl-Cert", "X-Ssl-Cert-Chain-1", "X-Ssl-Cert-Chain-2", "X-Ssl-Cert-Chain-3", "X-Ssl-Cert-Chain-4"};
    
    /**
     * The token endpoint for {@code POST} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.2">RFC 6749,
     * 3.2. Token Endpoint</a> says:
     * </p>
     *
     * <blockquote>
     * <i>The client MUST use the HTTP "POST" method when making access
     * token requests.</i>
     * </blockquote>
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-2.3">RFC 6749,
     * 2.3. Client Authentication</a> mentions (1) HTTP Basic Authentication
     * and (2) {@code client_id} &amp; {@code client_secret} parameters in
     * the request body as the means of client authentication. This
     * implementation supports the both means.
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.2"
     *      >RFC 6749, 3.2. Token Endpoint</a>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            MultivaluedMap<String, String> parameters,
            @Context HttpServletRequest request)
    {
        String[] clientCertificates = extractClientCertificates(request);
        
        // Handle the token request.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new TokenRequestHandlerSpiImpl(), parameters, authorization, clientCertificates);
    }

    private String[] extractClientCertificates(HttpServletRequest request)
    {
        // try to get the certificates from the servlet context directly
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0)
        {
            // we didn't find any certificates in the servlet request, try extracting them from the headers instead
            List<String> headerCerts = new ArrayList<>();
            
            for (String headerName : clientCertificatePathHeaders)
            {
                String header = request.getHeader(headerName);
                if (header != null)
                {
                    headerCerts.add(header);
                }
            }

            if (headerCerts.isEmpty())
            {
                return null;
            }
            else
            {
                return headerCerts.toArray(new String[] {});
            }
        }
        else 
        {
            String[] pemEncoded = new String[certs.length];
            
            // used for encoding certificates
            Base64 base64 = new Base64(Base64.PEM_CHUNK_SIZE, "\n".getBytes());
            
            try
            {
                for (int i = 0; i < certs.length; i++)
                {
                    // encode each certificate in PEM format
                    StringBuilder sb = new StringBuilder();
                    sb.append("-----BEGIN CERTIFICATE-----\n");
                    sb.append(base64.encode(certs[i].getEncoded()));
                    sb.append("\n-----END CERTIFICATE-----\n");

                    pemEncoded[i] = sb.toString();
                    
                }
            } catch (CertificateEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            
            return pemEncoded;
            
        }
        
    }
}
