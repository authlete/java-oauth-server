package com.authlete.jaxrs.server.util;

public class StringUtil {

    public static String toJsonError(final String error)
    {
        return String.format("{\"error\": \"%s\"", error);
    }

}
