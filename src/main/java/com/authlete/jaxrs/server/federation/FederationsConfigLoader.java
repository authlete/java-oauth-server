/*
 * Copyright (C) 2022 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.authlete.jaxrs.server.federation;


import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.Gson;


/**
 * Loader for configuration of ID federations.
 *
 * <p>
 * This loader loads configuration from a file. When the name of a configuration
 * file is not explicitly specified, in other words, when {@link #load()} method
 * is used, the file name is determined in the following order.
 * </p>
 *
 * <ol>
 * <li>Environment variable, {@code FEDERATIONS_FILE}
 * <li>System property, {@code federations.file}
 * <li>The default file name, {@code "federations.json"}
 * </ol>
 *
 * <p>
 * The content of the configuration file should be a JSON object that contains
 * {@code "federations"} as a top-level property.
 * </p>
 *
 * <pre>
 * {
 *     "federations": [
 *         (each element is mapped to {@link FederationConfig})
 *     ]
 * }
 * </pre>
 *
 * @see FederationsConfig
 */
public class FederationsConfigLoader
{
    private static final String DEFAULT_FILE = "federations.json";
    private static final String SYSPROP_FILE = "federations.file";
    private static final String ENVVAR_FILE  = "FEDERATIONS_FILE";


    private static String determineFile()
    {
        // From the environment variable.
        String file = getFileFromEnv();

        if (file == null)
        {
            // From the system property.
            file = getFileFromSysProp();
        }

        if (file == null)
        {
            // The default file.
            file = DEFAULT_FILE;
        }

        return file;
    }


    private static String getFileFromEnv()
    {
        return System.getenv(ENVVAR_FILE);
    }


    private static String getFileFromSysProp()
    {
        return System.getProperty(SYSPROP_FILE);
    }


    public static FederationsConfig load() throws IOException
    {
        return load(determineFile());
    }


    public static FederationsConfig load(String file) throws IOException
    {
        return load(Paths.get(file));
    }


    public static FederationsConfig load(Path path) throws IOException
    {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
        {
            return load(reader);
        }
    }


    public static FederationsConfig load(Reader reader) throws IOException
    {
        return new Gson().fromJson(reader, FederationsConfig.class);
    }
}
