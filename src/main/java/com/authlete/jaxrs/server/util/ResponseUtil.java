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


import java.util.Map;
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
     * {@code "application/jwt"}
     */
    private static final MediaType MEDIA_TYPE_JWT =
            new MediaType("application", "jwt");



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
        return ok(entity, /* headers */ null);
    }


    public static Response ok(String entity, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.OK, entity, headers).build();
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
        return okJson(entity, /* headers */ null);
    }


    public static Response okJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.OK, entity, headers).build();
    }


    public static Response okJwt(String entity, Map<String, Object> headers)
    {
        return builderForJwt(Status.OK, entity, headers).build();
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
        return ok(entity, /* headers */ null);
    }


    public static Response ok(Viewable entity, Map<String, Object> headers)
    {
        return builderForTextHtml(Status.OK, entity, headers).build();
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
        return acceptedJson(entity, /* headers */ null);
    }


    public static Response acceptedJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.ACCEPTED, entity, headers).build();
    }


    public static Response acceptedJwt(String entity, Map<String, Object> headers)
    {
        return builderForJwt(Status.ACCEPTED, entity, headers).build();
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
        return badRequest(entity, /* headers */ null);
    }


    public static Response badRequest(String entity, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.BAD_REQUEST, entity, headers).build();
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
        return badRequestJson(entity, /* headers */ null);
    }


    public static Response badRequestJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.BAD_REQUEST, entity, headers).build();
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
        return badRequest(entity, /* headers */ null);
    }


    public static Response badRequest(Viewable entity, Map<String, Object> headers)
    {
        return builderForTextHtml(Status.BAD_REQUEST, entity, headers).build();
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
        return unauthorized(entity, challenge, /* headers */ null);
    }


    public static Response unauthorized(
            String entity, String challenge, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.UNAUTHORIZED, entity, headers)
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
        return unauthorized(entity, challenge, /* headers */ null);
    }


    public static Response unauthorized(
            Viewable entity, String challenge, Map<String, Object> headers)
    {
        return builderForTextHtml(Status.UNAUTHORIZED, entity, headers)
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
     *         An "text/plain" response of "403 Forbidden".
     */
    public static Response forbidden(String entity)
    {
        return forbidden(entity, /* headers */ null);
    }


    public static Response forbidden(String entity, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.FORBIDDEN, entity, headers).build();
    }


    /**
     * Build an "application/json" response of "403 Forbidden".
     *
     * @param entity
     *         A string entity to contain in the response.
     *
     * @return
     *         An "application/json" response of "403 Forbidden".
     */
    public static Response forbiddenJson(String entity)
    {
        return forbiddenJson(entity, /* headers */ null);
    }


    public static Response forbiddenJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.FORBIDDEN, entity, headers).build();
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
        return notFound(entity, /* headers */ null);
    }


    public static Response notFound(String entity, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.NOT_FOUND, entity, headers).build();
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
        return notFoundJson(entity, /* headers */ null);
    }


    public static Response notFoundJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.NOT_FOUND, entity, headers).build();
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
        return notFound(entity, /* headers */ null);
    }


    public static Response notFound(Viewable entity, Map<String, Object> headers)
    {
        return builderForTextHtml(Status.NOT_FOUND, entity, headers).build();
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
        return internalServerError(entity, /* headers */ null);
    }


    public static Response internalServerError(String entity, Map<String, Object> headers)
    {
        return builderForTextPlain(Status.INTERNAL_SERVER_ERROR, entity, headers).build();
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
        return internalServerErrorJson(entity, /* headers */ null);
    }


    public static Response internalServerErrorJson(String entity, Map<String, Object> headers)
    {
        return builderForJson(Status.INTERNAL_SERVER_ERROR, entity, headers).build();
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
        return internalServerError(entity, /* headers */ null);
    }


    public static Response internalServerError(Viewable entity, Map<String, Object> headers)
    {
        return builderForTextHtml(Status.INTERNAL_SERVER_ERROR, entity, headers).build();
    }


    private static ResponseBuilder builderForTextPlain(
            Status status, String entity, Map<String, Object> headers)
    {
        return builder(status, entity, MEDIA_TYPE_PLAIN, headers);
    }


    private static ResponseBuilder builderForTextHtml(
            Status status, Viewable entity, Map<String, Object> headers)
    {
        return builder(status, entity, MEDIA_TYPE_HTML, headers);
    }


    private static ResponseBuilder builderForJson(
            Status status, String entity, Map<String, Object> headers)
    {
        return builder(status, entity, MEDIA_TYPE_JSON, headers);
    }


    private static ResponseBuilder builderForJwt(
            Status status, String entity, Map<String, Object> headers)
    {
        return builder(status, entity, MEDIA_TYPE_JWT, headers);
    }


    private static ResponseBuilder builder(
            Status status, Object entity, MediaType type, Map<String, Object> headers)
    {
        ResponseBuilder builder = Response
                .status(status)
                .entity(entity)
                .type(type);

        // If additional headers are given.
        if (headers != null)
        {
            // For each additional header.
            for (Map.Entry<String, Object> header : headers.entrySet())
            {
                // Add the header.
                builder.header(header.getKey(), header.getValue());
            }
        }

        return builder;
    }
}
