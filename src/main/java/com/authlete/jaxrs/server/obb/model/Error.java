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


public class Error implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String code;
    private String title;
    private String detail;


    public Error()
    {
    }


    public Error(String code, String title, String detail)
    {
        this.code   = code;
        this.title  = title;
        this.detail = detail;
    }


    public String getCode()
    {
        return code;
    }


    public Error setCode(String code)
    {
        this.code = code;

        return this;
    }


    public String getTitle()
    {
        return title;
    }


    public Error setTitle(String title)
    {
        this.title = title;

        return this;
    }


    public String getDetail()
    {
        return detail;
    }


    public Error setDetail(String detail)
    {
        this.detail = detail;

        return this;
    }
}
