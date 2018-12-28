package com.authlete.jaxrs.server.api.backchannel;


import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;


public class PollAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    public PollAuthenticationDeviceProcessor(String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage)
    {
        super(ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);
    }


    @Override
    public void process()
    {
    }
}
