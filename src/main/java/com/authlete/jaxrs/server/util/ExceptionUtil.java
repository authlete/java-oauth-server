/*
 * Copyright (C) 2019-2023 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.jaxrs.server.util;


import static com.authlete.jaxrs.server.util.ResponseUtil.badRequest;
import static com.authlete.jaxrs.server.util.ResponseUtil.badRequestJson;
import static com.authlete.jaxrs.server.util.ResponseUtil.forbidden;
import static com.authlete.jaxrs.server.util.ResponseUtil.forbiddenJson;
import static com.authlete.jaxrs.server.util.ResponseUtil.internalServerError;
import static com.authlete.jaxrs.server.util.ResponseUtil.internalServerErrorJson;
import static com.authlete.jaxrs.server.util.ResponseUtil.notFound;
import static com.authlete.jaxrs.server.util.ResponseUtil.unauthorized;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import org.glassfish.jersey.server.mvc.Viewable;


/**
 * Utility class for exceptions.
 *
 * @author Hideki Ikeda
 */
public class ExceptionUtil
{
    /**
     * Create an exception indicating "400 Bad Request".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "400 Bad Request".
     */
    public static WebApplicationException badRequestException(String entity)
    {
        return new WebApplicationException(entity, badRequest(entity));
    }


    /**
     * Create an exception indicating "400 Bad Request" in application/json format.
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "400 Bad Request".
     */
    public static WebApplicationException badRequestExceptionJson(String entity)
    {
        return badRequestExceptionJson(entity, /* headers */ null);
    }


    public static WebApplicationException badRequestExceptionJson(String entity, Map<String, Object> headers)
    {
        return new WebApplicationException(entity, badRequestJson(entity, headers));
    }


    /**
     * Create an exception indicating "400 Bad Request".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "400 Bad Request".
     */
    public static WebApplicationException badRequestException(Viewable entity)
    {
        return new WebApplicationException(badRequest(entity));
    }


    /**
     * Create an exception indicating "401 Unauthorized".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @param challenge
     *         The value of the "WWW-Authenticate" header of the response of the
     *         exception.
     *
     * @return
     *         An exception indicating "401 Unauthorized".
     */
    public static WebApplicationException unauthorizedException(String entity, String challenge)
    {
        return unauthorizedException(entity, challenge, /* headers */ null);
    }


    public static WebApplicationException unauthorizedException(String entity, String challenge, Map<String, Object> headers)
    {
        return new WebApplicationException(entity, unauthorized(entity, challenge, headers));
    }


    /**
     * Create an exception indicating "401 Unauthorized".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @param challenge
     *         The value of the "WWW-Authenticate" header of the response of the
     *         exception.
     *
     * @return
     *         An exception indicating "401 Unauthorized".
     */
    public static WebApplicationException unauthorizedException(Viewable entity, String challenge)
    {
        return new WebApplicationException(unauthorized(entity, challenge));
    }

    /**
     * Create an exception indicating "403 Forbidden".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "403 Forbidden".
     */
    public static WebApplicationException forbiddenException(final String entity)
    {
        return new WebApplicationException(entity, forbidden(entity));
    }


    /**
     * Create an exception indicating "403 Forbidden" in application/json format.
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "403 Forbidden".
     */
    public static WebApplicationException forbiddenExceptionJson(final String entity)
    {
        return forbiddenExceptionJson(entity, /* headers */ null);
    }


    public static WebApplicationException forbiddenExceptionJson(final String entity, Map<String, Object> headers)
    {
        return new WebApplicationException(entity, forbiddenJson(entity, headers));
    }


    /**
     * Create an exception indicating "404 Not Found".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "404 Not Found".
     */
    public static WebApplicationException notFoundException(String entity)
    {
        return new WebApplicationException(entity, notFound(entity));
    }


    /**
     * Create an exception indicating "404 Not Found".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "404 Not Found".
     */
    public static WebApplicationException notFoundException(Viewable entity)
    {
        return new WebApplicationException(notFound(entity));
    }


    /**
     * Create an exception indicating "500 Internal Server Error".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "500 Internal Server Error".
     */
    public static WebApplicationException internalServerErrorException(String entity)
    {
        return new WebApplicationException(entity, internalServerError(entity));
    }


    /**
     * Create an exception indicating "500 Internal Server Error" in application/json format.
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "500 Internal Server Error".
     */
    public static WebApplicationException internalServerErrorExceptionJson(String entity)
    {
        return internalServerErrorExceptionJson(entity, /* headers */ null);
    }


    public static WebApplicationException internalServerErrorExceptionJson(String entity, Map<String, Object> headers)
    {
        return new WebApplicationException(entity, internalServerErrorJson(entity, headers));
    }


    /**
     * Create an exception indicating "500 Internal Server Error".
     *
     * @param entity
     *         An entity to contain in the response of the exception.
     *
     * @return
     *         An exception indicating "500 Internal Server Error".
     */
    public static WebApplicationException internalServerErrorException(Viewable entity)
    {
        return new WebApplicationException(internalServerError(entity));
    }
}
