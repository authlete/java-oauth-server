/*
 * Copyright (C) 2022 Authlete, Inc.
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
package com.authlete.jaxrs.server.api;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import com.authlete.common.ida.DatasetExtractor;


/**
 * Utility to build a new dataset that satisfies conditions of a
 * {@code "verified_claims"} request from one of the given datasets.
 *
 * <p>
 * This class is used in the {@code getVerifiedClaims(String, Object)}
 * method of {@code AuthorizationDecisionHandlerSpi} and
 * {@code UserInfoRequestHandlerSpi} implementations.
 * </p>
 *
 * <p>
 * A point to note is that this class uses {@link DatasetExtractor}
 * which is a generic component included in the
 * <a href="https://github.com/authlete/authlete-java-common"
 * >authlete-java-common</a> library. The DatasetExtractor class
 * implements the filtering rules and the data minimization policy
 * that are written in <a href=
 * "https://openid.net/specs/openid-connect-4-identity-assurance-1_0.html"
 * >OpenID Connect for Identity Assurance 1.0</a>.
 * </p>
 *
 * @see <a href="https://openid.net/specs/openid-connect-4-identity-assurance-1_0.html"
 *      >OpenID Connect for Identity Assurance 1.0</a>
 */
class VerifiedClaimsBuilder
{
    // The content of "verified_claims" request. List or Map.
    private final Object mRequest;

    // Available datasets of a particular subject.
    private List<Map<String, Object>> mDatasets;


    public VerifiedClaimsBuilder(Object request, List<Map<String, Object>> datasets)
    {
        mRequest  = request;
        mDatasets = datasets;
    }


    @SuppressWarnings("unchecked")
    public Object build()
    {
        // If no dataset is available.
        if (mDatasets == null || mDatasets.size() == 0)
        {
            // The content of "verified_claims" cannot be built.
            return null;
        }

        // The request is a List instance or a Map instance.

        // LIST: "verified_claims": [ { ... }, ... ]
        if (mRequest instanceof List)
        {
            return buildList((List<Map<String, Object>>)mRequest, mDatasets);
        }

        // MAP: "verified_claims": { ... }
        if (mRequest instanceof Map)
        {
            return buildMap((Map<String, Object>)mRequest, mDatasets);
        }

        // The flow reaches here when the "claims" request parameter of the
        // authorization request does not include "verified_claims" or its
        // value is neither a JSON array nor a JSON object. The latter case
        // is a specification violation.
        return null;
    }


    private List<Map<String, Object>> buildList(
            List<Map<String, Object>> requests, List<Map<String, Object>> datasets)
    {
        // Utility to build a new dataset that meets conditions of a
        // "verified_claims" request from one of available datasets.
        DatasetExtractor extractor = createDatasetExtractor();

        // Build a new dataset for each element in the 'requests' array.
        List<Map<String, Object>> results = requests.stream()
                .map(request -> extractor.extract(request, datasets))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                ;

        // If none of the available datasets could satisfy any of the
        // elements in the 'requests' array.
        if (results.size() == 0)
        {
            // No content for "verified_claims" response.
            return null;
        }

        // Content for "verified_claims" response.
        return results;
    }


    private Map<String, Object> buildMap(
            Map<String, Object> request, List<Map<String, Object>> datasets)
    {
        // Build a new dataset that meets conditions of the request
        // from one of the available datasets.
        return createDatasetExtractor().extract(request, datasets);
    }


    private DatasetExtractor createDatasetExtractor()
    {
        // Create a dataset extractor.
        return new DatasetExtractor()
                .setLogger(LoggerFactory.getLogger(getClass()))
                ;
    }
}
