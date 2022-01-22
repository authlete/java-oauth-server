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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manager for ID federation.
 *
 * <p>
 * This manager loads configurations of ID federations from a file using
 * {@link FederationsConfigLoader} and creates {@link Federation} instances
 * for the configurations. The loaded configurations and created instances
 * are cached for later use.
 * </p>
 */
public class FederationManager
{
    private static class Holder
    {
        private static final FederationManager INSTANCE = new FederationManager();
    }


    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FederationConfig[] mConfigurations;
    private final Map<String, Federation> mFederations;


    private FederationManager()
    {
        // Load configurations of ID federations from a file.
        mConfigurations = loadConfigurations();

        // Create Federation instances for the configurations.
        mFederations = buildFederations(mConfigurations);
    }


    private FederationConfig[] loadConfigurations()
    {
        FederationsConfig config;

        try
        {
            // Load configurations of ID federations from a file.
            // The default location of the file is "federations.json".
            config = FederationsConfigLoader.load();
        }
        catch (Exception e)
        {
            logger.warn("Failed to load configurations of ID federations: " + e.getMessage());
            return null;
        }

        // "federations" in the configuration file.
        FederationConfig[] configs = config.getFederations();

        if (configs == null || configs.length == 0)
        {
            // If the configuration does not include "federations" or
            // its value is an empty array.
            logger.warn("The configuration of ID federations does not include 'federations' or its value is empty.");
            return null;
        }

        List<FederationConfig> validConfigs = new ArrayList<>();

        // For each entry in the "federations".
        for (int i = 0; i < configs.length; ++i)
        {
            FederationConfig cf = configs[i];

            // If the configuration at the index is invalid.
            if (!isConfigurationValid(cf, i))
            {
                // Skip the entry in the configuration of ID federations.
                continue;
            }

            validConfigs.add(cf);
        }

        if (validConfigs.size() == 0)
        {
            // No valid configuration in the "federations".
            return null;
        }

        return validConfigs.toArray(new FederationConfig[validConfigs.size()]);
    }


    private boolean isConfigurationValid(FederationConfig config, int index)
    {
        if (config == null)
        {
            logger.warn("The entry at the index {} in the configuration of ID federations is empty.", index);
            return false;
        }

        try
        {
            // Validate the content of the configuration.
            config.validate();
        }
        catch (Exception e)
        {
            logger.warn("The entry at the index {} in the configuration of ID federations is invalid: {}", index, e.getMessage());
            return false;
        }

        return true;
    }


    private Map<String, Federation> buildFederations(FederationConfig[] configs)
    {
        if (configs == null)
        {
            return null;
        }

        Map<String, Federation> federations = new HashMap<>();

        // For each entry in the "federations".
        for (FederationConfig config : configs)
        {
            // Create a Federation instance using the configuration.
            // The instance provides public methods for ID federation.
            // The discovery document and the JWK set document of the
            // OpenID provider will be cached in the instance.
            Federation federation = new Federation(config);

            // Register the Federation instance for later use.
            federations.put(config.getId(), federation);

            logger.info("An ID federation configuration was loaded: id={}, issuer={}",
                    config.getId(), config.getServer().getIssuer());
        }

        return federations;
    }


    public static FederationManager getInstance()
    {
        return Holder.INSTANCE;
    }


    public FederationConfig[] getConfigurations()
    {
        return mConfigurations;
    }


    public Federation getFederation(String federationId)
    {
        if (mFederations == null || federationId == null)
        {
            return null;
        }

        return mFederations.get(federationId);
    }
}
