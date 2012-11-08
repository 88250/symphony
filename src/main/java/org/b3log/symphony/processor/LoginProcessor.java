/*
 * Copyright (c) 2012, B3log Team
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
package org.b3log.symphony.processor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Sessions;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.QueryResults;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Login processor.
 * 
 * <p>
 * For user
 *   <ul>
 *     <li>Registration</li>
 *     <li>Login</li>
 *     <li>Logout</li>
 *   </ul>
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.5, Nov 8, 2012
 * @since 0.2.0
 */
@RequestProcessor
public final class LoginProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginProcessor.class.getName());
    /**
     * User management service.
     */
    private UserMgmtService userMgmtService = UserMgmtService.getInstance();
    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();
    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();
    /**
     * User service.
     */
    private UserService userService = UserServiceFactory.getUserService();

    /**
     * Shows registration page.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception 
     */
    @RequestProcessing(value = "/register", method = HTTPRequestMethod.GET)
    public void showRegister(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("register.ftl");

        final Map<String, Object> dataModel = renderer.getDataModel();

        Filler.fillHeader(request, response, dataModel);
        Filler.fillFooter(dataModel);
    }

    /**
     * Registers user.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/register", method = HTTPRequestMethod.POST)
    @Before(adviceClass = UserRegisterValidation.class)
    public void register(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = new JSONObject((String) request.getParameterMap().keySet().iterator().next());
        } catch (final JSONException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            requestJSONObject = new JSONObject();
        }
        final String name = requestJSONObject.optString(User.USER_NAME);

        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);

        final JSONObject user = new JSONObject();
        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, password);

        try {
            userMgmtService.addUser(user);

            Sessions.login(request, response, user);
            userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), true);

            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("registerFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.SEVERE, msg, e);

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Logins user.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/login", method = HTTPRequestMethod.POST)
    public void login(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        ret.put(Keys.MSG, langPropsService.get("loginFailLabel"));

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String nameOrEmail = requestJSONObject.optString("nameOrEmail");

        try {
            JSONObject user = userQueryService.getUserByName(nameOrEmail);
            if (null == user) {
                user = userQueryService.getUserByEmail(nameOrEmail);
            }

            if (null == user) {
                return;
            }

            final String userPassword = user.optString(User.USER_PASSWORD);
            if (userPassword.equals(requestJSONObject.optString(User.USER_PASSWORD))) {
                Sessions.login(request, response, user);
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), true);

                ret.put(Keys.MSG, "");
                ret.put(Keys.STATUS_CODE, true);
            }
        } catch (final ServiceException e) {
            ret.put(Keys.MSG, langPropsService.get("loginFailLabel"));
        }
    }

    /**
     * Logout.
     *
     * @param context the specified context
     * @throws IOException io exception
     */
    @RequestProcessing(value = {"/logout"}, method = HTTPRequestMethod.GET)
    public void logout(final HTTPRequestContext context) throws IOException {
        final HttpServletRequest httpServletRequest = context.getRequest();

        Sessions.logout(httpServletRequest, context.getResponse());

        String destinationURL = httpServletRequest.getParameter(Common.GOTO);
        if (Strings.isEmptyOrNull(destinationURL)) {
            destinationURL = "/";
        }

        context.getResponse().sendRedirect(destinationURL);
    }

    /**
     * Gets the current user.
     *
     * @param request the specified request
     * @return the current user, {@code null} if not found
     */
    public static JSONObject getCurrentUser(final HttpServletRequest request) {
        final GeneralUser currentUser = UserServiceFactory.getUserService().getCurrentUser(request);
        if (null == currentUser) {
            return null;
        }

        final String email = currentUser.getEmail();

        try {
            return UserRepository.getInstance().getByEmail(email);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets current user by request failed, returns null", e);

            return null;
        }
    }

    /**
     * Tries to login with cookie.
     *
     * @param request the specified request
     * @param response the specified response
     * @return returns {@code true} if logged in, returns {@code false} otherwise
     */
    public static boolean tryLogInWithCookie(final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie[] cookies = request.getCookies();
        if (null == cookies || 0 == cookies.length) {
            return false;
        }

        try {
            for (int i = 0; i < cookies.length; i++) {
                final Cookie cookie = cookies[i];

                if (!"b3log-latke".equals(cookie.getName())) {
                    continue;
                }

                final JSONObject cookieJSONObject = new JSONObject(cookie.getValue());

                final String userEmail = cookieJSONObject.optString(User.USER_EMAIL);
                if (Strings.isEmptyOrNull(userEmail)) {
                    break;
                }

                final JSONObject user = UserQueryService.getInstance().getUserByEmail(userEmail.toLowerCase().trim());
                if (null == user) {
                    break;
                }

                final String userPassword = user.optString(User.USER_PASSWORD);
                final String hashPassword = cookieJSONObject.optString(User.USER_PASSWORD);
                if (MD5.hash(userPassword).equals(hashPassword)) {
                    Sessions.login(request, response, user);
                    UserMgmtService.getInstance().updateOnlineStatus(user.optString(Keys.OBJECT_ID), true);
                    LOGGER.log(Level.FINER, "Logged in with cookie[email={0}]", userEmail);

                    return true;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Parses cookie failed, clears the cookie[name=b3log-latke]", e);

            final Cookie cookie = new Cookie("b3log-latke", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
        }

        return false;
    }
}
