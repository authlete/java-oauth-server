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


import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Operations to access the resource server database.
 */
public class ResourceServerDao extends BaseDao
{
    private static final String RESOURCE_SERVER = "/resource_servers.json";


    /**
     * Holder of the cache of resource server entities.
     */
    private static final class ResourceServerEntityHolder
    {
        // Cache of resource server entities. Keys are resource server
        // IDs. Values are ResourceServerEntity objects loaded from JSON
        // files.
        private static final Map<String, ResourceServerEntity> INSTANCE =
                createResourceServers();
    }


    /**
     * Create the content of ResourceServersHolder.INSTANCE.
     */
    private static Map<String, ResourceServerEntity> createResourceServers()
    {
        return loadResourceServers(RESOURCE_SERVER)
                .stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s));
    }


    /**
     * Load configurations of resource servers from the resource.
     */
    private static List<ResourceServerEntity> loadResourceServers(String resource)
    {
        // Create a Reader to read the resource.
        try ( Reader reader = createReader(ResourceServerDao.class, resource) )
        {
            // The type of the object to be loaded.
            Type type = new TypeToken<ArrayList<ResourceServerEntity>>(){}.getType();

            // Convert the JSON in the resource into a list of ResourceServerEntity.
            return new Gson().fromJson(reader, type);
        }
        catch (IOException e)
        {
            // Failed to read the resource.
            e.printStackTrace();

            return Collections.emptyList();
        }
    }


    /**
     * Get a resource server entity.
     *
     * @param rsId
     *         The ID of a resource server.
     *
     * @return
     *         A resource server entity specified by the ID. null is
     *         returned when the resource server entity is unavailable.
     */
    public static ResourceServerEntity get(String rsId)
    {
        return ResourceServerEntityHolder.INSTANCE.get(rsId);
    }
}
