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


import static com.authlete.jaxrs.server.federation.ConfigValidationHelper.ensureNotEmpty;
import java.io.Serializable;


/**
 * Configuration of ID federation.
 *
 * <pre>
 * {
 *     "id": "(unique identifier among the configurations)",
 *     "server": {
 *         (mapped to {@link ServerConfig})
 *     },
 *     "client": {
 *         (mapped to {@link ClientConfig})
 *     }
 * }
 * </pre>
 *
 * <p>
 * The value of {@code "id"} is used as <code><i>federationId</i></code>
 * in the following API paths.
 * </p>
 *
 * <ul>
 * <li><code>/api/federation/initiation/<i>federationId</i></code>
 * <li><code>/api/federation/callback/<i>federationId</i></code>
 * </ul>
 *
 * @see FederationsConfig
 */
public class FederationConfig implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String id;
    private ServerConfig server;
    private ClientConfig client;


    public String getId()
    {
        return id;
    }


    public FederationConfig setId(String id)
    {
        this.id = id;

        return this;
    }


    public ServerConfig getServer()
    {
        return server;
    }


    public FederationConfig setServer(ServerConfig server)
    {
        this.server = server;

        return this;
    }


    public ClientConfig getClient()
    {
        return client;
    }


    public FederationConfig setClient(ClientConfig client)
    {
        this.client = client;

        return this;
    }


    public void validate() throws IllegalStateException
    {
        ensureNotEmpty("id", id);
        ensureNotEmpty("server", server);
        ensureNotEmpty("client", client);

        server.validate();
        client.validate();
    }
}
