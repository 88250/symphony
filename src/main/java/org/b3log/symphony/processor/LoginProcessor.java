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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.annotation.RequestProcessing;
import org.b3log.latke.annotation.RequestProcessor;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.QueryResults;
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
 * @version 1.0.0.2, Aug 9, 2012
 * @since 0.2.0
 */
@RequestProcessor
public class LoginProcessor {

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
     * Shows registration page.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/register", method = HTTPRequestMethod.GET)
    public void showRegister(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("register.ftl");
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
    public void register(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.defaultResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final String name = requestJSONObject.optString(User.USER_NAME);

        if (invalidUserName(name)) {
            ret.put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidUserNameLabel"));
            return;
        }

        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);

        final JSONObject user = new JSONObject();
        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, password);

        try {
            userMgmtService.addUser(user);
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

        final JSONObject ret = QueryResults.defaultResult();
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
            if (!userPassword.equals(requestJSONObject.optString(User.USER_PASSWORD))) {
                return;
            }

            ret.put(Keys.MSG, "");
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            ret.put(Keys.MSG, langPropsService.get("loginFailLabel"));
        }
    }

    /**
     * Checks whether the specified name is invalid.
     * 
     * <p>
     * A valid user name:
     *   <ul>
     *     <li>length [1, 20]</li>
     *     <li>content {a-z, A-Z, 0-9, _}</li>
     *   </ul>
     * </p>
     * 
     * @param name the specified name
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    private boolean invalidUserName(final String name) {
        final int length = name.length();
        if (length < 1 || length > 20) {
            return true;
        }

        char c = 0;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);

            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || '0' <= c && c <= '9' || '_' == c) {
                continue;
            }

            return true;
        }

        return false;
    }
}
