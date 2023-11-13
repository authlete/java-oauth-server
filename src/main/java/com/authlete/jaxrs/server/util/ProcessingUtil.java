package com.authlete.jaxrs.server.util;


import static com.authlete.jaxrs.server.util.ExceptionUtil.badRequestException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import com.authlete.common.types.User;
import com.authlete.jaxrs.server.db.UserDao;


public class ProcessingUtil
{

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> flattenMultivaluedMap(final MultivaluedMap<K, V> multimap)
    {
        return multimap.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> new Object[]{e.getKey(), e.getValue().get(0)})
                .collect(Collectors.toMap(e -> (K)e[0], e -> (V)e[1]));
    }


    public static <K> boolean fromFormCheckbox(final Map<K, String> map, final K key)
    {
        return "on".equals(map.getOrDefault(key, "off"));
    }


    /**
     * Get the existing session.
     */
    public static HttpSession getSession(HttpServletRequest request)
    {
        // Get the existing session.
        HttpSession session = request.getSession(false);

        // If there exists a session.
        if (session != null)
        {
            // OK.
            return session;
        }

        // A session does not exist. Make a response of "400 Bad Request".
        throw badRequestException("A session does not exist.");
    }


    /**
     * Look up an end-user.
     */
    public static User getUser(HttpSession session, MultivaluedMap<String, String> parameters)
    {
        // Look up the user in the session to see if they're already logged in.
        User sessionUser = (User) session.getAttribute("user");

        if (sessionUser != null)
        {
            return sessionUser;
        }

        // Look up an end-user who has the login credentials.
        User loginUser = UserDao.getByCredentials(parameters.getFirst("loginId"),
                                                  parameters.getFirst("password"));

        if (loginUser != null)
        {
            session.setAttribute("user", loginUser);
            session.setAttribute("authTime", new Date());
        }

        return loginUser;
    }

}
