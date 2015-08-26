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
package org.b3log.symphony.processor;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.model.Verifycode;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.UserRegister2Validation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.PointtransferMgmtService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.service.VerifycodeMgmtService;
import org.b3log.symphony.service.VerifycodeQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Results;
import org.b3log.symphony.util.Sessions;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Login/Register processor.
 *
 * <p>
 * For user
 * <ul>
 * <li>Registration (/register), GET</li>
 * <li>Login (/login), GET/POST</li>
 * <li>Logout (/logout), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.0.8, Aug 18, 2015
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
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Verifycode management service.
     */
    @Inject
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * Verifycode query service.
     */
    @Inject
    private VerifycodeQueryService verifycodeQueryService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Shows registration page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/register", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showRegister(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put(Common.REFERRAL, "88250"); // HeHe~
        String referral = request.getParameter("r");
        if (!Strings.isEmptyOrNull(referral)) {
            dataModel.put(Common.REFERRAL, referral);
        }

        final String code = request.getParameter("code");
        if (Strings.isEmptyOrNull(code)) { // Register Step 1
            renderer.setTemplateName("register.ftl");
        } else { // Register Step 2
            final JSONObject verifycode = verifycodeQueryService.getVerifycode(code);
            if (null == verifycode) {
                dataModel.put(Keys.MSG, langPropsService.get("verifycodeExpiredLabel"));
                renderer.setTemplateName("/error/custom.ftl");
            } else {
                renderer.setTemplateName("register2.ftl");

                final String userId = verifycode.optString(Verifycode.USER_ID);
                final JSONObject user = userQueryService.getUser(userId);
                dataModel.put(User.USER, user);

                referral = StringUtils.substringAfter(code, "r=");
                if (!Strings.isEmptyOrNull(referral)) {
                    dataModel.put(Common.REFERRAL, referral);
                }
            }
        }

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Register Step 1.
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

        final JSONObject ret = Results.falseResult();
        renderer.setJSONObject(ret);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = new JSONObject((String) request.getParameterMap().keySet().iterator().next());
        } catch (final JSONException e1) {
            LOGGER.log(Level.ERROR, e1.getMessage(), e1);
            requestJSONObject = new JSONObject();
        }

        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String referral = requestJSONObject.optString(Common.REFERRAL);

        final JSONObject user = new JSONObject();
        user.put(User.USER_NAME, name);
        user.put(User.USER_EMAIL, email);
        user.put(User.USER_PASSWORD, "");

        try {
            final String newUserId = userMgmtService.addUser(user);

            final JSONObject verifycode = new JSONObject();
            verifycode.put(Verifycode.BIZ_TYPE, Verifycode.BIZ_TYPE_C_REGISTER);
            String code = RandomStringUtils.randomNumeric(6);
            if (!Strings.isEmptyOrNull(referral)) {
                code += "r=" + referral;
            }
            verifycode.put(Verifycode.CODE, code);
            verifycode.put(Verifycode.EXPIRED, DateUtils.addDays(new Date(), 1).getTime());
            verifycode.put(Verifycode.RECEIVER, email);
            verifycode.put(Verifycode.STATUS, Verifycode.STATUS_C_UNSENT);
            verifycode.put(Verifycode.TYPE, Verifycode.TYPE_C_EMAIL);
            verifycode.put(Verifycode.USER_ID, newUserId);
            verifycodeMgmtService.addVerifycode(verifycode);

            ret.put(Keys.STATUS_CODE, true);
            ret.put(Keys.MSG, langPropsService.get("verifycodeSentLabel"));
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("registerFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg + "[name={0}, email={1}]", name, email);

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Register Step 2.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws ServletException servlet exception
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/register2", method = HTTPRequestMethod.POST)
    @Before(adviceClass = UserRegister2Validation.class)
    public void register2(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = Results.falseResult();
        renderer.setJSONObject(ret);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = new JSONObject((String) request.getParameterMap().keySet().iterator().next());
        } catch (final JSONException e1) {
            LOGGER.log(Level.ERROR, e1.getMessage(), e1);
            requestJSONObject = new JSONObject();
        }

        final String password = requestJSONObject.optString(User.USER_PASSWORD); // Hashed
        final int appRole = requestJSONObject.optInt(UserExt.USER_APP_ROLE);
        final String referral = requestJSONObject.optString(Common.REFERRAL);
        final String userId = requestJSONObject.optString(Common.USER_ID);

        String name = null;
        String email = null;
        try {
            final JSONObject user = userQueryService.getUser(userId);
            if (null == user) {
                ret.put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + "User Not Found");

                return;
            }

            name = user.optString(User.USER_NAME);
            email = user.optString(User.USER_EMAIL);

            user.put(UserExt.USER_APP_ROLE, appRole);
            user.put(User.USER_PASSWORD, password);
            user.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);

            userMgmtService.addUser(user);

            Sessions.login(request, response, user);
            
            final String ip = Requests.getRemoteAddr(request);
            userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);

            if (!Strings.isEmptyOrNull(referral)) {
                final JSONObject referralUser = userQueryService.getUserByName(referral);
                if (null != referralUser) {
                    final String referralId = referralUser.optString(Keys.OBJECT_ID);
                    // Point
                    pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, userId,
                            Pointtransfer.TRANSFER_TYPE_C_INVITED_REGISTER,
                            Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER, referralId);
                    pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, referralId,
                            Pointtransfer.TRANSFER_TYPE_C_INVITE_REGISTER,
                            Pointtransfer.TRANSFER_SUM_C_INVITE_REGISTER, userId);
                }
            }

            ret.put(Keys.STATUS_CODE, true);

            LOGGER.log(Level.INFO, "Registered a user [name={0}, email={1}]", name, email);

            // Timeline
            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Common.NEW_USER);
            String content = langPropsService.get("timelineNewUserLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + name + "'>" + name + "</a>");
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("registerFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg + "[name={0}, email={1}]", name, email);

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

        final JSONObject ret = Results.falseResult();
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
                ret.put(Keys.MSG, langPropsService.get("notFoundUserLabel"));
                return;
            }

            if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
                ret.put(Keys.MSG, langPropsService.get("userBlockLabel"));

                return;
            }

            final String userPassword = user.optString(User.USER_PASSWORD);
            if (userPassword.equals(requestJSONObject.optString(User.USER_PASSWORD))) {
                Sessions.login(request, response, user);

                final String ip = Requests.getRemoteAddr(request);
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), ip, true);

                ret.put(Keys.MSG, "");
                ret.put(Keys.STATUS_CODE, true);

                return;
            }

            ret.put(Keys.MSG, langPropsService.get("wrongPwdLabel"));
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
}
