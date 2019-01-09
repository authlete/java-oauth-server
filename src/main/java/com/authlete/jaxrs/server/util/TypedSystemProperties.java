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


import java.util.Properties;
import com.authlete.common.util.StringBasedTypedProperties;


/**
 * A class for system properties.
 *
 * @author Hideki Ikeda
 */
public class TypedSystemProperties extends StringBasedTypedProperties
{
    @Override
    public boolean contains(String key)
    {
        Properties properties = System.getProperties();

        if (properties == null)
        {
            return false;
        }

        return properties.containsKey(key);
    }


    @Override
    public String getString(String key, String defaultValue)
    {
        if (key == null)
        {
            return defaultValue;
        }

        return System.getProperty(key, defaultValue);
    }


    @Override
    public void setString(String key, String value)
    {
        if (key == null)
        {
            return;
        }

        System.setProperty(key, value);
    }


    @Override
    public void remove(String key)
    {
        if (key == null)
        {
            return;
        }

        setString(key, null);
    }


    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("clear() is not supported.");
    }
}
