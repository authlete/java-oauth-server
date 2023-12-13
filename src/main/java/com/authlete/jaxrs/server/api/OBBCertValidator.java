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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import com.authlete.jaxrs.server.util.CertValidator;


public class OBBCertValidator extends CertValidator
{
    // The pattern of the environment variables each of which specifies
    // the path of a root certificate. The range of the number at the
    // end is from 0 to 9.
    private static final String ENV_ROOT_CERTIFICATE_PATTERN = "^OBB_ROOT_CERTIFICATE_[0-9]$";

    // Paths of root certificates that have been issued by OBB. These
    // are used as fallback when valid paths are not specified via the
    // environment variables.
    private static final Path[] ROOT_CERTIFICATES = {
            Paths.get(pwd(), "certs", "Open_Banking_Brasil_Sandbox_Root_G2.pem")
    };


    private static OBBCertValidator sInstance;
    private static boolean sInstantiationTried;


    private OBBCertValidator()
            throws CertificateException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, IOException
    {
        super(determineRootCertificates());
    }


    private static Path[] determineRootCertificates()
    {
        // The pattern of names of environment variables each of which
        // specifies the path of a root certificate.
        Pattern pattern = Pattern.compile(ENV_ROOT_CERTIFICATE_PATTERN);

        Set<Path> pathSet = new TreeSet<>();

        // For each environment variable.
        for (Map.Entry<String, String> entry : System.getenv().entrySet())
        {
            // The name of the environment variable.
            String name = entry.getKey();

            // If the name of the environment variable does not match the pattern.
            if (!pattern.matcher(name).matches())
            {
                continue;
            }

            // The path of a root certificate.
            Path path = Paths.get(entry.getValue());

            // If the path does not exist.
            if (!Files.exists(path))
            {
                System.err.format(
                        "[OBBCertValidator] Ignoring '%s' (specified by %s) because it does not exist.\n",
                        path.toString(), name);
                continue;
            }

            // If the path is a directory.
            if (Files.isDirectory(path))
            {
                System.err.format(
                        "[OBBCertValidator] Ignoring '%s' (specified by %s) because it is a directory.\n",
                        path.toString(), name);
                continue;
            }

            pathSet.add(path);
        }

        // Paths collected from the environment variables or the fallback.
        Path[] paths = (pathSet.size() == 0) ? ROOT_CERTIFICATES
                     : pathSet.toArray(new Path[pathSet.size()]);

        for (int i = 0; i < paths.length; ++i)
        {
            System.out.format(
                    "[OBBCertValidator] Using a root certificate [%d/%d]: %s\n",
                    (i+1), paths.length, paths[i].toString());
        }

        return paths;
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
