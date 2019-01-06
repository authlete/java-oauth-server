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
 * A class representing a request to Authlete's CIBA authentication device simulator's
 * {@code /api/authenticate/poll} API.
 *
 * @author Hideki Ikeda
 */
public class PollAuthenticationRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
}
