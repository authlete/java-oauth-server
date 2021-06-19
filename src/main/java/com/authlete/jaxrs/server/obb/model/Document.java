/*
 * Copyright (C) 2021 Authlete, Inc.
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
package com.authlete.jaxrs.server.obb.model;


import java.io.Serializable;


public class Document implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String identification;
    private String rel;


    public String getIdentification()
    {
        return identification;
    }


    public Document setIdentification(String identification)
    {
        this.identification = identification;

        return this;
    }


    public String getRel()
    {
        return rel;
    }


    public Document setRel(String rel)
    {
        this.rel = rel;

        return this;
    }
}
