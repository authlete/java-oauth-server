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


import java.io.Serializable;


/**
 * Configuration of ID federations.
 *
 * <pre>
 * {
 *     "federations": [
 *         (each element is mapped to {@link FederationConfig})
 *     ]
 * }
 * </pre>
 *
 * @see FederationsConfigLoader
 */
public class FederationsConfig implements Serializable
{
    private static final long serialVersionUID = 1L;


    private FederationConfig[] federations;


    public FederationConfig[] getFederations()
    {
        return federations;
    }


    public FederationsConfig setFederations(FederationConfig[] federations)
    {
        this.federations = federations;

        return this;
    }
}
