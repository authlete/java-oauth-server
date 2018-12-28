package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;


public class AsyncAuthenticationResponse implements Serializable
{
    private static final long serialVersionUID = 1L;


    @XmlElement(name = "request_id")
    private String requestId;


    public String getRequestId()
    {
        return requestId;
    }


    public AsyncAuthenticationResponse setRequestId(String requestId)
    {
        this.requestId = requestId;

        return this;
    }
}
