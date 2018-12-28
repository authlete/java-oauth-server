package com.authlete.jaxrs.server.ad.dto;


public class AsyncAuthenticationRequest extends BaseAuthenticationRequest<AsyncAuthenticationRequest>
{
    private static final long serialVersionUID = 1L;


    private String state;


    public String getState()
    {
        return state;
    }


    public AsyncAuthenticationRequest setState(String state)
    {
        this.state = state;

        return this;
    }
}
