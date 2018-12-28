package com.authlete.jaxrs.server.ad.type;


public enum Status
{
    ACTIVE((short)1, "active"),


    COMPLETE((short)2, "complete"),


    TIMEOUT((short)3, "timeout")
    ;


    private static final Status[] sValues = values();
    private final short mValue;
    private final String mString;


    private Status(short value, String string)
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


    /**
     * This method is needed for JAX-RS to map a string to an instance of this enum.
     */
    public static Status fromString(String status)
    {
        if (status == null)
        {
            return null;
        }

        for (Status value : sValues)
        {
            if (value.mString.equals(status))
            {
                // Found.
                return value;
            }
        }

        // Not found.
        return null;
    }


    public boolean isComplete()
    {
        return this == COMPLETE;
    }
}
