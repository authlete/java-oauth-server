package com.authlete.jaxrs.server.api.backchannel;


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
     * @param authReqId
     *         The authentication request ID ({@code auth_req_id}) issued to the
     *         client.
     *
     * @param expiresIn
     *         The duration of the issued authentication request ID ({@code auth_req_id})
     *         in seconds.
     *
     * @return
     *         A processor that communicates with the authentication device simulator
     *         for end-user authentication and authorization in synchronous mode.
     */
    public SyncAuthenticationDeviceProcessor(String ticket, User user, String clientName,
            String[] acrs, Scope[] scopes, String[] claimNames, String bindingMessage,
            String authReqId, int expiresIn)
    {
        super(ticket, user, clientName, acrs, scopes, claimNames, bindingMessage,
                authReqId, expiresIn);
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
            response = AuthenticationDevice.sync(mUser.getSubject(), buildMessage(),
                    computeAuthTimeout(), mAuthReqId);
        }
        catch (Throwable t)
        {
            // An unexpected error occurred when communicating with the authentication
            // device.
            completeWithTransactionFailed(
                    "Failed to communicate with the authentication device synchronously.");
            return;
        }

        // Handle the authentication/authorization result returned from the authentication
        // device.
        handleResult(response.getResult());
    }
}
