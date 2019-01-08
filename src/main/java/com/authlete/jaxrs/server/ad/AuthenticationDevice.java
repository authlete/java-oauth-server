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


import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import org.glassfish.jersey.client.ClientConfig;
import com.authlete.jaxrs.server.ServerConfig;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationRequest;
import com.authlete.jaxrs.server.ad.dto.AsyncAuthenticationResponse;
import com.authlete.jaxrs.server.ad.dto.PollAuthenticationResponse;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationRequest;
import com.authlete.jaxrs.server.ad.dto.SyncAuthenticationResponse;


/**
 * A class to communicate with {@link <a href="https://app.swaggerhub.com/apis-docs/Authlete/cibasim">
 * Authlete CIBA authentication device simulator API</a>}.
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
     * Authlete CIBA authentication simulator API endpoints.
     */
    private static final String SYNC_AUTHENTICATION_ENDPOINT_PATH        = "/api/authenticate/sync";
    private static final String ASYNC_AUTHENTICATION_ENDPOINT_PATH       = "/api/authenticate/async";
    private static final String POLL_AUTHENTICATION_ENDPOINT_PATH        = "/api/authenticate/poll";
    private static final String POLL_AUTHENTICATION_RESULT_ENDPOINT_PATH = "/api/authenticate/result";


    /**
     * Parameters required to communicate with the authentication device simulator.
     */
    private static final String sBaseUrl                 = ServerConfig.getAdBaseUrl();
    private static final String sWorkspace               = ServerConfig.getAdWorkspace();
    private static final int sSyncAuthenticationTimeout  = ServerConfig.getAdSyncAuthenticationTimeout();
    private static final int sSyncConnectTimeout         = ServerConfig.getAdSyncConnectTimeout();
    private static final int sSyncReadTimeout            = ServerConfig.getAdSyncReadTimeout();
    private static final int sAsyncAuthenticationTimeout = ServerConfig.getAdAsyncAuthenticationTimeout();
    private static final int sAsyncConnectTimeout        = ServerConfig.getAdAsyncConnectTimeout();
    private static final int sAsyncReadTimeout           = ServerConfig.getAdAsyncReadTimeout();
    private static final int sPollAuthenticationTimeout  = ServerConfig.getAdPollAuthenticationTimeout();
    private static final int sPollConnectTimeout         = ServerConfig.getAdPollConnectTimeout();
    private static final int sPollReadTimeout            = ServerConfig.getAdPollReadTimeout();;
    private static final Client sSyncClient              = createClient(sSyncReadTimeout, sSyncConnectTimeout);
    private static final Client sAsyncClient             = createClient(sAsyncReadTimeout, sAsyncConnectTimeout);
    private static final Client sPollClient              = createClient(sPollReadTimeout, sPollConnectTimeout);


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
     *         The message to be shown to the end-user on the authentication device.
     *
     * @return
     *         A response from the authentication device.
     *
     * @throws ResponseProcessingException
     *         in case processing of a received HTTP response fails (e.g. in a filter
     *         or during conversion of the response entity data to an instance
     *         of a particular Java type).
     *
     * @throws ProcessingException
     *         in case the request processing or subsequent I/O operation fails.
     *
     * @throws WebApplicationException
     *         in case the response status code of the response returned by the
     *         server is not {@link javax.ws.rs.core.Response.Status.Family#SUCCESSFUL
     *         successful} and the specified response type is not {@link javax.ws.rs.core.Response}.
     */
    public static SyncAuthenticationResponse syncAuth(String subject, String message)
    {
        // A request to be sent to the authentication device.
        SyncAuthenticationRequest request = new SyncAuthenticationRequest()
            .setWorkspace(sWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(sSyncAuthenticationTimeout);

        // Send the request to the authentication device as a HTTP Post request.
        return sSyncClient
            .target(sBaseUrl)
            .path(SYNC_AUTHENTICATION_ENDPOINT_PATH)
            .request(APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, APPLICATION_JSON_TYPE.withCharset("UTF-8")), SyncAuthenticationResponse.class);
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
     *         The message to be shown to the end-user on the authentication device.
     *
     * @return
     *         A response from the authentication device simulator.
     *
     * @throws ResponseProcessingException
     *         in case processing of a received HTTP response fails (e.g. in a filter
     *         or during conversion of the response entity data to an instance
     *         of a particular Java type).
     *
     * @throws ProcessingException
     *         in case the request processing or subsequent I/O operation fails.
     *
     * @throws WebApplicationException
     *         in case the response status code of the response returned by the
     *         server is not {@link javax.ws.rs.core.Response.Status.Family#SUCCESSFUL
     *         successful} and the specified response type is not {@link javax.ws.rs.core.Response}.
     */
    public static AsyncAuthenticationResponse asyncAuth(String subject, String message)
    {
        AsyncAuthenticationRequest request = new AsyncAuthenticationRequest()
            .setWorkspace(sWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(sAsyncAuthenticationTimeout);

        // Send the request to the authentication device as a HTTP Post request.
        return sAsyncClient
            .target(sBaseUrl)
            .path(ASYNC_AUTHENTICATION_ENDPOINT_PATH)
            .request(APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, APPLICATION_JSON_TYPE.withCharset("UTF-8")), AsyncAuthenticationResponse.class);
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
     *         The message to be shown to the end-user on the authentication device.
     *
     * @return
     *         A response from the authentication device simulator.
     *
     * @throws ResponseProcessingException
     *         in case processing of a received HTTP response fails (e.g. in a filter
     *         or during conversion of the response entity data to an instance
     *         of a particular Java type).
     *
     * @throws ProcessingException
     *         in case the request processing or subsequent I/O operation fails.
     *
     * @throws WebApplicationException
     *         in case the response status code of the response returned by the
     *         server is not {@link javax.ws.rs.core.Response.Status.Family#SUCCESSFUL
     *         successful} and the specified response type is not {@link javax.ws.rs.core.Response}.
     */
    public PollAuthenticationResponse pollAuth(String subject, String message)
    {
        // TODO: Implement this.
        return null;
    }
}
