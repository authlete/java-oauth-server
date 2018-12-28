package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import com.authlete.jaxrs.server.ad.type.Result;


public class AsyncAuthenticationCallbackRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    @XmlElement(name = "request_id")
    private String requestId;

    private Result result;
    private String state;


    public String getRequestId()
    {
        return requestId;
    }


    public AsyncAuthenticationCallbackRequest setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }


    public Result getResult()
    {
        return result;
    }


    public AsyncAuthenticationCallbackRequest setResult(Result result)
    {
        this.result = result;

        return this;
    }


    public String getState()
    {
        return state;
    }


    public AsyncAuthenticationCallbackRequest setState(String state)
    {
        this.state = state;

        return this;
    }
}
