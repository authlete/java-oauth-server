package com.authlete.jaxrs.server.api.backchannel;


import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.ad.type.Mode;


public class AuthenticationDeviceProcessorFactory
{
    public static AuthenticationDeviceProcessor create(Mode mode, String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage)
    {
        if (mode == null)
        {
            throw new IllegalArgumentException("Mode must be specified.");
        }

        switch (mode)
        {
            case SYNC:
                return new SyncAuthenticationDeviceProcessor(
                        ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);

            case ASYNC:
                return new AsyncAuthenticationDeviceProcessor(
                        ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);

            case POLL:
                return new PollAuthenticationDeviceProcessor(
                        ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);

            default:
                // Unsupported mode. This never happens.
                throw new RuntimeException("Unsupported mode.");
        }
    }

}
