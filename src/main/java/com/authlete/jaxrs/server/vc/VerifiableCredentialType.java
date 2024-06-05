/*
 * Copyright (C) 2023-2024 Authlete, Inc.
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
package com.authlete.jaxrs.server.vc;


import java.util.Arrays;
import com.authlete.common.types.StandardClaims;


/**
 * Verifiable Credential Type identified by the "{@code vct}" claim
 * in an SD-JWT VC.
 */
public enum VerifiableCredentialType
{
    IDENTITY_CREDENTIAL(
            "https://credentials.example.com/identity_credential",
            new String[] {
                    StandardClaims.GIVEN_NAME,
                    StandardClaims.FAMILY_NAME,
                    StandardClaims.BIRTHDATE
            }
    ),

    /**
     * The vct used in the <a href="https://www.digital-identity-wallet.eu/"
     * >POTENTIAL</a> Interop Event Track 2.
     *
     * <blockquote>
     * <table border="1" cellpadding="5" style="border-collapse: collapse;">
     *   <tr bgcolor="orange">
     *     <th>vct</th>
     *     <th>claims</th>
     *   </tr>
     *   <tr>
     *     <td style="vertical-align: top;">
     *       <code>urn:eu.europa.ec.eudi:pid:1</code>
     *     </td>
     *     <td style="vertical-align: top;">
     *       <ul>
     *         <li><code>family_name</code>
     *         <li><code>given_name</code>
     *         <li><code>birthdate</code>
     *         <li><code>age_equal_or_over/18</code>
     *         <li><code>place_of_birth/locality</code>
     *         <li><code>address/formatted</code>
     *         <li><code>issuing_authority</code>
     *         <li><code>issuing_country</code>
     *       </ul>
     *     </td>
     *   </tr>
     * </table>
     * </blockquote>
     *
     * @see <a href="https://gitlab.opencode.de/potential/interop-event/-/tree/master/track2/description"
     *      >POTENTIAL Interop Event Track 2 / description</a>
     */
    EUDI_PID_1(
            "urn:eu.europa.ec.eudi:pid:1",
            new String[] {
                    StandardClaims.FAMILY_NAME,
                    StandardClaims.GIVEN_NAME,
                    StandardClaims.BIRTHDATE,
                    "age_equal_or_over",
                    "place_of_birth",
                    StandardClaims.ADDRESS,
                    "issuing_authority",
                    "issuing_country",
            }
    ),
    ;


    private final String id;
    private final String[] claims;


    private VerifiableCredentialType(String id, String[] claims)
    {
        this.id     = id;
        this.claims = claims;
    }


    public String getId()
    {
        return id;
    }


    public String[] getClaims()
    {
        return claims;
    }


    public static VerifiableCredentialType byId(final String id)
    {
        return Arrays.stream(VerifiableCredentialType.values())
                .filter(format -> format.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
