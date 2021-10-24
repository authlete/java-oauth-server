/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.util;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import com.authlete.jaxrs.util.CertificateUtils;


public class CertValidator
{
    private static final CertificateFactory sCertificateFactory = getCertificateFactoryInstance();
    private static final CertPathValidator  sCertPathValidator  = getCertPathValidatorInstance();
    private final PKIXParameters mParameters;


    private static CertificateFactory getCertificateFactoryInstance()
    {
        try
        {
            return CertificateFactory.getInstance("X.509");
        }
        catch (CertificateException e)
        {
            // This won't happen.
            e.printStackTrace();
            return null;
        }
    }


    private static CertPathValidator getCertPathValidatorInstance()
    {
        try
        {
            return CertPathValidator.getInstance("PKIX");
        }
        catch (NoSuchAlgorithmException e)
        {
            // This won't happen.
            e.printStackTrace();
            return null;
        }
    }


    private static CertificateFactory certificateFactory()
    {
        return sCertificateFactory;
    }


    private static CertPathValidator certPathValidator()
    {
        return sCertPathValidator;
    }


    private static PKIXParameters createParameters(Path... anchorCertificates)
            throws CertificateException, InvalidAlgorithmParameterException, IOException
    {
        Set<TrustAnchor> anchors = new HashSet<>();

        for (Path anchorCertificate : anchorCertificates)
        {
            anchors.add(createTrustAnchor(anchorCertificate));
        }

        PKIXParameters params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false);

        return params;
    }


    private static TrustAnchor createTrustAnchor(Path anchorCertificate)
            throws CertificateException, IOException
    {
        return new TrustAnchor(createCertificate(anchorCertificate), null);
    }


    private static X509Certificate createCertificate(Path certificate)
            throws CertificateException, IOException
    {
        try (InputStream in = Files.newInputStream(certificate))
        {
            return (X509Certificate)certificateFactory().generateCertificate(in);
        }
    }


    public CertValidator(Path...anchorCertificates)
            throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IOException
    {
        mParameters = createParameters(anchorCertificates);
    }


    public PKIXCertPathValidatorResult validate(CertPath certPath)
            throws CertPathValidatorException, InvalidAlgorithmParameterException
    {
        return (PKIXCertPathValidatorResult)
                certPathValidator().validate(certPath, mParameters);
    }


    public PKIXCertPathValidatorResult validate(List<? extends Certificate> certificates)
            throws CertPathValidatorException, InvalidAlgorithmParameterException, CertificateException
    {
        return validate(certificateFactory().generateCertPath(certificates));
    }


    public PKIXCertPathValidatorResult validate(String... certificates)
            throws CertPathValidatorException, InvalidAlgorithmParameterException, CertificateException
    {
        List<Certificate> certs = new ArrayList<>(certificates.length);

        for (String certificate : certificates)
        {
            certs.add(toCertificate(certificate));
        }

        return validate(certs);
    }


    public PKIXCertPathValidatorResult validate(HttpServletRequest request) throws GeneralSecurityException
    {
        // Extract the chain of the client certificate.
        String[] chain = CertificateUtils.extractChain(request);

        // If no certificate chain is included.
        if (chain == null || chain.length == 0)
        {
            throw new GeneralSecurityException(
                    "The HTTP request does not contain a certificate chain.");
        }

        return validate(chain);
    }


    private static Certificate toCertificate(String certificate) throws CertificateException
    {
        certificate = normalizeCertificate(certificate);

        try (InputStream in = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8)))
        {
            return certificateFactory().generateCertificate(in);
        }
        catch (IOException e)
        {
            // This won't happen.
            e.printStackTrace();
            return null;
        }
    }


    private static String normalizeCertificate(String certificate)
    {
        String pem = certificate.replaceAll("\\s+(?!CERTIFICATE-----)", "\n").trim();

        if (pem.startsWith("-----BEGIN CERTIFICATE"))
        {
            return pem.trim();
        }

        return new StringBuilder()
            .append("-----BEGIN CERTIFICATE-----\n")
            .append(pem)
            .append("\n-----END CERTIFICATE-----")
            .toString();
    }
}
