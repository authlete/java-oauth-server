package com.authlete.jaxrs.server.api.backchannel;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AuthInfoHolder
{
    private static final Map<String, AuthInfo> sHolder = new ConcurrentHashMap<String, AuthInfo>();


    public static AuthInfo get(String requestId)
    {
        return sHolder.get(requestId);
    }


    public static AuthInfo put(String requestId, AuthInfo info)
    {
        return sHolder.put(requestId, info);
    }


    public static AuthInfo remove(String requestId)
    {
        return sHolder.remove(requestId);
    }
}
