package com.authlete.jaxrs.server.api.backchannel;


import java.util.concurrent.Executors;
import javax.ws.rs.WebApplicationException;
import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.common.types.UserIdentificationHintType;
import com.authlete.jaxrs.server.ad.type.Mode;
import com.authlete.jaxrs.server.db.UserDao;
import com.authlete.jaxrs.spi.BackchannelAuthenticationRequestHandlerSpiAdapter;


public class BackchannelAuthenticationRequestHandlerSpiImpl extends BackchannelAuthenticationRequestHandlerSpiAdapter
{
    /**
     * The flag to show whether communication with the authentication device has
     * started or not.
     */
    private boolean communicationWithAuthenticationDeviceStarted = false;


    @Override
    public User getUserByHint(UserIdentificationHintType hintType, String hint, String sub)
    {
        // TODO
        if (hintType == null || hint == null || hint.length() == 0)
        {
            // This won't happen.
            return null;
        }

        switch (hintType)
        {
            case LOGIN_HINT:
                // Get a user with the login hint.
                return getUserByLoginHint(hint);

            case LOGIN_HINT_TOKEN:
                // Get a user with the login hint token.
                return getUserByLoginHintToken(hint);

            case ID_TOKEN_HINT:
                // Get a user with the ID token hint.
                return getUserByIdTokenHint(hint, sub);

            default:
                // Unknown hint type. This never happens.
                return null;
        }
    }


    // TODO: Should we pre-determine which value could be a hint?
    // TODO: We need to know which value the CD will send as a login hint token.
    private User getUserByLoginHint(String hint)
    {
        // First, find a user assuming the value of the login hint is a subject.
        User user = UserDao.getBySubject(hint);

        if (user != null)
        {
            // OK. Found a user.
            return user;
        }

        // Second, find a user assuming the value of the login hint is an email address.
        user = UserDao.getByEmail(hint);

        if (user != null)
        {
            // OK. Found a user.
            return user;
        }

        // Lastly, find a user assuming the value of the login hint is a phone number.
        return UserDao.getByPhoneNumber(hint);
    }


    private User getUserByLoginHintToken(String hint)
    {
        // TODO: Implement this.
        return null;
    }


    private User getUserByIdTokenHint(String hint, String sub)
    {
        // The value of 'sub' parameter is the value of 'sub' claim contained in
        // the ID token that was included in the authentication request as an
        // 'id_token_hint' request parameter. In this dummy implementation, we
        // only use this value to find a user but you may use the value of 'hint'
        // parameter (, which is equivalent to the value of the payload of the ID
        // token).
        return UserDao.getBySubject(sub);
    }


    @Override
    public boolean isLoginHintTokenExpired(String loginHintToken)
    {
        if (loginHintToken == null || loginHintToken.length() == 0)
        {
            // This won't happen.
            return false;
        }

        // TODO: Implement this.
        return false;
    }


    @Override
    public String getUserCode(User user)
    {
        return null; //user.getCode();
    }


    @Override
    public void startCommunicationWithAuthenticationDevice(
            User user, String ticket, String[] requestedAcrs, Scope[] requestedScopes,
            String[] requestedClaimNames, String bindingMessage, String[] warnings)
    {
        // Ensure that the communication with the authentication device has not
        // started yet so that the following authentication/authorization process
        // will never be performed more than once.
        synchronized (this)
        {
            if (communicationWithAuthenticationDeviceStarted)
            {
                // The communication with authentication device has already started.
                return;
            }

            communicationWithAuthenticationDeviceStarted = true;
        }

        // To process user authentication, we use our own authentication device
        // (AD) here. According the specification of the AD, there are three modes
        // of communication with the AD as follows.
        //
        //   1. synchronous mode
        //   2. asynchronous mode
        //   3. poll mode
        //
        // For example, in synchronous mode, we ask the AD to authenticate the
        // user and get authorization from the user by sending a HTTP request and
        // wait to get the HTTP response that contains the authentication and
        // authorization result. These are processed by SyncAuthenticationDeviceProcessor.
        // There are also other processors for the other modes. For more details,
        // see 'com.authlete.jaxrs.server.api.XxxProcessor'.

        // The mode in which this authorization server communicates with the
        // authentication device.
        Mode mode = getAuthenticationDeviceMode();

        // Debug code.
        log(mode, ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage, warnings);

        // Get a processor to process end-user authentication and authorization
        // by communicating with the authentication device.
        AuthenticationDeviceProcessor processor =
                AuthenticationDeviceProcessorFactory.create(mode, ticket, user, requestedAcrs, requestedScopes, requestedClaimNames, bindingMessage);

        // Start executing the process in the background.
        Executors.newSingleThreadExecutor().execute(new AuthTask(processor));
    }


    private void log(Mode mode, String ticket, User user, String[] requestedAcrs, Scope[] requestedScopes, String[] requestedClaimNames, String bindingMessage, String[] warnings)
    {
        System.out.println("user: " + user.getSubject());
        System.out.println("ticket: " + ticket);
        if (requestedAcrs != null) { System.out.println("requestedAcrs[0]: " + requestedAcrs[0]); }
        if (requestedScopes != null) { System.out.println("requestedScopes[0]: " + requestedScopes[0]); }
        if (requestedClaimNames != null) { System.out.println("requestedClaimNames[0]: " + requestedClaimNames[0]); }
        System.out.println("bindingMessage: " + bindingMessage);
        if (warnings != null) { System.out.println("warnings[0]: " + warnings[0]); }
    }


    private Mode getAuthenticationDeviceMode()
    {
        // In this dummy implementation, we always use SYNC mode when communicating
        // with the authentication device but you may change this behavior as you
        // like (e.g. which mode you use may vary depending on the ACRs values).
        return Mode.ASYNC;
        //return Mode.SYNC;
    }


    /**
     * A class representing a task in which end-user authentication and authorization
     * is performed by communicating with the authentication device.
     */
    private class AuthTask implements Runnable
    {
        private final AuthenticationDeviceProcessor mProcessor;


        private AuthTask(AuthenticationDeviceProcessor processor)
        {
            mProcessor = processor;
        }


        @Override
        public void run()
        {
            try
            {
                // Execute the processor.
                mProcessor.process();
            }
            catch (WebApplicationException e)
            {
                // Do something.
                e.printStackTrace();
            }
            catch (Throwable t)
            {
                // Do something.
                t.printStackTrace();
            }

            System.out.println("background process is done.");
        }
    }
}
