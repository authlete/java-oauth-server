package com.authlete.jaxrs.server.api.backchannel;


import java.util.Date;
import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.ad.AuthenticationDevice;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationResponse;


public class SyncAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    public SyncAuthenticationDeviceProcessor(String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage)
    {
        super(ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);
    }


    @Override
    public void process()
    {
        // The response from the authentication device.
        SyncAuthenticationResponse response;

        try
        {
            // Perform the end-user authentication and authorization by communicating
            // with the authentication device in the sync mode.
            response = AuthenticationDevice.getInstance().syncAuth(mUser.getSubject(), buildMessage());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithError();
            return;
        }

        // The authentication result returned from the authentication device.
        com.authlete.jaxrs.server.ad.type.Result result = response.getResult();

        if (result == null)
        {
            // The result returned from the authentication deivce is empty.
            // This should never happen.
            completeWithError();
            return;
        }

        switch (result)
        {
            case allow:
                System.out.println("AD 'allow'");
                // The user authorized the client.
                completeWithAuthorized(new Date());
                return;

            case deny:
                System.out.println("AD 'deny'");
                // The user denied the client.
                completeWithAccessDenied();
                return;

            case timeout:
                System.out.println("AD 'timeout'");
                // Timeout occurred while the authentication device was authenticating
                // the user.
                completeWithError();
                return;

            default:
                System.out.println("AD 'default'");
                // An unknown result returned from the authentication device.
                // This should never happen.
                completeWithError();
                return;
        }
    }
}
