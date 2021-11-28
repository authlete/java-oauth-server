/*
 * Copyright (C) 2019-2020 Authlete, Inc.
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


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.authlete.common.assurance.Claims;
import com.authlete.common.assurance.Document;
import com.authlete.common.assurance.IDDocument;
import com.authlete.common.assurance.Issuer;
import com.authlete.common.assurance.Verification;
import com.authlete.common.assurance.VerifiedClaims;
import com.authlete.common.assurance.constraint.VerifiedClaimsConstraint;


/**
 * Operations to access the database of verified claims.
 */
public class VerifiedClaimsDao
{
    // Dummy database for verified claims. Keys are end-user subjects.
    private static final Map<String, VerifiedClaims> sVerifiedClaimsDB =
            buildVerifiedClaimsDB();


    private static Map<String, VerifiedClaims> buildVerifiedClaimsDB()
    {
        Map<String, VerifiedClaims> db = new HashMap<String, VerifiedClaims>();

        setupVerifiedClaimsDB(db);

        return db;
    }


    private static void setupVerifiedClaimsDB(Map<String, VerifiedClaims> db)
    {
        db.put("1003", new VerifiedClaims()
            .setVerification(new Verification()
                .setTrustFramework("de_aml")
                .setTime("2012-04-23T18:25:43+01")
                .setVerificationProcess("676q3636461467647q8498785747q487")
                .addEvidence(new IDDocument()
                    .setMethod("pipp")
                    .setDocument(new Document()
                        .setType("idcard")
                        .setIssuer(new Issuer()
                            .setName("Stadt Augsburg")
                            .setCountry("DE")
                        )
                        .setNumber("53554554")
                        .setDateOfIssuance("2012-04-23")
                        .setDateOfExpiry("2022-04-22")
                    )
                )
            )
            .setClaims(new Claims()
                .putClaim("given_name","Max")
                .putClaim("family_name", "Meier")
                .putClaim("birthdate", "1956-01-28")
                .putClaim("nationalities", Arrays.asList("USA", "DEU"))
            )
        );
    }


    public static List<VerifiedClaims> get(String subject, VerifiedClaimsConstraint constraint)
    {
        // NOTE:
        // Commercial implementations should have complex logic to construct
        // verified claims based on the constraint.
        VerifiedClaims vc = sVerifiedClaimsDB.get(subject);

        return Arrays.asList(vc);
    }
}
