package com.authlete.jaxrs.server.api.backchannel;


import java.util.Date;
import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.ad.AuthenticationDevice;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationResponse;


/**
 * A processor that communicates with <a href="https://cibasim.authlete.com">
 * Authlete CIBA authentication device simulator</a> for end-user authentication
 * and authorization in synchronous mode.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication device
 *      simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">Authlete
 *      CIBA authentication device simulator API</a>
 *
 * @author Hideki Ikeda
 */
public class SyncAuthenticationDeviceProcessor extends BaseAuthenticationDeviceProcessor
{
    /**
     * Construct a processor that communicates with the authentication device simulator
     * for end-user authentication and authorization in synchronous mode.
     *
     * @param ticket
     *         A ticket that was issued by Authlete's {@code /api/backchannel/authentication}
     *         API.
     *
     * @param user
     *         An end-user to be authenticated and asked to authorize the client
     *         application.
     *
     * @param clientName
     *         The name of the client application.
     *
     * @param acrs
     *         The requested ACRs.
     *
     * @param scopes
     *         The requested scopes.
     *
     * @param claimNames
     *         The names of the requested claims.
     *
     * @param bindingMessage
     *         The binding message to be shown to the end-user on the authentication
     *         device.
     *
     * @return
     *         A processor that communicates with the authentication device simulator
     *         for end-user authentication and authorization in synchronous mode.
     */
    public SyncAuthenticationDeviceProcessor(String ticket, User user, String clientName, String[] acrs, Scope[] scopes, String[] claimNames, String bindingMessage)
    {
        super(ticket, user, clientName, acrs, scopes, claimNames, bindingMessage);
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
            response = AuthenticationDevice.syncAuth(mUser.getSubject(), buildMessage());
        }
        catch (Throwable t)
        {
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithTransactionFailed();
            return;
        }

        // The authentication result returned from the authentication device.
        com.authlete.jaxrs.server.ad.type.Result result = response.getResult();

        if (result == null)
        {
            // The result returned from the authentication device is empty.
            // This should never happen.
            completeWithTransactionFailed();
            return;
        }

        switch (result)
        {
            case allow:
                // The user authorized the client.
                completeWithAuthorized(new Date());
                return;

            case deny:
                // The user denied the client.
                completeWithAccessDenied();
                return;

            case timeout:
                // Timeout occurred on the authentication device.
                completeWithTransactionFailed("Timeout occurred on the authentication device.");
                return;

            default:
                // An unknown result returned from the authentication device.
                completeWithTransactionFailed();
                return;
        }
    }
}
