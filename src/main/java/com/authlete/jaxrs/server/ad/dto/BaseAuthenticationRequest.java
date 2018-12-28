package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;


public class BaseAuthenticationRequest<T extends BaseAuthenticationRequest<T>> implements Serializable
{
    private static final long serialVersionUID = 1L;


    private String workspace;
    private String user;
    private String message;
    private int timeout;


    public String getWorkspace()
    {
        return workspace;
    }


    @SuppressWarnings("unchecked")
    public T setWorkspace(String workspace)
    {
        this.workspace = workspace;

        return (T)this;
    }


    public String getUser()
    {
        return user;
    }


    @SuppressWarnings("unchecked")
    public T setUser(String user)
    {
        this.user = user;

        return (T)this;
    }


    public String getMessage()
    {
        return message;
    }


    @SuppressWarnings("unchecked")
    public T setMessage(String message)
    {
        this.message = message;

        return (T)this;
    }


    public int getTimeout()
    {
        return timeout;
    }


    @SuppressWarnings("unchecked")
    public T setTimeout(int timeout)
    {
        this.timeout = timeout;

        return (T)this;
    }
}
