package com.authlete.jaxrs.server.util;


import java.util.Arrays;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.api.credential.OrderFormat;


public class CredentialUtil
{
    public static CredentialIssuanceOrder toOrder(final IntrospectionResponse introspection,
                                                  final CredentialRequestInfo info)
    {
        final String formatId = info.getFormat();
        final OrderFormat format = OrderFormat.byId(formatId);
        if (format == null)
        {
            throw ExceptionUtil.badRequestException(String.format("Unsupported credential format %s.", formatId));
        }

        return format.getProcessor()
                .toOrder(introspection, info);
    }


    public static CredentialIssuanceOrder[] toOrder(final IntrospectionResponse introspection,
                                                    final CredentialRequestInfo[] infos)
    {
        return Arrays.stream(infos)
                .map(info -> toOrder(introspection, info))
                .toArray(CredentialIssuanceOrder[]::new);
    }
}
