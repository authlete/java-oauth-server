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


/**
 * Meta
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_Meta"
 *      >Meta</a>
 */
public class Meta implements Serializable
{
    private static final long serialVersionUID = 1L;


    private int totalRecords;
    private int totalPages;
    private String requestDateTime;


    public Meta()
    {
    }


    public Meta(int totalRecords, int totalPages, String requestDateTime)
    {
        this.totalRecords    = totalRecords;
        this.totalPages      = totalPages;
        this.requestDateTime = requestDateTime;
    }


    public int getTotalRecords()
    {
        return totalRecords;
    }


    public Meta setTotalRecords(int totalRecords)
    {
        this.totalRecords = totalRecords;

        return this;
    }


    public int getTotalPages()
    {
        return totalPages;
    }


    public Meta setTotalPages(int totalPages)
    {
        this.totalPages = totalPages;

        return this;
    }


    public String getRequestDateTime()
    {
        return requestDateTime;
    }


    public Meta setRequestDateTime(String datetime)
    {
        this.requestDateTime = datetime;

        return this;
    }
}
