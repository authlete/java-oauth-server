/*
 * Copyright (C) 2023 Authlete, Inc.
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
package com.authlete.jaxrs.server.db;


import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;


public class BaseDao
{
    /**
     * Create a Reader instance that reads the specified resource.
     */
    protected static Reader createReader(Class<?> clazz, String resource)
    {
        return new InputStreamReader(
                clazz.getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
