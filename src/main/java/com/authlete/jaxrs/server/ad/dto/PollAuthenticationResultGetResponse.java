package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;

import com.authlete.jaxrs.server.ad.type.Result;
import com.authlete.jaxrs.server.ad.type.Status;


public class PollAuthenticationResultGetResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    Status status;
    Result result;


    public Status getStatus()
    {
        return status;
    }


    public PollAuthenticationResultGetResponse setAuthenticationStatus(Status status)
    {
        this.status = status;

        return this;
    }


    public Result getResult()
    {
        return result;
    }


    public PollAuthenticationResultGetResponse setAuthenticationResult(Result result)
    {
        this.result = result;

        return this;
    }
}
