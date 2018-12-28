package com.authlete.jaxrs.server.api.backchannel;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.dto.Scope;
import com.authlete.common.dto.BackchannelAuthenticationCompleteRequest.Result;
import com.authlete.common.types.User;
import com.authlete.jaxrs.BackchannelAuthenticationCompleteHandler;


public abstract class BaseAuthenticationDeviceProcessor implements AuthenticationDeviceProcessor
{
    protected final String mTicket;
    protected final User mUser;
    protected final String[] mRequestedAcrs;
    protected final Scope[] mRequestedScopes;
    protected final String[] mRequestedClaimNames;
    protected final String mBindingMessage;


    public BaseAuthenticationDeviceProcessor(
            String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage)
    {
        mTicket              = ticket;
        mUser                = user;
        mRequestedAcrs       = requestedAcrs;
        mRequestedScopes     = requestedScopes;
        mRequestedClaimNames = requestedClaimNames;
        mBindingMessage      = bindingMessage;
    }


    protected void completeWithAuthorized(Date authTime)
    {
        complete(Result.AUTHORIZED, authTime);
    }


    protected void completeWithAccessDenied()
    {
        complete(Result.ACCESS_DENIED, null);
    }


    protected void completeWithError()
    {
        complete(Result.ERROR, null);
    }


    protected void complete(Result result, Date authTime)
    {
        // Complete the authentication and authorization process.
        new BackchannelAuthenticationCompleteHandler(
                AuthleteApiFactory.getDefaultApi(),
                new BackchannelAuthenticationCompleteHandlerSpiImpl(result, mUser, authTime, mRequestedAcrs)
            )
        .handle(mTicket, mRequestedClaimNames);
    }


    protected String buildMessage()
    {
        // TODO
        return String.format("[Binding Message]: '%s'\n[Requested Scopes]: '%s'", mBindingMessage, extractScopeNames());
    }


    protected String extractScopeNames()
    {
        if (mRequestedScopes == null || mRequestedScopes.length == 0)
        {
            return null;
        }

        List<String> scopeNames = new ArrayList<String>();

        for (Scope scope : mRequestedScopes)
        {
            scopeNames.add(scope.getName());
        }

        return String.join(",", scopeNames);
    }
}
