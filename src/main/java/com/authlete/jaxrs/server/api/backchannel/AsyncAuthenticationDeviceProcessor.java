package com.authlete.jaxrs.server.api.backchannel;


import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.ad.AuthenticationDevice;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationResponse;


public class AsyncAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    public AsyncAuthenticationDeviceProcessor(String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage)
    {
        super(ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);
    }


    @Override
    public void process()
    {
        // The response to be returned from the authentication device.
        AsyncAuthenticationResponse response;

        try
        {
            // Communicate with the authentication device to authenticate the user and
            // authorize the client's request.
            response = AuthenticationDevice.getInstance().asyncAuth(mUser.getSubject(), buildMessage());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithError();
            return;
        }

        // OK. The communication between this authorization server and the authentication
        // device has been successfully done.

        // The ID of the request that was sent to the authentication device.
        String requestId = response.getRequestId();

        System.out.println("obtained request ID: " + requestId);

        // Check the request ID.
        if (requestId == null || requestId.length() == 0)
        {
            // The request ID was invalid. This should never happen.
            completeWithError();
            return;
        }

        // OK. The request ID returned from the authentication device is valid.
        // In this case, the authentication/authorization process does not complete
        // in this class. Instead, an authentication/authorization result will be
        // returned form the authentication device to the BackchannelAuthenticationCallbackEndpoint
        // of this authorization server later and then the authentication/authorization
        // process will complete there. Then, we need to store some information
        // required to complete the process (e.g. ticket, claim names, etc...) at
        // the BackchannelAuthenticationCallbackEndpoint.

        // Information about this authentication/authorization process.
        AuthInfo info = new AuthInfo(mTicket, mUser, mRequestedClaimNames, mRequestedAcrs);

        // Store the information into the holder for later use.
        // See BackchannelAuthenticationCallbackEndpoint for more details.
        AuthInfoHolder.put(requestId, info);
    }
}
