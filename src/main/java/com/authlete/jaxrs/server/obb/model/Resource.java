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


public class Resource implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String resourceId;
    private String type;
    private String status;


    public Resource()
    {
    }


    public Resource(String resourceId, String type, String status)
    {
        this.resourceId = resourceId;
        this.type       = type;
        this.status     = status;
    }


    public String getResourceId()
    {
        return resourceId;
    }


    public Resource setResourceId(String resourceId)
    {
        this.resourceId = resourceId;

        return this;
    }


    public String getType()
    {
        return type;
    }


    public Resource setType(String type)
    {
        this.type = type;

        return this;
    }


    public String getStatus()
    {
        return status;
    }


    public Resource setStatus(String status)
    {
        this.status = status;

        return this;
    }
}
