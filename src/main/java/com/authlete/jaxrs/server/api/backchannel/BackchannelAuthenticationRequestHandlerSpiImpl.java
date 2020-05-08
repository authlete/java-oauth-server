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


import java.util.concurrent.Executors;
import javax.ws.rs.WebApplicationException;
import com.authlete.common.dto.BackchannelAuthenticationIssueResponse;
import com.authlete.common.dto.BackchannelAuthenticationResponse;
import com.authlete.common.dto.Scope;
import com.authlete.common.types.User;
import com.authlete.common.types.UserIdentificationHintType;
import com.authlete.jaxrs.server.ServerConfig;
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
        if (hintType == null)
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


    private User getUserByLoginHint(String hint)
    {
        // Find a user using the login hint. A login hint is a value which identifies
        // the end-user. In this implementation, we're assuming subject, email
        // address and phone number can be a login hint.

        // First, find a user assuming the login hint value is a subject.
        User user = UserDao.getBySubject(hint);

        if (user != null)
        {
            // OK. Found a user.
            return user;
        }

        // Second, find a user assuming the login hint value is an email address.
        user = UserDao.getByEmail(hint);

        if (user != null)
        {
            // OK. Found a user.
            return user;
        }

        // Lastly, find a user assuming the login hint value is a phone number.
        return UserDao.getByPhoneNumber(hint);
    }


    private User getUserByLoginHintToken(String hint)
    {
        // This implementation doesn't use login hint token.
        return null;
    }


    private User getUserByIdTokenHint(String hint, String sub)
    {
        // The value of 'sub' parameter is the value of 'sub' claim contained in
        // the ID token that was included in the authentication request as an
        // 'id_token_hint' request parameter. In this implementation, we only use
        // the value of 'sub' parameter to find a user but you may use the value
        // of 'hint' parameter (, which is equivalent to the value of the payload
        // of the 'id_token_hint' request parameter).
        return UserDao.getBySubject(sub);
    }


    @Override
    public boolean isLoginHintTokenExpired(String loginHintToken)
    {
        // This implementation doesn't use login hint token.
        return false;
    }


    @Override
    public boolean shouldCheckUserCode(User user, BackchannelAuthenticationResponse info)
    {
        // This implementation checks a user code only when the value of "userCodeRequired"
        // parameter is true (i.e. both the "backchannel_user_code_parameter" metadata
        // of the client (= Client's "bcUserCodeRequired" property) and the
        // "backchannel_user_code_parameter_supported" metadata of the service
        // (= Service's "backchannelUserCodeParameterSupported" property) are
        // true). However, you may require a user code in some particular cases
        // even if the value of the "userCodeRequired" parameter is false.
        return info.isUserCodeRequired();
    }


    @Override
    public boolean isValidUserCode(User user, String userCode)
    {
        // The actual code of the user.
        String uc = (String)user.getAttribute("code");

        if (uc == null || uc.length() == 0)
        {
            // The user does not have a code.
            return false;
        }

        return uc.equals(userCode);
    }


    @Override
    public boolean isValidBindingMessage(String bindingMessage)
    {
        // In this implementation, any value is regarded as a valid binding message.
        // You may add additional checks here according to the following excerpt
        // from the specification.
        //
        //   CIBA Core spec, 7.1. Authentication Request
        //     binding_message
        //       ...
        //       The value SHOULD contain something that enables the end-user to
        //       reliably discern that the transaction is related across the consumption
        //       device and the authentication device, such as a random value of
        //       reasonable entropy (e.g. a transactional approval code). Because
        //       the various devices involved may have limited display abilities
        //       and the message is intending for visual inspection by the end-user,
        //       the binding_message value SHOULD be relatively short and use a
        //       limited set of plain text characters.
        //
        return true;
    }


    @Override
    public void startCommunicationWithAuthenticationDevice(User user, BackchannelAuthenticationResponse baRes,
            BackchannelAuthenticationIssueResponse baiRes)
    {
        // Ensure that the authorization server has not started communicating with
        // the authentication device yet so that the following authentication/authorization
        // process will never be performed more than once.
        synchronized (this)
        {
            if (communicationWithAuthenticationDeviceStarted)
            {
                // The communication with authentication device has already started.
                return;
            }

            communicationWithAuthenticationDeviceStarted = true;
        }

        // To process the end-user authentication and authorization, we use Authlete
        // authentication device simulator (https://cibasim.authlete.com) as an
        // authentication device (AD) here.
        // According to API documents (https://app.swaggerhub.com/apis-docs/Authlete/cibasim),
        // the simulator has three communication modes as follows.
        //
        //   1. synchronous mode
        //   2. asynchronous mode
        //   3. poll mode
        //
        // For example, in synchronous mode, the authorization server ask the AD
        // to authenticate the user and get authorization from the user by sending
        // a HTTP request and wait to get the HTTP response that contains an authentication
        // and authorization result. These are processed by SyncAuthenticationDeviceProcessor.
        // We also have other types of processors for other modes. For more details,
        // see 'com.authlete.jaxrs.server.api.backchannel.XxxProcessor'.

        // The ticket to call Authlete's /api/backchannel/authentication/complete
        // API after processing end-user authentication and authorization.
        String ticket = baRes.getTicket();

        // The name of the client.
        String clientName = baRes.getClientName();

        // The acr values requested by the client.
        String[] acrs = baRes.getAcrs();

        // The scopes requested by the client.
        Scope[] scopes = baRes.getScopes();

        // The claims requested by the client.
        String[] claimNames = baRes.getClaimNames();

        // The biding message to be shown to the user on authentication.
        String bindingMessage = baRes.getBindingMessage();

        // The auth_req_id issued to the client. This is used to programmatically
        // complete the authentication on the authentication device.
        String authReqId = baiRes.getAuthReqId();

        // The duration of the issued auth_req_id in seconds.
        int expiresIn = baiRes.getExpiresIn();

        // The mode in which this authorization server communicates with the
        // authentication device.
        Mode mode = ServerConfig.getAuthleteAdMode();

        // Get a processor to process end-user authentication and authorization
        // by communicating with the authentication device.
        AuthenticationDeviceProcessor processor = AuthenticationDeviceProcessorFactory.create(
                mode, ticket, user, clientName, acrs, scopes, claimNames, bindingMessage, authReqId, expiresIn);

        // Start executing the process in the background.
        Executors.newSingleThreadExecutor().execute(new AuthTask(processor));
    }


    /**
     * A class representing a task in which end-user authentication and authorization
     * is performed by communicating with the authentication device.
     */
    private static class AuthTask implements Runnable
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
            }
            catch (Throwable t)
            {
                // Do something.
            }
        }
    }
}
