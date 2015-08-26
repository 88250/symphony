/*
 * Copyright (c) 2012-2015, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.util;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.json.JSONObject;


/**
 * Session utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.1, Aug 26, 2015
 */
public final class Sessions {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Sessions.class.getName());

    /**
     * Cookie expiry: one year.
     */
    private static final int COOKIE_EXPIRY = 60 * 60 * 24 * 365;

    /**
     * Private default constructor.
     */
    private Sessions() {}

    /**
     * Logins the specified user from the specified request.
     * 
     * <p>
     * If no session of the specified request, do nothing.
     * </p>
     *
     * @param request the specified request
     * @param response the specified response
     * @param user the specified user, for example,
     * <pre>
     * {
     *     "userEmail": "",
     *     "userPassword": ""
     * }
     * </pre>
     */
    public static void login(final HttpServletRequest request, final HttpServletResponse response, final JSONObject user) {
        final HttpSession session = request.getSession(false);

        if (null == session) {
            LOGGER.warn("The session is null");
            return;
        }

        session.setAttribute(User.USER, user);

        try {
            final JSONObject cookieJSONObject = new JSONObject();

            cookieJSONObject.put(User.USER_EMAIL, user.optString(User.USER_EMAIL));
            cookieJSONObject.put(User.USER_PASSWORD, user.optString(User.USER_PASSWORD));

            final Cookie cookie = new Cookie("b3log-latke", cookieJSONObject.toString());

            cookie.setPath("/");
            cookie.setMaxAge(COOKIE_EXPIRY);
            cookie.setHttpOnly(true); // HTTP Only
            
            response.addCookie(cookie);
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Can not write cookie", e);
        }
    }

    /**
     * Logouts a user with the specified request.
     *
     * @param request the specified request
     * @param response the specified response
     * @return {@code true} if succeed, otherwise returns {@code false}
     */
    public static boolean logout(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final Cookie cookie = new Cookie("b3log-latke", null);

            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);

            session.invalidate();

            return true;
        }

        return false;
    }

    /**
     * Gets the current user with the specified request.
     * 
     * @param request the specified request
     * @return the current user, returns {@code null} if not logged in 
     */
    public static JSONObject currentUser(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            return (JSONObject) session.getAttribute(User.USER);
        }

        return null;
    }

    /**
     * Gets the current logged in user password with the specified request.
     *
     * @param request the specified request
     * @return the current user password or {@code null}
     */
    public static String currentUserPwd(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_PASSWORD);
        }

        return null;
    }

    /**
     * Gets the current logged in user name with the specified request.
     *
     * @param request the specified request
     * @return the current user name or {@code null}
     */
    public static String currentUserName(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_NAME);
        }

        return null;
    }

    /**
     * Gets the current logged in user email with the specified request.
     *
     * @param request the specified request
     * @return the current user name or {@code null}
     */
    public static String currentUserEmail(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);

        if (null != session) {
            final JSONObject user = (JSONObject) session.getAttribute(User.USER);

            return user.optString(User.USER_EMAIL);
        }

        return null;
    }
}
