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


import java.util.MissingResourceException;
import java.util.ResourceBundle;
import com.authlete.jaxrs.server.util.TypedSystemProperties;


/**
 * Reads resilience configuration from {@code resilience.properties} (on the
 * classpath) with JVM system properties taking precedence.
 *
 * <p>
 * This mirrors {@link com.authlete.jaxrs.server.util.ServerProperties
 * ServerProperties} but binds to the dedicated {@code resilience} resource
 * bundle so that resilience tuning lives in its own file, separate from the
 * server's functional configuration.
 * </p>
 *
 * @see ResilienceConfig
 */
class ResilienceProperties extends TypedSystemProperties
{
    private static final ResourceBundle RESOURCE_BUNDLE;


    static
    {
        ResourceBundle bundle = null;

        try
        {
            bundle = ResourceBundle.getBundle("resilience");
        }
        catch (MissingResourceException mre)
        {
            // The file is optional; built-in defaults will be used instead.
        }

        RESOURCE_BUNDLE = bundle;
    }


    @Override
    public String getString(String key, String defaultValue)
    {
        if (key == null)
        {
            return defaultValue;
        }

        // A JVM system property always wins over the file.
        if (super.contains(key))
        {
            return super.getString(key, defaultValue);
        }

        // The properties file is not available.
        if (RESOURCE_BUNDLE == null)
        {
            return defaultValue;
        }

        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            return defaultValue;
        }
    }
}