package com.authlete.jaxrs.server.api.credential;


import java.util.Arrays;


public enum OrderFormat
{
    SD_JWT("vc+sd-jwt", new SdJwtOrderProcessor());


    private String id;
    private IOrderProcessor processor;


    OrderFormat(final String id,
                final IOrderProcessor processor)
    {
        this.id = id;
        this.processor = processor;
    }


    public String getId()
    {
        return id;
    }


    public IOrderProcessor getProcessor()
    {
        return processor;
    }


    public static OrderFormat byId(final String id)
    {
        return Arrays.stream(OrderFormat.values())
                .filter(format -> format.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
