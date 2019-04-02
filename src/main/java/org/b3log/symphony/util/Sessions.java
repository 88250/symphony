/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.util.Crypts;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.UserMgmtService;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Session utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.1.1.0, Jan 22, 2019
 */
public final class Sessions {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Sessions.class);

    /**
     * Session cache.
     */
    private static final Cache SESSION_CACHE = CacheFactory.getCache("sessions");

    /**
     * Cookie name.
     */
    public static final String COOKIE_NAME = "sym-ce";

    /**
     * Cookie value separator.
     */
    public static final String COOKIE_ITEM_SEPARATOR = ":";

    /**
     * Cookie expiry: 7 days.
     */
    private static final int COOKIE_EXPIRY = 60 * 60 * 24 * 7;

    /**
     * Thread local data
     */
    private static final ThreadLocal<JSONObject> THREAD_LOCAL_DATA = new ThreadLocal<>();

    /**
     * Checks whether is bot.
     *
     * @return {@code true} if it is bot, returns {@code false} otherwise
     */
    public static boolean isBot() {
        final JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return false;
        }

        return data.optBoolean(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT);
    }

    /**
     * Sets the specified bot flag into thread local data.
     *
     * @param isBot the specified bot flag
     */
    public static void setBot(final boolean isBot) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, isBot);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, isBot);
    }

    /**
     * Checks whether is mobile.
     *
     * @return {@code true} if it is mobile, returns {@code false} otherwise
     */
    public static boolean isMobile() {
        final JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return false;
        }

        return data.optBoolean(Common.IS_MOBILE);
    }

    /**
     * Sets the specified mobile flag into thread local data.
     *
     * @param isMobile the specified mobile flag
     */
    public static void setMobile(final boolean isMobile) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(Common.IS_MOBILE, isMobile);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(Common.IS_MOBILE, isMobile);
    }

    /**
     * Gets the current avatar view mode from thread local data.
     *
     * @return avatar view mode, returns {@value UserExt#USER_AVATAR_VIEW_MODE_C_ORIGINAL} "original mode" if not found
     */
    public static int getAvatarViewMode() {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
        }

        return data.optInt(UserExt.USER_AVATAR_VIEW_MODE);
    }

    /**
     * Sets the specified avatar view mode into thread local data.
     *
     * @param avatarViewMode the specified avatar view mode
     */
    public static void setAvatarViewMode(final int avatarViewMode) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(UserExt.USER_AVATAR_VIEW_MODE, avatarViewMode);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(UserExt.USER_AVATAR_VIEW_MODE, avatarViewMode);
    }

    /**
     * Checks whether is logged in.
     *
     * @return {@code true} if logged in, returns {@code false} otherwise
     */
    public static boolean isLoggedIn() {
        final JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return false;
        }

        return data.optBoolean(Common.IS_LOGGED_IN);
    }

    /**
     * Sets the specified logged in flag into thread local data.
     *
     * @param isLoggedIn the specified logged in flag
     */
    public static void setLoggedIn(final boolean isLoggedIn) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(Common.IS_LOGGED_IN, isLoggedIn);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(Common.IS_LOGGED_IN, isLoggedIn);
    }

    /**
     * Gets the current user from thread local data.
     *
     * @return user, returns {@code null} if not found
     */
    public static JSONObject getUser() {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return null;
        }

        return data.optJSONObject(User.USER);
    }

    /**
     * Sets the specified user into thread local data.
     *
     * @param user the specified user
     */
    public static void setUser(final JSONObject user) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(User.USER, user);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(User.USER, user);
    }

    /**
     * Gets the current template dir from thread local data.
     *
     * @return template dir, returns "classic" if not found
     */
    public static String getTemplateDir() {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            return "classic";
        }

        return data.optString(Keys.TEMAPLTE_DIR_NAME);
    }

    /**
     * Sets the specified template dir into thread local data.
     *
     * @param templateDir the specified template dir
     */
    public static void setTemplateDir(final String templateDir) {
        JSONObject data = THREAD_LOCAL_DATA.get();
        if (null == data) {
            data = new JSONObject().put(Keys.TEMAPLTE_DIR_NAME, templateDir);
            THREAD_LOCAL_DATA.set(data);

            return;
        }

        data.put(Keys.TEMAPLTE_DIR_NAME, templateDir);
    }

    /**
     * Clears the thread local data.
     */
    public static void clearThreadLocalData() {
        THREAD_LOCAL_DATA.set(null);
    }

    /**
     * Gets CSRF token from the specified request.
     *
     * @param context the specified request context
     * @return CSRF token, returns {@code ""} if not found
     */
    public static String getCSRFToken(final RequestContext context) {
        final JSONObject user = Sessions.getUser();
        if (null == user) {
            return "";
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        if (StringUtils.isBlank(userId)) {
            return "";
        }

        JSONObject csrfTokenValue = SESSION_CACHE.get(userId + Common.CSRF_TOKEN);
        if (null == csrfTokenValue) {
            csrfTokenValue = new JSONObject();
            csrfTokenValue.put(Common.DATA, RandomStringUtils.randomAlphanumeric(12));

            SESSION_CACHE.put(userId + Common.CSRF_TOKEN, csrfTokenValue);
        }

        return csrfTokenValue.optString(Common.DATA);
    }

    /**
     * Logins the specified user.
     *
     * @param response      the specified response
     * @param userId        the specified user id, for example,
     * @param rememberLogin remember login or not
     * @return token, returns {@code null} if login failed
     */
    public static String login(final HttpServletResponse response,
                               final String userId, final boolean rememberLogin) {
        try {
            final BeanManager beanManager = BeanManager.getInstance();
            final UserRepository userRepository = beanManager.getReference(UserRepository.class);
            final JSONObject user = userRepository.get(userId);
            if (null == user) {
                LOGGER.log(Level.WARN, "Login user [id=" + userId + "] failed");

                return null;
            }

            SESSION_CACHE.put(userId, user);

            final JSONObject csrfToken = new JSONObject();
            csrfToken.put(Common.DATA, RandomStringUtils.randomAlphanumeric(12));
            SESSION_CACHE.put(userId + Common.CSRF_TOKEN, csrfToken);

            final JSONObject cookieJSONObject = new JSONObject();
            cookieJSONObject.put(Keys.OBJECT_ID, user.optString(Keys.OBJECT_ID));

            final String random = RandomStringUtils.randomAlphanumeric(16);
            cookieJSONObject.put(Keys.TOKEN, user.optString(User.USER_PASSWORD) + COOKIE_ITEM_SEPARATOR + random);
            cookieJSONObject.put(Common.REMEMBER_LOGIN, rememberLogin);

            final String ret = Crypts.encryptByAES(cookieJSONObject.toString(), Symphonys.COOKIE_SECRET);
            final Cookie cookie = new Cookie(COOKIE_NAME, ret);

            cookie.setPath("/");
            cookie.setMaxAge(rememberLogin ? COOKIE_EXPIRY : -1);
            cookie.setHttpOnly(true); // HTTP Only
            cookie.setSecure(StringUtils.equalsIgnoreCase(Latkes.getServerScheme(), "https"));

            response.addCookie(cookie);

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Login user [id=" + userId + "] failed", e);

            return null;
        }
    }

    /**
     * Logouts the specified user.
     *
     * @param userId   the specified user id
     * @param response the specified response
     */
    public static void logout(final String userId, final HttpServletResponse response) {
        if (null != response) {
            final Cookie cookie = new Cookie(COOKIE_NAME, null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        SESSION_CACHE.remove(userId);

        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        userMgmtService.updateOnlineStatus(userId, "", false, true);
    }

    /**
     * Gets the current user with the specified request.
     *
     * @param request the specified request
     * @return the current user, returns {@code null} if not logged in
     */
    public static JSONObject currentUser(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (null == cookies || 0 == cookies.length) {
            return null;
        }

        try {
            for (final Cookie cookie : cookies) {
                if (!Sessions.COOKIE_NAME.equals(cookie.getName())) {
                    continue;
                }

                final String value = Crypts.decryptByAES(cookie.getValue(), Symphonys.COOKIE_SECRET);
                final JSONObject cookieJSONObject = new JSONObject(value);

                final String userId = cookieJSONObject.optString(Keys.OBJECT_ID);
                if (StringUtils.isBlank(userId)) {
                    return null;
                }

                JSONObject ret = SESSION_CACHE.get(userId);
                if (null == ret) {
                    ret = tryLogInWithCookie(cookieJSONObject, request);
                }
                if (null == ret) {
                    return null;
                }

                final String token = cookieJSONObject.optString(Keys.TOKEN);
                final String password = StringUtils.substringBeforeLast(token, COOKIE_ITEM_SEPARATOR);
                final String userPassword = ret.optString(User.USER_PASSWORD);
                if (!userPassword.equals(password)) {
                    return null;
                }

                if (UserExt.USER_STATUS_C_INVALID == ret.optInt(UserExt.USER_STATUS)
                        || UserExt.USER_STATUS_C_INVALID_LOGIN == ret.optInt(UserExt.USER_STATUS)
                        || UserExt.USER_STATUS_C_DEACTIVATED == ret.optInt(UserExt.USER_STATUS)) {
                    SESSION_CACHE.remove(userId);

                    return null;
                }

                final String ip = Requests.getRemoteAddr(request);
                if (StringUtils.isNotBlank(ip)) {
                    ret.put(UserExt.USER_LATEST_LOGIN_IP, ip);
                    SESSION_CACHE.put(userId, ret);
                }

                return JSONs.clone(ret);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Parses cookie failed, clears cookie");
        }

        return null;
    }

    /**
     * Tries to login with cookie.
     *
     * @param cookieJSONObject the specified cookie json object
     * @param request          the specified request
     * @return returns user if logged in, returns {@code null} otherwise
     */
    private static JSONObject tryLogInWithCookie(final JSONObject cookieJSONObject,
                                                 final HttpServletRequest request) {
        final BeanManager beanManager = BeanManager.getInstance();
        final UserRepository userRepository = beanManager.getReference(UserRepository.class);
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);

        try {
            final String userId = cookieJSONObject.optString(Keys.OBJECT_ID);
            if (StringUtils.isBlank(userId)) {
                return null;
            }

            final JSONObject ret = userRepository.get(userId);
            if (null == ret) {
                return null;
            }

            final String ip = Requests.getRemoteAddr(request);
            if (StringUtils.isNotBlank(ip)) {
                ret.put(UserExt.USER_LATEST_LOGIN_IP, ip);
            }

            final String userPassword = ret.optString(User.USER_PASSWORD);
            final String token = cookieJSONObject.optString(Keys.TOKEN);
            final String password = StringUtils.substringBeforeLast(token, COOKIE_ITEM_SEPARATOR);
            if (userPassword.equals(password)) {
                userMgmtService.updateOnlineStatus(userId, ip, true, true);

                SESSION_CACHE.put(userId, ret);

                return ret;
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Parses cookie failed, clears cookie");
        }

        return null;
    }

    /**
     * Gets a value from session cache with the specified key.
     *
     * @param key the specified key
     * @return session, returns {@code null} if not found
     */
    public static JSONObject get(final String key) {
        JSONObject ret = SESSION_CACHE.get(key);
        if (null == ret) {
            return null;
        }

        return JSONs.clone(ret);
    }

    /**
     * Puts a value into session cache with the specified key and value.
     *
     * @param key   the specified key
     * @param value the specified value
     */
    public static void put(final String key, final JSONObject value) {
        SESSION_CACHE.put(key, value);
    }

    /**
     * Private constructor.
     */
    private Sessions() {
    }
}
