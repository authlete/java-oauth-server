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


import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;


/**
 * Helper class for validation on configuration of ID federations.
 */
class ConfigValidationHelper
{
    public static IllegalStateException illegalState(String format, Object... arguments)
    {
        String message = MessageFormat.format(format, arguments);

        return new IllegalStateException(message);
    }


    public static IllegalStateException lack(String key)
    {
        return illegalState("The ID federation configuration lacks ''{0}'' or its value is empty.", key);
    }


    public static void ensureNotEmpty(String key, Object value) throws IllegalStateException
    {
        if (value == null)
        {
            throw lack(key);
        }
    }


    public static void ensureNotEmpty(String key, String value) throws IllegalStateException
    {
        if (value == null || value.isEmpty())
        {
            throw lack(key);
        }
    }


    public static <T> void ensureNotEmpty(String key, T[] array) throws IllegalStateException
    {
        if (array == null || array.length == 0)
        {
            throw lack(key);
        }
    }


    public static void ensureUri(String key, String value) throws IllegalStateException
    {
        try
        {
            new URI(value);
        }
        catch (URISyntaxException e)
        {
            throw illegalState("The value of ''{0}'' in the ID federation configuration is malformed: {1}", key, value);
        }
    }
}
