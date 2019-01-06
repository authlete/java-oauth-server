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
package com.authlete.jaxrs.server.ad.type;


/**
 * The status of end-user authentication and authorization on Authlete's CIBA
 * authentication device when it is used in poll mode.
 *
 * @author Hideki Ikeda
 *
 */
public enum Status
{
    /**
     * The status showing that end-user authentication and authorization is being
     * processed.
     */
    active((short)1, "active"),


    /**
     * The status showing that end-user authentication and authorization process
     * has completed.
     */
    complete((short)2, "complete"),


    /**
     * The status showing that timeout occurred during end-user authentication
     * and authorization process
     */
    timeout((short)3, "timeout")
    ;


    private final short mValue;
    private final String mString;


    private Status(short value, String string)
    {
        mValue = value;
        mString = string;
    }


    /**
     * Get the value of the status.
     *
     * @return
     *         The value of the status.
     */
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
