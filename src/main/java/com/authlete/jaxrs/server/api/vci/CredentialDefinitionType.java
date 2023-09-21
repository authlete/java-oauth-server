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
package com.authlete.jaxrs.server.api.vci;


import java.util.Arrays;
import com.authlete.common.types.StandardClaims;


public enum CredentialDefinitionType
{
    IDENTITY_CREDENTIAL("IdentityCredential", new String[]{
            StandardClaims.GIVEN_NAME,
            StandardClaims.FAMILY_NAME,
            StandardClaims.BIRTHDATE
    });


    final String id;
    final String[] claims;


    CredentialDefinitionType(final String typeId,
                             final String[] claims)
    {
        this.id = typeId;
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


    public static CredentialDefinitionType byId(final String id)
    {
        return Arrays.stream(CredentialDefinitionType.values())
                .filter(format -> format.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
