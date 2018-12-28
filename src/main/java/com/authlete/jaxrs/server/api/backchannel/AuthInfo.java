package com.authlete.jaxrs.server.api.backchannel;


import com.authlete.common.types.User;


public class AuthInfo
{
    String mTicket;
    User mUser;
    String[] mClaimNames;
    String[] mAcrs;


    public AuthInfo(String ticket, User user, String[] claimNames, String[] acrs)
    {
        mTicket     = ticket;
        mUser       = user;
        mClaimNames = claimNames;
        mAcrs       = acrs;
    }


    public String getTicket()
    {
        return mTicket;
    }


    public User getUser()
    {
        return mUser;
    }


    public String[] getClaimNames()
    {
        return mClaimNames;
    }


    public String[] getAcrs()
    {
        return mAcrs;
    }
}
