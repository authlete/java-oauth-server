package com.authlete.jaxrs.server.api.credential;


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
