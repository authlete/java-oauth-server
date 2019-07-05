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
package com.authlete.jaxrs.server.api.device;


import static com.authlete.jaxrs.server.util.ResponseUtil.badRequest;
import static com.authlete.jaxrs.server.util.ResponseUtil.internalServerError;
import static com.authlete.jaxrs.server.util.ResponseUtil.ok;
import java.util.Date;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import com.authlete.common.dto.DeviceCompleteRequest.Result;
import com.authlete.common.types.User;
import com.authlete.jaxrs.spi.DeviceCompleteRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jaxrs.spi.DeviceCompleteRequestHandlerSpi
 * DeviceCompleteRequestHandlerSpi} interface which needs to be given to the constructor
 * of {@link com.authlete.jaxrs.DeviceCompleteRequestHandler DeviceCompleteRequestHandler}.
 *
 * @author Hideki Ikeda
 */
public class DeviceCompleteRequestHandlerSpiImpl extends DeviceCompleteRequestHandlerSpiAdapter
{
    /**
     * The result of end-user authentication and authorization.
     */
    private final Result mResult;


    /**
     * The authenticated user.
     */
    private User mUser;


    /**
     * The time when the user was authenticated in seconds since Unix epoch.
     */
    private long mUserAuthenticatedAt;


    /**
     * Requested ACRs.
     */
    private String[] mAcrs;


    public DeviceCompleteRequestHandlerSpiImpl(
            MultivaluedMap<String, String> parameters, User user, Date userAuthenticatedAt,
            String[] acrs)
    {
        // Check the result of end-user authentication and authorization.
        mResult = parameters.containsKey("authorized") ? Result.AUTHORIZED : Result.ACCESS_DENIED;

        if (mResult != Result.AUTHORIZED)
        {
            // The end-user has not authorized the client.
            return;
        }

        // OK. The end-user has successfully authorized the client.

        // The end-user.
        mUser = user;

        // The time at which end-user has been authenticated.
        mUserAuthenticatedAt = (userAuthenticatedAt == null) ? 0 : userAuthenticatedAt.getTime() / 1000L;

        // The requested ACRs.
        mAcrs = acrs;
    }


    @Override
    public Result getResult()
    {
        return mResult;
    }


    @Override
    public String getUserSubject()
    {
        return mUser.getSubject();
    }


    @Override
    public long getUserAuthenticatedAt()
    {
        return mUserAuthenticatedAt;
    }


    @Override
    public String getAcr()
    {
        // Note that this is a dummy implementation. Regardless of whatever
        // the actual authentication was, this implementation returns the
        // first element of the requested ACRs if it is available.
        //
        // Of course, this implementation is not suitable for commercial use.

        if (mAcrs == null || mAcrs.length == 0)
        {
            return null;
        }

        // The first element of the requested ACRs.
        String acr = mAcrs[0];

        if (acr == null || acr.length() == 0)
        {
            return null;
        }

        // Return the first element of the requested ACRs. Again,
        // this implementation is not suitable for commercial use.
        return acr;
    }


    @Override
    public Object getUserClaim(String claimName)
    {
        return mUser.getClaim(claimName, null);
    }


    @Override
    public Response onSuccess()
    {
        // The user has authorized or denied the client.
        // Return a response of "200 OK".
        return ok("OK. The user authorization process has been done.");
    }


    @Override
    public Response onInvalidRequest()
    {
        // The API call to Authlete was invalid. There should be some bugs in the
        // implementation of this authorization sever.
        // Return a response of "500 Internal Server Error".
        return internalServerError("Server Error. Please re-initiate the flow again.");
    }


    @Override
    public Response onUserCodeExpired()
    {
        // The user code has already expired.
        // Return a response of "400 Bad Request".
        return badRequest("The user code has expired. Please re-initiate the flow again.");
    }


    @Override
    public Response onUserCodeNotExist()
    {
        // The user code does not exist (= invalidated).
        // Return a response of "400 Bad Request".
        return badRequest("The user code has been invalidated. Please re-initiate the flow again.");
    }


    @Override
    public Response onServerError()
    {
        // An error has occurred on Authlete.
        // Return a response of "500 Internal Server Error".
        return internalServerError("Server Error. Please re-initiate the flow again.");
    }
}
