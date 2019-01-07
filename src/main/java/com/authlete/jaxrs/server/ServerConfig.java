package com.authlete.jaxrs.server;


public class ServerConfig
{
    // TODO: Load the resource form the external file.
    private static final Object reource = null;

    private static final String sAdWorkspace;
    private static final String sAdBaseUrl;
    private static final String sAdSyncAuthenticationEndpointPath;
    private static final int sAdSyncAuthenticationTimeout;
    private static final int sAdSyncAuthenticationReadTimeout;
    private static final int sAdSyncAuthenticationConnectTimeout;
    private static final String sAdAsyncAuthenticationEndpointPath;
    private static final int sAdAsyncAuthenticationTimeout;
    private static final int sAdAsyncAuthenticationReadTimeout;
    private static final int sAdAsyncAuthenticationConnectTimeout;
    private static final String sAdPollAuthenticationEndpointPath;
    private static final String sAdPollAuthenticationResultEndpointPath;
    private static final int sAdPollAuthenticationTimeout;
    private static final int sAdPollAuthenticationReadTimeout;
    private static final int sAdPollAuthenticationConnectTimeout;


    static
    {
        sAdWorkspace = "authlete-hide/test-all";
        sAdBaseUrl   = "https://cibasim.authlete.com";

        sAdSyncAuthenticationEndpointPath    = null;
        sAdSyncAuthenticationTimeout         = 0;
        sAdSyncAuthenticationReadTimeout     = 0;
        sAdSyncAuthenticationConnectTimeout  = 0;

        sAdAsyncAuthenticationEndpointPath   = null;
        sAdAsyncAuthenticationTimeout        = 0;
        sAdAsyncAuthenticationReadTimeout    = 0;
        sAdAsyncAuthenticationConnectTimeout = 0;

        sAdPollAuthenticationEndpointPath       = null;
        sAdPollAuthenticationResultEndpointPath = null;
        sAdPollAuthenticationTimeout            = 0;
        sAdPollAuthenticationReadTimeout        = 0;
        sAdPollAuthenticationConnectTimeout     = 0;
    }


    public static String getAdWorkspace()
    {
        return sAdWorkspace;
    }


    public static String getAdBaseUrl()
    {
        return sAdBaseUrl;
    }


    public static String getAdSyncAuthenticationEndpointPath()
    {
        return sAdSyncAuthenticationEndpointPath;
    }


    public static int getAdSyncAuthenticationTimeout()
    {
        return sAdSyncAuthenticationTimeout;
    }


    public static int getAdSyncAuthenticationReadTimeout()
    {
        return sAdSyncAuthenticationReadTimeout;
    }


    public static int getAdSyncAuthenticationConnectTimeout()
    {
        return sAdSyncAuthenticationConnectTimeout;
    }


    public static String getAdAsyncAuthenticationEndpointPath()
    {
        return sAdAsyncAuthenticationEndpointPath;
    }


    public static int getAdAsyncAuthenticationTimeout()
    {
        return sAdAsyncAuthenticationTimeout;
    }


    public static int getAdAsyncAuthenticationReadTimeout()
    {
        return sAdAsyncAuthenticationReadTimeout;
    }


    public static int getAdAsyncAuthenticationConnectTimeout()
    {
        return sAdAsyncAuthenticationConnectTimeout;
    }


    public static String getAdPollAuthenticationEndpointPath()
    {
        return sAdPollAuthenticationEndpointPath;
    }


    public static String getAdPollAuthenticationResultEndpointPath()
    {
        return sAdPollAuthenticationResultEndpointPath;
    }


    public static int getAdPollAuthenticationTimeout()
    {
        return sAdPollAuthenticationTimeout;
    }


    public static int getAdPollAuthenticationReadTimeout()
    {
        return sAdPollAuthenticationReadTimeout;
    }


    public static int getAdPollAuthenticationConnectTimeout()
    {
        return sAdPollAuthenticationConnectTimeout;
    }
}
