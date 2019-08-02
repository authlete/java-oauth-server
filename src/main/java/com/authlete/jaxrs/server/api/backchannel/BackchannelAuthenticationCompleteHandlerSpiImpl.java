/*
 * Copyright (C) 2019 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.api.backchannel;


import static com.authlete.jaxrs.server.util.ExceptionUtil.internalServerErrorException;
import java.net.URI;
import java.util.Date;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientProperties;
import com.authlete.common.dto.BackchannelAuthenticationCompleteRequest.Result;
import com.authlete.common.dto.BackchannelAuthenticationCompleteResponse;
import com.authlete.common.types.User;
import com.authlete.jaxrs.spi.BackchannelAuthenticationCompleteRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.BackchannelAuthenticationCompleteRequestHandlerSpi
 * BackchannelAuthenticationCompleteRequestHandlerSpi} interface which needs to
 * be given to the constructor of {@link com.authlete.jaxrs.BackchannelAuthenticationCompleteRequestHandler
 * BackchannelAuthenticationCompleteRequestHandler}.
 *
 * @author Hideki Ikeda
 */
public class BackchannelAuthenticationCompleteHandlerSpiImpl extends BackchannelAuthenticationCompleteRequestHandlerSpiAdapter
{
    /**
     * The result of end-user authentication and authorization.
     */
    private final Result mResult;


    /**
     * The authenticated user.
     */
    private final User mUser;


    /**
     * The time when the user was authenticated in seconds since Unix epoch.
     */
    private long mUserAuthenticatedAt;


    /**
     * Requested ACRs.
     */
    private String[] mAcrs;


    /**
     * The description of the error.
     */
    private String mErrorDescription;


    /**
     * The URI of a document which describes the error in detail.
     */
    private URI mErrorUri;


    public BackchannelAuthenticationCompleteHandlerSpiImpl(
            Result result, User user, Date userAuthenticatedAt, String[] acrs,
            String errorDescription, URI errorUri)
    {
        // The result of end-user authentication and authorization.
        mResult = result;

        // The end-user.
        mUser = user;

        if (result != Result.AUTHORIZED)
        {
            // The description of the error.
            mErrorDescription = errorDescription;

            // The URI of a document which describes the error in detail.
            mErrorUri = errorUri;

            // The end-user has not authorized the client.
            return;
        }

        // The time at which end-user has been authenticated.
        mUserAuthenticatedAt = (userAuthenticatedAt == null) ? 0 : userAuthenticatedAt.getTime() / 1000L;

        // The requested ACRs.
        mAcrs = acrs;
    }


    @Override
    public Result getResult()
    {
        return mResult;
    }


    @Override
    public String getUserSubject()
    {
        return mUser.getSubject();
    }


    @Override
    public long getUserAuthenticatedAt()
    {
        return mUserAuthenticatedAt;
    }


    @Override
    public String getAcr()
    {
        // Note that this is a dummy implementation. Regardless of whatever
        // the actual authentication was, this implementation returns the
        // first element of the requested ACRs if it is available.
        //
        // Of course, this implementation is not suitable for commercial use.

        if (mAcrs == null || mAcrs.length == 0)
        {
            return null;
        }

        // The first element of the requested ACRs.
        String acr = mAcrs[0];

        if (acr == null || acr.length() == 0)
        {
            return null;
        }

        // Return the first element of the requested ACRs. Again,
        // this implementation is not suitable for commercial use.
        return acr;
    }


    @Override
    public Object getUserClaim(String claimName)
    {
        return mUser.getClaim(claimName, null);
    }


    @Override
    public void sendNotification(BackchannelAuthenticationCompleteResponse info)
    {
        // The URL of the consumption device's notification endpoint.
        URI clientNotificationEndpointUri = info.getClientNotificationEndpoint();

        // The token that is needed for client authentication at the consumption
        // device's notification endpoint.
        String notificationToken = info.getClientNotificationToken();

        // The notification content (JSON) to send to the consumption device.
        String notificationContent = info.getResponseContent();

        // Send the notification to the consumption device's notification endpoint.
        Response response =
                doSendNotification(clientNotificationEndpointUri, notificationToken, notificationContent);

        // The status of the response from the consumption device.
        Status status = Status.fromStatusCode(response.getStatusInfo().getStatusCode());

        // TODO: CIBA specification does not specify how to deal with responses
        // returned from the consumption device in case of error push notification.
        // Then, even in case of error push notification, the current implementation
        // treats the responses as in the case of successful push notification.

        // Check if the "HTTP 200 OK" or "HTTP 204 No Content".
        if (status == Status.OK || status == Status.NO_CONTENT)
        {
            // In this case, the request was successfully processed by the consumption
            // device since the specification says as follows.
            //
            //   CIBA Core spec, 10.2. Ping Callback and 10.3. Push Callback
            //     For valid requests, the Client Notification Endpoint SHOULD
            //     respond with an HTTP 204 No Content.  The OP SHOULD also accept
            //     HTTP 200 OK and any body in the response SHOULD be ignored.
            //
            return;
        }

        if (status.getFamily() == Status.Family.REDIRECTION)
        {
            // HTTP 3xx code. This case must be ignored since the specification
            // says as follows.
            //
            //   CIBA Core spec, 10.2. Ping Callback, 10.3. Push Callback
            //     The Client MUST NOT return an HTTP 3xx code.  The OP MUST
            //     NOT follow redirects.
            //
            return;
        }
    }


    private Response doSendNotification(URI clientNotificationEndpointUri,
            String notificationToken, String notificationContent)
    {
        // A web client to send a notification to the consumption device's notification
        // endpoint.
        Client webClient = createClient();

        try
        {
            // Send the notification to the consumption device..
            return webClient.target(clientNotificationEndpointUri).request()
                    // CIBA Core says "The OP MUST NOT follow redirects."
                    .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + notificationToken)
                    .post(Entity.json(notificationContent));
        }
        catch (Throwable t)
        {
            // Failed to send the notification to the consumption device.
            throw internalServerErrorException(
                    t.getMessage() + ": Failed to send the notification to the consumption device");
        }
        finally
        {
            // Close the web client.
            webClient.close();
        }
    }


    @Override
    public String getErrorDescription()
    {
        return mErrorDescription;
    }


    @Override
    public URI getErrorUri()
    {
        return mErrorUri;
    }


    private Client createClient()
    {
        // SSLContext's for older TLS versions ("TLSv1" and "TLSv1.1") may not
        // include any FAPI cipher suites. Here we create an SSLContext with
        // "TLSv1.2" whose getDefaultSSLParameters().getCipherSuites() probably
        // includes FAPI cipher suites.
        SSLContext sc = createSslContext("TLSv1.2");

        return ClientBuilder.newBuilder().sslContext(sc).build();
    }


    private SSLContext createSslContext(String protocol)
    {
        try
        {
            // Get an SSL context for the protocol.
            SSLContext sc = SSLContext.getInstance(protocol);

            // Initialize the SSL context.
            sc.init(null, null, null);

            return sc;
        }
        catch (Exception e)
        {
            throw internalServerErrorException(
                    "Failed to get an SSLContext for " + protocol + ": " + e.getMessage());
        }
    }
}
