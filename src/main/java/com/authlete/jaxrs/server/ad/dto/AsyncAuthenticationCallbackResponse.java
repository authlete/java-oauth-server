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
package com.authlete.jaxrs.server.ad.dto;


import java.io.Serializable;


/**
 * A class for a response to a callback request that Authlete's CIBA authentication
 * device simulator makes when it is used in asynchronous mode.
 *
 * @author Hideki Ikeda
 */
public class AsyncAuthenticationCallbackResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
}
