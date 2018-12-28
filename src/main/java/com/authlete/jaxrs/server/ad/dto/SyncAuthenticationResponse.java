package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;
import com.authlete.jaxrs.server.ad.type.Result;


public class SyncAuthenticationResponse implements Serializable
{
    private static final long serialVersionUID = 1L;


    private Result result;


    public Result getResult()
    {
        return result;
    }


    public SyncAuthenticationResponse setResult(Result result)
    {
        this.result = result;

        return this;
    }
}
