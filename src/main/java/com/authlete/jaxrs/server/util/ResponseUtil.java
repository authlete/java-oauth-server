/*
 * Copyright (C) 2019 Authlete, Inc.
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


import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.server.mvc.Viewable;


/**
 * Utility class for responses.
 *
 * @author Hideki Ikeda
 */
public class ResponseUtil
{
    /**
     * {@code "text/html;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_HTML =
            MediaType.TEXT_HTML_TYPE.withCharset("UTF-8");


    /**
     * {@code "text/plain;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_PLAIN =
            MediaType.TEXT_PLAIN_TYPE.withCharset("UTF-8");

    /**
     * {@code "application/json;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_JSON =
            MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8");


    /**
     * Build a "text/plain" response of "200 OK".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         A "text/plain" response of "200 OK".
     */
    public static Response ok(String entity)
    {
        return builderForTextPlain(Status.OK, entity).build();
    }


    /**
     * Build an "application/json" response of "200 OK".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "200 OK".
     */
    public static Response okJson(String entity)
    {
        return builderForJson(Status.OK, entity).build();
    }


    /**
     * Build a "text/html" response of "200 OK".
     *
     * @param entity
     *         A {@link Viewable} entity to contain in the response.
     *
     * @return
     *         A "text/html" response of "200 OK".
     */
    public static Response ok(Viewable entity)
    {
        return builderForTextHtml(Status.OK, entity).build();
    }


    /**
     * Build an "application/json" response of "202 ACCEPTED".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "202 ACCEPTED".
     */
    public static Response acceptedJson(String entity)
    {
        return builderForJson(Status.ACCEPTED, entity).build();
    }


    /**
     * Build a response of "204 No Content".
     *
     * @return
     *         A response of "204 No Content".
     */
    public static Response noContent()
    {
        return Response.noContent().build();
    }


    /**
     * Build a "text/plain" response of "400 Bad Request".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         A "text/plain" response of "400 Bad Request".
     */
    public static Response badRequest(String entity)
    {
        return builderForTextPlain(Status.BAD_REQUEST, entity).build();
    }


    /**
     * Build an "application/json" response of "400 Bad Request".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "400 Bad Request".
     */
    public static Response badRequestJson(String entity)
    {
        return builderForJson(Status.BAD_REQUEST, entity).build();
    }


    /**
     * Build a "text/html" response of "400 Bad Request".
     *
     * @param entity
     *         A {@link Viewable} entity to contain in the response.
     *
     * @return
     *         A "text/html" response of "400 Bad Request".
     */
    public static Response badRequest(Viewable entity)
    {
        return builderForTextHtml(Status.BAD_REQUEST, entity).build();
    }


    /**
     * Build a "text/plain" response of "401 Unauthorized".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @param challenge
     *         The value of the "WWW-Authenticate" header of the response.
     *
     * @return
     *         A "text/plain" response of "401 Unauthorized".
     */
    public static Response unauthorized(String entity, String challenge)
    {
        return builderForTextPlain(Status.UNAUTHORIZED, entity)
                .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
                .build();
    }


    /**
     * Build a "text/html" response of "401 Unauthorized".
     *
     * @param entity
     *         A {@link Viewable} entity to contain in the response.
     *
     * @param challenge
     *         The value of the "WWW-Authenticate" header of the response.
     *
     * @return
     *         A "text/html" response of "401 Unauthorized".
     */
    public static Response unauthorized(Viewable entity, String challenge)
    {
        return builderForTextHtml(Status.UNAUTHORIZED, entity)
                .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
                .build();
    }


    /**
     * Build a "text/plain" response of "403 Forbidden".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "text/plain" response of "403 Forbidde".
     */
    public static Response forbidden(final String entity)
    {
        return builderForTextPlain(Status.FORBIDDEN, entity).build();
    }


    /**
     * Build an "application/json" response of "403 Forbidden".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "403 Forbidde".
     */
    public static Response forbiddenJson(final String entity)
    {
        return builderForJson(Status.FORBIDDEN, entity).build();
    }


    /**
     * Build a "text/plain" response of "404 Not Found".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         A "text/plain" response of "404 Not Found".
     */
    public static Response notFound(String entity)
    {
        return builderForTextPlain(Status.NOT_FOUND, entity).build();
    }


    /**
     * Build an "application/json" response of "404 Not Found".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "404 Not Found".
     */
    public static Response notFoundJson(String entity)
    {
        return builderForJson(Status.NOT_FOUND, entity).build();
    }


    /**
     * Build a "text/html" response of "404 Not Found".
     *
     * @param entity
     *         A {@link Viewable} entity to contain in the response.
     *
     * @return
     *         A "text/html" response of "404 Not Found".
     */
    public static Response notFound(Viewable entity)
    {
        return builderForTextHtml(Status.NOT_FOUND, entity).build();
    }


    /**
     * Build a "text/plain" response of "500 Internal Server Error".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         A "text/plain" response of "500 Internal Server Error".
     */
    public static Response internalServerError(String entity)
    {
        return builderForTextPlain(Status.INTERNAL_SERVER_ERROR, entity).build();
    }


    /**
     * Build a "text/plain" response of "500 Internal Server Error".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         A "text/plain" response of "500 Internal Server Error".
     */
    public static Response internalServerErrorJson(String entity)
    {
        return builderForTextPlain(Status.INTERNAL_SERVER_ERROR, entity).build();
    }


    /**
     * Build a "text/html" response of "500 Internal Server Error".
     *
     * @param entity
     *         A {@link Viewable} entity to contain in the response.
     *
     * @return
     *         A "text/html" response of "500 Internal Server Error".
     */
    public static Response internalServerError(Viewable entity)
    {
        return builderForTextHtml(Status.INTERNAL_SERVER_ERROR, entity).build();
    }


    private static ResponseBuilder builderForTextPlain(Status status, String entity)
    {
        return builder(status, entity, MEDIA_TYPE_PLAIN);
    }


    private static ResponseBuilder builderForTextHtml(Status status, Viewable entity)
    {
        return builder(status, entity, MEDIA_TYPE_HTML);
    }


    private static ResponseBuilder builderForJson(Status status, String entity)
    {
        return builder(status, entity, MEDIA_TYPE_JSON);
    }


    private static ResponseBuilder builder(Status status, Object entity, MediaType type)
    {
        return Response
                .status(status)
                .entity(entity)
                .type(type);
    }
}
