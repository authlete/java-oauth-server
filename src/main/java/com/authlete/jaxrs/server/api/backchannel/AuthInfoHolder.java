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
package com.authlete.jaxrs.server.api.backchannel;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The holder storing {@link AuthInfo information} required to complete processes
 * that are executed in {@link AsyncAuthenticationDeviceProcessor}. The information
 * is expected to be stored in the {@link AsyncAuthenticationDeviceProcessor#process()
 * process()} method of {@link AsyncAuthenticationDeviceProcessor} and retrieved
 * in {@link BackchannelAuthenticationCallbackEndpoint} to complete the processes.
 *
 * <p>
 * Note that this implementation is a dummy implementation and not suitable for
 * commercial use.
 * </p>
 *
 * @see AuthInfo
 *
 * @see AsyncAuthenticationDeviceProcessor
 *
 * @see BackchannelAuthenticationCallbackEndpoint
 *
 * @author Hideki Ikeda
 */
public class AuthInfoHolder
{
    private static final Map<String, AuthInfo> sHolder = new ConcurrentHashMap<String, AuthInfo>();


    /**
     * Get the information by the request ID.
     *
     * @param requestId
     *         The request ID.
     *
     * @return
     *         The information associated with the request ID.
     */
    public static AuthInfo get(String requestId)
    {
        return sHolder.get(requestId);
    }


    /**
     * Associate information with a request ID.
     *
     * @param requestId
     *         A request ID with which the specified information is to be associated.
     *
     * @param info
     *         Information to be associated with the specified request ID
     *
     * @return
     *         The previous value associated with the specified request ID, or
     *         {@code null} if there was no mapping for the request ID.
     */
    public static AuthInfo put(String requestId, AuthInfo info)
    {
        return sHolder.put(requestId, info);
    }


    /**
     * Remove information for a request ID.
     *
     * @param requestId
     *         A request ID whose information is to be removed.
     *
     * @return
     *         The previous value associated with the request ID, or {@code null}
     *         if there was no mapping for the request ID.
     */
    public static AuthInfo remove(String requestId)
    {
        return sHolder.remove(requestId);
    }
}
