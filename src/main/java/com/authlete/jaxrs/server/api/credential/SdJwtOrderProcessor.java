package com.authlete.jaxrs.server.api.credential;


import java.util.Arrays;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.common.types.StandardClaims;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;


public class SdJwtOrderProcessor implements IOrderProcessor
{
    private static final String PAYLOAD_TEMPLATE = "{" +
            "\"type\":\"IdentityCredential\"," +
            "\"sub\":\"%s\"," +
            "\"given_name\":\"%s\"," +
            "\"family_name\":\"%s\"," +
            "\"birthdate\":\"%s\"" +
            "}";


    public CredentialIssuanceOrder toOrder(final IntrospectionResponse introspection,
                                           final CredentialRequestInfo info)
    {
        // The subject (the identifier of the user) that is associated with the access token.
        final String subject = introspection.getSubject();

        // The information about the user identified by the subject.
        final User user = UserDao.getBySubject(subject);

        // Some claims of the user.
        final String givenName = (String) user.getClaim(StandardClaims.GIVEN_NAME, null);
        final String familyName = (String) user.getClaim(StandardClaims.FAMILY_NAME, null);
        final String birthDate = (String) user.getClaim(StandardClaims.BIRTHDATE, null);

        // Prepare the credential payload.
        final String credentialPayload = String.format(PAYLOAD_TEMPLATE,
                                                       subject,
                                                       givenName,
                                                       familyName,
                                                       birthDate);

        return new CredentialIssuanceOrder()
                .setRequestIdentifier(info.getIdentifier())
                .setCredentialPayload(credentialPayload);
    }
}
