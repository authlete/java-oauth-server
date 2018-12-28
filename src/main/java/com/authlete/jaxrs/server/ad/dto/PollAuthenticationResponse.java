package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;


public class PollAuthenticationResponse implements Serializable
{
    private static final long serialVersionUID = 1L;


    String requestId;


    public String getRequestId()
    {
        return requestId;
    }


    public PollAuthenticationResponse setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }
}
