package com.authlete.jaxrs.server.api.credential;


import java.util.Arrays;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;


public interface IOrderProcessor
{
    CredentialIssuanceOrder toOrder(final IntrospectionResponse introspection,
                                    final CredentialRequestInfo info);


    default CredentialIssuanceOrder[] toOrder(final IntrospectionResponse introspection,
                                              final CredentialRequestInfo[] infos)
    {
        return Arrays.stream(infos)
                .map(info -> this.toOrder(introspection, info))
                .toArray(CredentialIssuanceOrder[]::new);
    }
}
