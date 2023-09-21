package com.authlete.jaxrs.server.util;


import java.util.Collection;
import java.util.LinkedList;
import com.authlete.common.dto.CredentialIssuanceOrder;
import com.authlete.common.dto.CredentialRequestInfo;
import com.authlete.common.dto.IntrospectionResponse;
import com.authlete.jaxrs.server.api.vci.OrderFormat;


public class CredentialUtil
{
    public static CredentialIssuanceOrder toOrder(final IntrospectionResponse introspection,
                                                  final CredentialRequestInfo info)
            throws UnknownCredentialFormatException
    {
        final String formatId = info.getFormat();
        final OrderFormat format = OrderFormat.byId(formatId);
        if (format == null)
        {
            throw new UnknownCredentialFormatException(String.format("Unsupported credential format %s.", formatId));
        }

        return format.getProcessor()
                .toOrder(introspection, info);
    }


    public static CredentialIssuanceOrder[] toOrder(final IntrospectionResponse introspection,
                                                    final CredentialRequestInfo[] infos)
            throws UnknownCredentialFormatException
    {
        final Collection<CredentialIssuanceOrder> orders = new LinkedList<>();
        for(final CredentialRequestInfo info : infos)
        {
            orders.add(toOrder(introspection, info));
        }

        return orders.toArray(new CredentialIssuanceOrder[0]);
    }


    public static class UnknownCredentialFormatException extends Exception {
        UnknownCredentialFormatException(final String message) {
            super(message);
        }

        public String getJsonError()
        {
            return StringUtil.toJsonError(this.getMessage());
        }
    }
}
