/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.api.v2;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.CaptchaProcessor;
import org.b3log.symphony.processor.LoginProcessor;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Login API v2.
 * <p>
 * <ul>
 * <li>Login (/api/v2/login), POST</li>
 * <li>Logout (/api/v2/logout), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Mar 16, 2016
 * @since 2.1.0
 */
@RequestProcessor
public class LoginAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginAPI2.class);
    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;
    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Logout.
     *
     * @param context the specified context
     */
    @RequestProcessing(value = {"/api/v2/logout"}, method = HTTPRequestMethod.POST)
    public void logout(final HTTPRequestContext context) {
        final HttpServletRequest httpServletRequest = context.getRequest();

        Sessions.logout(httpServletRequest, context.getResponse());

        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, 0);
    }

    /**
     * Login.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/api/v2/login", method = HTTPRequestMethod.POST)
    public void login(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        context.renderJSON().renderMsg(langPropsService.get("loginFailLabel"));
        context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.NOT_FOUND);

        try {
            final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
            final String nameOrEmail = requestJSONObject.optString(User.USER_NAME);

            JSONObject user = userQueryService.getUserByName(nameOrEmail);
            if (null == user) {
                user = userQueryService.getUserByEmail(nameOrEmail);
            }

            if (null == user) {
                context.renderMsg(langPropsService.get("notFoundUserLabel"));

                return;
            }

            if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
                context.renderMsg(langPropsService.get("userBlockLabel"));

                return;
            }

            if (UserExt.USER_STATUS_C_NOT_VERIFIED == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
                context.renderMsg(langPropsService.get("notVerifiedLabel"));

                return;
            }

            if (UserExt.USER_STATUS_C_INVALID_LOGIN == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
                context.renderMsg(langPropsService.get("invalidLoginLabel"));

                return;
            }

            final String userId = user.optString(Keys.OBJECT_ID);
            JSONObject wrong = LoginProcessor.WRONG_PWD_TRIES.get(userId);
            if (null == wrong) {
                wrong = new JSONObject();
            }

            final int wrongCount = wrong.optInt(Common.WRON_COUNT);
            if (wrongCount > 3) {
                final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
                if (!StringUtils.equals(wrong.optString(CaptchaProcessor.CAPTCHA), captcha)) {
                    context.renderMsg(langPropsService.get("captchaErrorLabel"));
                    context.renderJSONValue(Common.NEED_CAPTCHA, userId);

                    return;
                }
            }

            final String userPassword = user.optString(User.USER_PASSWORD);
            if (userPassword.equals(requestJSONObject.optString(User.USER_PASSWORD))) {
                final String token = Sessions.login(request, response, user, requestJSONObject.optBoolean(Common.REMEMBER_LOGIN));

                final String ip = Requests.getRemoteAddr(request);
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);

                context.renderMsg("").renderJSONValue(Keys.STATUS_CODE, 0);
                context.renderJSONValue(Common.TOKEN, token);
                context.renderJSONValue(User.USER_NAME, user.optString(User.USER_NAME));

                LoginProcessor.WRONG_PWD_TRIES.remove(userId);

                return;
            }

            if (wrongCount > 2) {
                context.renderJSONValue(Common.NEED_CAPTCHA, userId);
            }

            wrong.put(Common.WRON_COUNT, wrongCount + 1);
            LoginProcessor.WRONG_PWD_TRIES.put(userId, wrong);

            context.renderMsg(langPropsService.get("wrongPwdLabel"));
        } catch (final Exception e) {
            context.renderMsg(langPropsService.get("loginFailLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }
}
