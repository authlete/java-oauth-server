package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;


public class PollAuthenticationResultGetRequest  implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String requestId;


    public String getRequestId()
    {
        return requestId;
    }


    public PollAuthenticationResultGetRequest setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }
}
