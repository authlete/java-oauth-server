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
package com.authlete.jaxrs.server.api;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import com.authlete.jaxrs.server.util.CertValidator;


public class OBBCertValidator extends CertValidator
{
    // Root certificates issued by OBB Authority.
    private static final Path[] ROOT_CERTIFICATES = {
            Paths.get(pwd(), "certs", "Open_Banking_Brasil_Sandbox_Root_G1.pem")
    };


    private static OBBCertValidator sInstance;
    private static boolean sInstantiationTried;


    private OBBCertValidator()
            throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IOException
    {
        super(ROOT_CERTIFICATES);
    }


    private static String pwd()
    {
        return Paths.get("").toAbsolutePath().toString();
    }


    public static synchronized OBBCertValidator getInstance() throws GeneralSecurityException
    {
        if (sInstantiationTried)
        {
            if (sInstance != null)
            {
                return sInstance;
            }

            throw new GeneralSecurityException(
                    "Certificate validator for Open Banking Brasil is not available.");
        }

        sInstantiationTried = true;

        try
        {
            sInstance = new OBBCertValidator();
            return sInstance;
        }
        catch (Exception e)
        {
            e.printStackTrace();

            throw new GeneralSecurityException(
                    "Failed to create a certificate validator for Open Banking Brasil: " + e.getMessage(), e);
        }
    }
}
