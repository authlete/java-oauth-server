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
 * ResponseResourceList.
 *
 * @see <a href="https://openbanking-brasil.github.io/areadesenvolvedor/#tocS_ResponseResourceList"
 *      >ResponseResourceList</a>
 */
public class ResponseResourceList implements Serializable
{
    private static final long serialVersionUID = 1L;


    private Resource[] data;
    private Links links;
    private Meta meta;


    public ResponseResourceList()
    {
    }


    public ResponseResourceList(Resource[] data, Links links, Meta meta)
    {
        this.data  = data;
        this.links = links;
        this.meta  = meta;
    }


    public Resource[] getData()
    {
        return data;
    }


    public ResponseResourceList setData(Resource[] data)
    {
        this.data = data;

        return this;
    }


    public Links getLinks()
    {
        return links;
    }


    public ResponseResourceList setLinks(Links links)
    {
        this.links = links;

        return this;
    }


    public Meta getMeta()
    {
        return meta;
    }


    public ResponseResourceList setMeta(Meta meta)
    {
        this.meta = meta;

        return this;
    }
}
