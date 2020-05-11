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
package com.authlete.jaxrs.server.ad;


import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import org.glassfish.jersey.client.ClientConfig;
import com.authlete.jaxrs.server.ServerConfig;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationRequest;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationResponse;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationRequest;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResponse;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResultRequest;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResultResponse;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationRequest;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationResponse;


/**
 * A class to communicate with <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">
 * Authlete CIBA authentication device simulator API</a>.
 *
 * @see <a href="https://cibasim.authlete.com">Authlete CIBA authentication
 *      device simulator</a>
 *
 * @see <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">Authlete CIBA authentication device simulator API</a>
 *
 * @author Hideki Ikeda
 */
public class AuthenticationDevice
{
    /**
     * The limit values for end-user authentication/authorization timeout defined
     * by <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">Authlete
     * CIBA authentication device simulator API</a>.
     */
    public static final int AUTH_TIMEOUT_MIN = 5;
    public static final int AUTH_TIMEOUT_MAX = 60;


    /**
     * Authlete CIBA authentication simulator API endpoints.
     */
    private static final String SYNC_ENDPOINT_PATH   = "/api/authenticate/sync";
    private static final String ASYNC_ENDPOINT_PATH  = "/api/authenticate/async";
    private static final String POLL_ENDPOINT_PATH   = "/api/authenticate/poll";
    private static final String POLL_RESULT_ENDPOINT_PATH = "/api/authenticate/result";


    /**
     * Parameters required to communicate with the authentication device simulator.
     */
    private static final String sBaseUrl                = ServerConfig.getAuthleteAdBaseUrl();
    private static final String sWorkspace              = ServerConfig.getAuthleteAdWorkspace();
    private static final int sSyncConnectTimeout        = ServerConfig.getAuthleteAdSyncConnectTimeout();
    private static final int sSyncAdditionalReadTimeout = ServerConfig.getAuthleteAdSyncAdditionalReadTimeout();
    private static final int sAsyncConnectTimeout       = ServerConfig.getAuthleteAdAsyncConnectTimeout();
    private static final int sAsyncReadTimeout          = ServerConfig.getAuthleteAdAsyncReadTimeout();
    private static final int sPollConnectTimeout        = ServerConfig.getAuthleteAdPollConnectTimeout();
    private static final int sPollReadTimeout           = ServerConfig.getAuthleteAdPollReadTimeout();
    private static final int sPollResultConnectTimeout  = ServerConfig.getAuthleteAdPollResultConnectTimeout();
    private static final int sPollResultReadTimeout     = ServerConfig.getAuthleteAdPollResultReadTimeout();


    private static Client createClient(int readTimeout, int connectTimeout)
    {
        // Client configuration.
        ClientConfig config = new ClientConfig();

        // Read timeout.
        config.property(READ_TIMEOUT, readTimeout);

        // Connect timeout.
        config.property(CONNECT_TIMEOUT, connectTimeout);

        // The client that synchronously communicates with the authentication device simulator.
        return ClientBuilder.newClient(config);
    }


    /**
     * Send a request to the authentication device simulator for end-user authentication
     * and authorization in synchronous mode.
     *
     * @param subject
     *         The subject of the end-user to be authenticated and asked to authorize
     *         the client application.
     *
     * @param message
     *         A message to be shown to the end-user on the authentication device.
     *
     * @param authTimeout
     *         The value of timeout in seconds for the end-user authentication/authorization
     *         on the authentication device.
     *
     * @param actionizeToken
     *         A token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     *         to automate authentication device responses.
     *
     * @return
     *         A response from the authentication device.
     */
    public static SyncAuthenticationResponse sync(String subject, String message,
            int authTimeout, String actionizeToken)
    {
        // Determine the read timeout in milliseconds based on the value of the
        // authentication timeout. This should be a bit longer than the timeout
        // for end-user authentication/authorization.
        int readTimeout = authTimeout * 1000 + sSyncAdditionalReadTimeout;

        // Create a web client to communicate with the authentication device.
        Client client = createClient(readTimeout, sSyncConnectTimeout);

        // A request to be sent to the authentication device.
        SyncAuthenticationRequest request = new SyncAuthenticationRequest()
            .setWorkspace(sWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(authTimeout)
            .setActionizeToken(actionizeToken);

        // Send the request as an HTTP POST request.
        return post(client, SYNC_ENDPOINT_PATH, request, SyncAuthenticationResponse.class);
    }


    /**
     * Send a request to the authentication device simulator for for end-user authentication
     * and authorization in asynchronous mode.
     *
     * @param subject
     *         The subject of the end-user to be authenticated and asked to authorize
     *         the client application.
     *
     * @param message
     *         A message to be shown to the end-user on the authentication device.
     *
     * @param authTimeout
     *         The value of timeout in seconds for the end-user authentication/authorization
     *         on the authentication device.
     *
     * @param actionizeToken
     *         A token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     *         to automate authentication device responses.
     *
     * @return
     *         A response from the authentication device simulator.
     */
    public static AsyncAuthenticationResponse async(String subject, String message,
            int authTimeout, String actionizeToken)
    {
        // Create a web client to communicate with the authentication device.
        Client client = createClient(sAsyncReadTimeout, sAsyncConnectTimeout);

        // A request to be sent to the authentication device.
        AsyncAuthenticationRequest request = new AsyncAuthenticationRequest()
            .setWorkspace(sWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(authTimeout)
            .setActionizeToken(actionizeToken);

        // Send the request as an HTTP POST request.
        return post(client, ASYNC_ENDPOINT_PATH, request, AsyncAuthenticationResponse.class);
    }


    /**
     * Send a request to the authentication device simulator for end-user authentication
     * and authorization in poll mode.
     *
     * @param subject
     *         The subject of the end-user to be authenticated and asked to authorize
     *         the client application.
     *
     * @param message
     *         A message to be shown to the end-user on the authentication device.
     *
     * @param authTimeout
     *         The value of timeout in seconds for the end-user authentication/authorization
     *         on the authentication device.
     *
     * @param actionizeToken
     *         A token that is used with the actionize endpoint ({@code /api/atuhenticate/actionize})
     *         to automate authentication device responses.
     *
     * @return
     *         A response from the authentication device simulator.
     */
    public static PollAuthenticationResponse poll(String subject, String message,
            int authTimeout, String actionizeToken)
    {
        // Create a web client to communicate with the authentication device.
        Client client = createClient(sPollReadTimeout, sPollConnectTimeout);

        // A request to be sent to the authentication device.
        PollAuthenticationRequest request = new PollAuthenticationRequest()
            .setWorkspace(sWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(authTimeout)
            .setActionizeToken(actionizeToken);

        // Send the request as an HTTP POST request.
        return post(client, POLL_ENDPOINT_PATH, request, PollAuthenticationResponse.class);
    }


    /**
     * Send a request to the authentication device simulator for fetching the result
     * of the end-user authentication and authorization in poll mode.
     *
     * @param requestId
     *         A request ID that was returned from the authentication simlator's
     *         poll result endpoint ({@code /api/authenticate/result}).
     *
     * @return
     *         A response from the authentication device simulator.
     */
    public static PollAuthenticationResultResponse pollResult(String requestId)
    {
        // Create a web client to communicate with the authentication device.
        Client client = createClient(sPollResultReadTimeout, sPollResultConnectTimeout);

        // A request to be sent to the authentication device.
        PollAuthenticationResultRequest request = new PollAuthenticationResultRequest()
            .setRequestId(requestId);

        // Send the request as an HTTP POST request.
        return post(client, POLL_RESULT_ENDPOINT_PATH, request, PollAuthenticationResultResponse.class);
    }


    private static <TRequest, TResponse> TResponse post(Client client, String path,
            TRequest request, Class<TResponse> responseClass)
    {
        try
        {
            // Send the request to the authentication device as a HTTP Post request.
            return client
                .target(sBaseUrl)
                .path(path)
                .request()
                .post(Entity.json(request), responseClass);
        }
        finally
        {
            // Close the client.
            client.close();
        }
    }
}
