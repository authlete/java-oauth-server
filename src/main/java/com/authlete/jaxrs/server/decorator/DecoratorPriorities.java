/*
 * Copyright (C) 2026 Authlete, Inc.
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
package com.authlete.jaxrs.server.decorator;


/**
 * Filter and interceptor priorities that are used as a parameter of
 * the {@link jakarta.annotation.Priority Priority} annotation.
 *
 * @see jakarta.annotation.Priority
 * @see jakarta.ws.rs.Priorities
 */
public class DecoratorPriorities
{
    /*
     * Priorities for ContainerResponseFilter implementations.
     *
     * <p>
     * The smaller the priority, the later the filter is executed.
     * </p>
     */
    public static final int FAPI_INTERACTION_ID_RESPONSE_FILTER = 40200;
}
