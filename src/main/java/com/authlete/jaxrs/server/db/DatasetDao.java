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
package com.authlete.jaxrs.server.db;


import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.gson.Gson;


/**
 * Sources of datasets (contents of "verified_claims").
 */
public class DatasetDao extends BaseDao
{
    // Sources of datasets. The JSON files have been copied from
    // https://bitbucket.org/openid/ekyc-ida/src/master/examples/response/
    //
    //   For "Inga" (subject = 1004)
    //     DOCUMENT_800_63A (trust_framework = "nist_800_63A")
    //     DOCUMENT_UKTDIF  (trust_framework = "uk_tfida")
    //
    private static final String DOCUMENT_800_63A = "/ekyc-ida/examples/response/document_800_63A.json";
    private static final String DOCUMENT_UKTDIF  = "/ekyc-ida/examples/response/document_UKTDIF.json";


    // List of [subject, resource... ]. Each element is a list whose first
    // element is "subject" (user identifier) and second & subsequent
    // elements are names of resource files.
    //
    // Values of "subject" should be found in UserDao.
    private static final List<List<String>> SUBJECT_RESOURCES_LIST = Arrays.asList(
            // Subject, Resource 0, Resource 1, ...
            Arrays.asList("1004", DOCUMENT_800_63A, DOCUMENT_UKTDIF)
    );


    /**
     * Holder of the cache of datasets.
     */
    private static final class SubjectDatasetsMapHolder
    {
        // Cache of datasets. Keys are subjects (user identifiers).
        // Values are contents of "verified_claims" objects loaded
        // from JSON files.
        private static final Map<String, List<Map<String, Object>>> INSTANCE =
                createSubjectDatasetsMap();
    }


    /**
     * Create the content of SubjectDatasetsMapHolder.INSTANCE.
     */
    private static Map<String, List<Map<String, Object>>> createSubjectDatasetsMap()
    {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();

        for (List<String> subjectResources : SUBJECT_RESOURCES_LIST)
        {
            // Subject (user identifier)
            String subject = subjectResources.get(0);

            // Datasets (loaded from resources)
            List<Map<String, Object>> datasets = subjectResources.stream().skip(1)
                    .map(resource -> loadDataset(resource)).collect(Collectors.toList());

            map.put(subject, datasets);
        }

        return map;
    }


    /**
     * Load a dataset (the content of "verified_claims") from the resource.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadDataset(String resource)
    {
        // Create a Reader to read the resource.
        try ( Reader reader = createReader(DatasetDao.class, resource) )
        {
            // Convert the JSON in the resource into a Map instance.
            Map<String, Object> map = new Gson().fromJson(reader, Map.class);

            // Return the content of "verified_claims".
            return (Map<String, Object>)map.get("verified_claims");
        }
        catch (IOException e)
        {
            // Failed to read the resource.
            e.printStackTrace();

            return Collections.emptyMap();
        }
    }


    /**
     * Get the datasets of the subject (user identifier).
     *
     * @param subject
     *         The subject of a user.
     *
     * @return
     *         List of datasets. Each dataset corresponds to the content of
     *         "verified_claims". null is returned when datasets of the
     *         specified subject are unavailable.
     */
    public static List<Map<String, Object>> get(String subject)
    {
        return SubjectDatasetsMapHolder.INSTANCE.get(subject);
    }
}
