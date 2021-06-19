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
 * Links
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_Links"
 *      >Links</a>
 */
public class Links implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String self;
    private String first;
    private String prev;
    private String next;
    private String last;


    public String getSelf()
    {
        return self;
    }


    public Links setSelf(String self)
    {
        this.self = self;

        return this;
    }


    public String getFirst()
    {
        return first;
    }


    public Links setFirst(String first)
    {
        this.first = first;

        return this;
    }


    public String getPrev()
    {
        return prev;
    }


    public Links setPrev(String prev)
    {
        this.prev = prev;

        return this;
    }


    public String getNext()
    {
        return next;
    }


    public Links setNext(String next)
    {
        this.next = next;

        return this;
    }


    public String getLast()
    {
        return last;
    }


    public Links setLast(String last)
    {
        this.last = last;

        return this;
    }
}
