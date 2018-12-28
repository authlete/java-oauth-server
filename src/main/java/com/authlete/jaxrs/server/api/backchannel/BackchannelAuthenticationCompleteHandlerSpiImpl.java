package com.authlete.jaxrs.server.api.backchannel;


import java.util.Date;
import com.authlete.common.dto.BackchannelAuthenticationCompleteRequest.Result;
import com.authlete.common.types.User;
import com.authlete.jaxrs.spi.BackchannelAuthenticationCompleteHandlerSpiAdapter;


public class BackchannelAuthenticationCompleteHandlerSpiImpl extends BackchannelAuthenticationCompleteHandlerSpiAdapter
{
    /**
     * The result of end-user authentication and authorization.
     */
    private final Result mResult;


    /**
     * The authenticated user.
     */
    private final User mUser;


    /**
     * The time when the user was authenticated in seconds since Unix epoch.
     */
    private long mUserAuthenticatedAt;


    /**
     * Requested ACRs.
     */
    private String[] mAcrs;


    public BackchannelAuthenticationCompleteHandlerSpiImpl(Result result, User user, Date userAuthenticatedAt, String[] acrs)
    {
        // The result of end-user authentication and authorization.
        mResult = result;

        // The end-user.
        mUser = user;

        if (result != Result.AUTHORIZED)
        {
            // The end-user has not authorized the client.
            return;
        }

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
}
