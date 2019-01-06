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
 * A class to communicate with Authlete's CIBA authentication device simulator.
 *
 * @author Hideki Ikeda
 */
public class AuthenticationDevice
{
    private static final class DEFAULT
    {
        private static class SYNC
        {
            private static final String AUTHENTICATION_ENDPOINT_PATH = "/sync";
            private static final int AUTHENTICATION_TIMEOUT = 10; // sec
            private static final int AUTHENTICATION_CONNECT_TIMEOUT = 10000; // millisec
            private static final int AUTHENTICATION_READ_TIMEOUT = 60000; // millisec
        }

        private static class ASYNC
        {
            private static final String AUTHENTICATION_ENDPOINT_PATH = "/async";
            private static final int AUTHENTICATION_TIMEOUT = 10;
            private static final int AUTHENTICATION_CONNECT_TIMEOUT = 10000;
            private static final int AUTHENTICATION_READ_TIMEOUT = 60000;
        }

        private static class POLL
        {
            private static final String AUTHENTICATION_ENDPOINT_PATH = "/poll";
            private static final String AUTHENTICATION_RESULT_ENDPOINT_PATH = "/result";
            private static final int AUTHENTICATION_TIMEOUT = 10;
            private static final int AUTHENTICATION_CONNECT_TIMEOUT = 10;
            private static final int AUTHENTICATION_READ_TIMEOUT = 10;
        }
    }


    private static final class InstanceHolder
    {
        private static final AuthenticationDevice INSTANCE = new AuthenticationDevice();
    }


    private final String mWorkspace;
    private final String mBaseUrl;
    private final Client mSyncAuthClient;
    private final String mSyncAuthenticationEndpointPath;
    private final int mSyncAuthenticationTimeout;
    private final int mSyncAuthenticationConnectTimeout;
    private final int mSyncAuthenticationReadTimeout;
    private final Client mAsyncAuthClient;
    private final String mAsyncAuthenticationEndpointPath;
    private final int mAsyncAuthenticationTimeout;
    private final int mAsyncAuthenticationConnectTimeout;
    private final int mAsyncAuthenticationReadTimeout;
    private final Client mPollAuthClient;
    private final String mPollAuthenticationEndpointPath;
    private final String mPollAuthenticationResultEndpointPath;
    private final int mPollAuthenticationTimeout;
    private final int mPollAuthenticationConnectTimeout;
    private final int mPollAuthenticationReadTimeout;

    // basic credentials?


    private AuthenticationDevice()
    {
        mWorkspace                        = ServerConfig.getAdWorkspace();
        mBaseUrl                          = ServerConfig.getAdBaseUrl();

        mSyncAuthenticationEndpointPath   = determineSyncAuthenticationEndpointPath();
        mSyncAuthenticationTimeout        = determineSyncAuthenticationTimeout();
        mSyncAuthenticationConnectTimeout = determineSyncAuthenticationConnectTimeout();
        mSyncAuthenticationReadTimeout    = determineSyncAuthenticationReadTimeout();
        mSyncAuthClient                   = createSyncClient();

        mAsyncAuthenticationEndpointPath   = determineAsyncAuthenticationEndpointPath();
        mAsyncAuthenticationTimeout        = determineAsyncAuthenticationTimeout();
        mAsyncAuthenticationConnectTimeout = determineAsyncAuthenticationConnectTimeout();
        mAsyncAuthenticationReadTimeout    = determineAsyncAuthenticationReadTimeout();
        mAsyncAuthClient                   = createAsyncClient();

        mPollAuthenticationEndpointPath       = determinePollAuthenticationEndpointPath();
        mPollAuthenticationResultEndpointPath = determinePollAuthenticationResultEndpointPath();
        mPollAuthenticationTimeout            = determinePollAuthenticationTimeout();
        mPollAuthenticationConnectTimeout     = determinePollAuthenticationConnectTimeout();
        mPollAuthenticationReadTimeout        = determinePollAuthenticationReadTimeout();
        mPollAuthClient                       = createPollClient();
    }


    private Client createSyncClient()
    {
        return createClient(mSyncAuthenticationReadTimeout, mSyncAuthenticationConnectTimeout);
    }


    private Client createAsyncClient()
    {
        return createClient(mAsyncAuthenticationReadTimeout, mAsyncAuthenticationConnectTimeout);
    }


    private Client createPollClient()
    {
        return createClient(mPollAuthenticationReadTimeout, mPollAuthenticationConnectTimeout);
    }


    private Client createClient(int readTimeout, int connectTimeout)
    {
        // Client configuration
        ClientConfig config = new ClientConfig();

        // Read timeout
        config.property(READ_TIMEOUT, readTimeout);

        // Connect timeout
        config.property(CONNECT_TIMEOUT, connectTimeout);

        // The client that synchronously communicates with the authentication device.
        return ClientBuilder.newClient(config);
    }


    private String determineSyncAuthenticationEndpointPath()
    {
        return determineStringParameter(
            ServerConfig.getAdSyncAuthenticationEndpointPath(),
            DEFAULT.SYNC.AUTHENTICATION_ENDPOINT_PATH
        );
    }


    private int determineSyncAuthenticationTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdSyncAuthenticationTimeout(),
            DEFAULT.SYNC.AUTHENTICATION_TIMEOUT
        );
    }


    private int determineSyncAuthenticationConnectTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdSyncAuthenticationConnectTimeout(),
            DEFAULT.SYNC.AUTHENTICATION_CONNECT_TIMEOUT
        );
    }


    private int determineSyncAuthenticationReadTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdSyncAuthenticationReadTimeout(),
            DEFAULT.SYNC.AUTHENTICATION_READ_TIMEOUT
        );
    }


    private String determineAsyncAuthenticationEndpointPath()
    {
        return determineStringParameter(
            ServerConfig.getAdSyncAuthenticationEndpointPath(),
            DEFAULT.ASYNC.AUTHENTICATION_ENDPOINT_PATH
        );
    }


    private int determineAsyncAuthenticationTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdAsyncAuthenticationTimeout(),
            DEFAULT.ASYNC.AUTHENTICATION_TIMEOUT
        );
    }


    private int determineAsyncAuthenticationConnectTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdAsyncAuthenticationConnectTimeout(),
            DEFAULT.ASYNC.AUTHENTICATION_CONNECT_TIMEOUT
        );
    }


    private int determineAsyncAuthenticationReadTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdAsyncAuthenticationReadTimeout(),
            DEFAULT.ASYNC.AUTHENTICATION_READ_TIMEOUT
        );
    }


    private String determinePollAuthenticationEndpointPath()
    {
        return determineStringParameter(
            ServerConfig.getAdPollAuthenticationEndpointPath(),
            DEFAULT.POLL.AUTHENTICATION_ENDPOINT_PATH
        );
    }


    private String determinePollAuthenticationResultEndpointPath()
    {
        return determineStringParameter(
            ServerConfig.getAdPollAuthenticationResultEndpointPath(),
            DEFAULT.POLL.AUTHENTICATION_RESULT_ENDPOINT_PATH
        );
    }


    private int determinePollAuthenticationTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdPollAuthenticationTimeout(),
            DEFAULT.POLL.AUTHENTICATION_TIMEOUT
        );
    }


    private int determinePollAuthenticationConnectTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdPollAuthenticationConnectTimeout(),
            DEFAULT.POLL.AUTHENTICATION_CONNECT_TIMEOUT
        );
    }


    private int determinePollAuthenticationReadTimeout()
    {
        return determineIntParameter(
            ServerConfig.getAdPollAuthenticationReadTimeout(),
            DEFAULT.POLL.AUTHENTICATION_READ_TIMEOUT
        );
    }


    private String determineStringParameter(String value, String defaultValue)
    {
        return value == null ? defaultValue : value;
    }


    private int determineIntParameter(int value, int defaultValue)
    {
        return value == 0 ? defaultValue : value;
    }


    /**
     * Get the singleton instance of this class.
     *
     * @return
     *         The instance of this class.
     */
    public static AuthenticationDevice getInstance()
    {
        return InstanceHolder.INSTANCE;
    }


    /**
     * Communicate with the authentication device simulator in sync mode.
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
    public SyncAuthenticationResponse syncAuth(String subject, String message)
    {
        // A request to be sent to the authentication device.
        SyncAuthenticationRequest request = new SyncAuthenticationRequest()
            .setWorkspace(mWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(mSyncAuthenticationTimeout);

        // Send the request to the authentication device as a HTTP Post request.
        return mSyncAuthClient
            .target(mBaseUrl)
            .path(mSyncAuthenticationEndpointPath)
            .request(APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, APPLICATION_JSON_TYPE.withCharset("UTF-8")), SyncAuthenticationResponse.class);
    }


    /**
     * Communicate with the authentication device simulator in async mode.
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
    public AsyncAuthenticationResponse asyncAuth(String subject, String message)
    {
        AsyncAuthenticationRequest request = new AsyncAuthenticationRequest()
            .setWorkspace(mWorkspace)
            .setUser(subject)
            .setMessage(message)
            .setTimeout(mSyncAuthenticationTimeout);

        // Send the request to the authentication device as a HTTP Post request.
        return mAsyncAuthClient
            .target(mBaseUrl)
            .path(mAsyncAuthenticationEndpointPath)
            .request(APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, APPLICATION_JSON_TYPE.withCharset("UTF-8")), AsyncAuthenticationResponse.class);
    }


    /**
     * Communicate with the authentication device simulator in poll mode.
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
    public PollAuthenticationResponse pollAuth(String subject, String message)
    {
        // TODO: Implement this.
        return null;
    }
}
