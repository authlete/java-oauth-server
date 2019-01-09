/*
 * Copyright (C) 2019 Authlete, Inc.
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


import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * A class to read properties from an external file or system properties.
 *
 * @author Hideki Ikeda
 */
public class ServerProperties extends TypedSystemProperties
{
    private static final ResourceBundle RESOURCE_BUNDLE;


    static
    {
        ResourceBundle bundle = null;

        try
        {
            bundle = ResourceBundle.getBundle("java-oauth-server");
        }
        catch (MissingResourceException mre)
        {
            // ignore
            mre.printStackTrace();
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

        // If the parameter identified by the key exists in the system properties.
        if (super.contains(key))
        {
            // Use the value of the system property.
            return super.getString(key, defaultValue);
        }

        // If "java-oauth-server.properties" is not available.
        if (RESOURCE_BUNDLE == null)
        {
            // Use the default value.
            return defaultValue;
        }

        try
        {
            // Search "java-oauth-server.properties" for the parameter.
            return RESOURCE_BUNDLE.getString(key);
        }
        catch (MissingResourceException e)
        {
            // Return the default value.
            return defaultValue;
        }
    }
}
