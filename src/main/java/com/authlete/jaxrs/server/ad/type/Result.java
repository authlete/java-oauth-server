package com.authlete.jaxrs.server.ad.type;


public enum Result
{
    allow((short)1, "allow"),


    deny((short)2, "deny"),


    timeout((short)3, "timeout")
    ;


    private final short mValue;
    private final String mString;


    private Result(short value, String string)
    {
        mValue = value;
        mString = string;
    }


    public short getValue()
    {
        return mValue;
    }


    @Override
    public String toString()
    {
        return mString;
    }
}
