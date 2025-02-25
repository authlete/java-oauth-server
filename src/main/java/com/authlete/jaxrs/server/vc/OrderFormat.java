/*
 * Copyright (C) 2023-2025 Authlete, Inc.
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
package com.authlete.jaxrs.server.vc;


import java.util.Arrays;


/**
 * Order formats.
 *
 * <p>
 * NOTE: The media type of SD-JWT VC has been changed from {@code vc+sd-jwt} to
 * {@code dc+sd-jwt} by <a href="https://github.com/oauth-wg/oauth-sd-jwt-vc/pull/268"
 * >OAuth-SD-JWT-VC PR 268: change media type from vc+sd-jwt to dc+sd-jwt</a>.
 * </p>
 *
 * @see <a href="https://github.com/oauth-wg/oauth-sd-jwt-vc/pull/268"
 *      >OAuth-SD-JWT VC PR 268: change media type from vc+sd-jwt to dc+sd-jwt</a>
 *
 * @see <a href="https://datatracker.ietf.org/meeting/121/materials/slides-121-oauth-sessb-sd-jwt-and-sd-jwt-vc-02#page=51"
 *      >IETF 121 Dublin, SD-JWT/SD-JWT VC, Page 51</a>
 */
public enum OrderFormat
{
    DC_SD_JWT("dc+sd-jwt", new SdJwtOrderProcessor()),
    VC_SD_JWT("vc+sd-jwt", new SdJwtOrderProcessor()),
    MDOC("mso_mdoc", new MdocOrderProcessor()),
    ;


    private final String id;
    private final OrderProcessor processor;


    private OrderFormat(String id, OrderProcessor processor)
    {
        this.id        = id;
        this.processor = processor;
    }


    public String getId()
    {
        return id;
    }


    public OrderProcessor getProcessor()
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
