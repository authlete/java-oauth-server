package com.authlete.jaxrs.server.core;


import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Session Tracker to track active session IDs.
 *
 * <p>
 * This class is designed to check whether a session corresponding to a given
 * session ID exists.
 * </p>
 *
 * <p>
 * To support the "<a href=
 * "https://openid.net/specs/openid-connect-native-sso-1_0.html">OpenID Connect
 * Native SSO for Mobile Apps 1.0</a>" specification (a.k.a. "Native SSO"), it
 * is necessary for the token endpoint implementation to verify whether the
 * session ID associated with a presented refresh token or subject token
 * exists. For this purpose, the {@link #isActiveSessionId(String)} method is
 * used.
 * </p>
 *
 * <p>
 * When a token request compliant with Native SSO is processed by Authlete's
 * {@code /auth/token} API, the {@code action} field in the API response will
 * be {@link com.authlete.common.dto.TokenResponse.Action#NATIVE_SSO NATIVE_SSO},
 * and the session ID corresponding to the refresh token or subject token will
 * be included as the value of the {@code sessionId} parameter. This value
 * should be passed to the {@link #isActiveSessionId(String)} method to
 * determine whether the session ID is still active.
 * </p>
 *
 * <p>
 * Support for the Native SSO specification was introduced in Authlete 3.0.
 * </p>
 *
 * @see <a href="https://openid.net/specs/openid-connect-native-sso-1_0.html"
 *      >OpenID Connect Native SSO for Mobile Apps 1.0</a>
 */
@WebListener
public class SessionTracker implements HttpSessionListener
{
    private static final Set<String> activeSessionIds = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(SessionTracker.class);


    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        // The session ID.
        String sessionId = retrieveSessionId(se);

        logger.debug("A session with the session ID '{}' was created.", sessionId);

        // Add the session ID to the list of active session IDs.
        activeSessionIds.add(sessionId);
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        // The session ID.
        String sessionId = retrieveSessionId(se);

        logger.debug("The session with the session ID '{}' was destroyed.", sessionId);

        // Remove the session ID from the list of active session IDs.
        activeSessionIds.remove(sessionId);
    }


    private static String retrieveSessionId(HttpSessionEvent se)
    {
        return se.getSession().getId();
    }


    /**
     * Check whether the session corresponding to the specified session ID is
     * active.
     *
     * @param sessionId
     *         A session ID.
     *
     * @return
     *         {@code true} if the session corresponding to the specified
     *         session ID is active.
     */
    public static boolean isActiveSessionId(String sessionId)
    {
        if (sessionId == null)
        {
            return false;
        }

        // Whether the session with the specified session ID is active.
        boolean active = activeSessionIds.contains(sessionId);

        logger.debug("The session with the session ID '{}' is {}active.", sessionId, active ? "" : "not ");

        return active;
    }
}
