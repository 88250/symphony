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
package org.b3log.symphony.processor.middleware.validate;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.CaptchaProcessor;
import org.b3log.symphony.service.InvitecodeQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

import java.util.Map;

/**
 * UserRegisterValidation for validate {@link org.b3log.symphony.processor.LoginProcessor} register(Type POST) method.
 *
 * @author <a href="https://hacpai.com/member/mainlove">Love Yao</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class UserRegisterValidationMidware {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(UserRegisterValidationMidware.class);

    /**
     * Max user name length.
     */
    public static final int MAX_USER_NAME_LENGTH = 64;

    /**
     * Min user name length.
     */
    public static final int MIN_USER_NAME_LENGTH = 1;

    /**
     * Max password length.
     * <p>
     * MD5 32
     * </p>
     */
    private static final int MAX_PWD_LENGTH = 32;
    /**
     * Min password length.
     */
    private static final int MIN_PWD_LENGTH = 1;
    /**
     * Captcha length.
     */
    private static final int CAPTCHA_LENGTH = 4;
    /**
     * Invitecode length.
     */
    private static final int INVITECODE_LENGHT = 16;
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;
    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;
    /**
     * Invitecode query service.
     */
    @Inject
    private InvitecodeQueryService invitecodeQueryService;
    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;
    /**
     * Role query servicce.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Checks whether the specified name is invalid.
     * <p>
     * A valid user name:
     * <ul>
     * <li>length [1, 64]</li>
     * <li>content {a-z, A-Z, 0-9, -}</li>
     * </ul>
     * </p>
     *
     * @param name the specified name
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    public static boolean invalidUserName(final String name) {
        if (StringUtils.isBlank(name)) {
            return true;
        }

        if (UserExt.isReservedUserName(name)) {
            return true;
        }

        final int length = name.length();
        if (length < MIN_USER_NAME_LENGTH || length > MAX_USER_NAME_LENGTH) {
            return true;
        }

        char c;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9') || '-' == c) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * Checks password, length [1, 16].
     *
     * @param password the specific password
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    public static boolean invalidUserPassword(final String password) {
        return password.length() < MIN_PWD_LENGTH || password.length() > MAX_PWD_LENGTH;
    }

    public void handle(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final String referral = requestJSONObject.optString(Common.REFERRAL);

        // check if admin allow to register
        final JSONObject option = optionQueryService.getOption(Option.ID_C_MISC_ALLOW_REGISTER);
        if ("1".equals(option.optString(Option.OPTION_VALUE))) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("notAllowRegisterLabel")));
            context.abort();

            return;
        }

        boolean useInvitationLink = false;

        if (!invalidUserName(referral)) {
            try {
                final JSONObject referralUser = userQueryService.getUserByName(referral);
                if (null != referralUser) {

                    final Map<String, JSONObject> permissions =
                            roleQueryService.getUserPermissionsGrantMap(referralUser.optString(Keys.OBJECT_ID));
                    final JSONObject useILPermission =
                            permissions.get(Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
                    useInvitationLink = useILPermission.optBoolean(Permission.PERMISSION_T_GRANT);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Query user [name=" + referral + "] failed", e);
            }
        }

        // invitecode register
        if (!useInvitationLink && "2".equals(option.optString(Option.OPTION_VALUE))) {
            final String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);

            if (StringUtils.isBlank(invitecode) || INVITECODE_LENGHT != invitecode.length()) {
                context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidInvitecodeLabel")));
                context.abort();

                return;
            }

            final JSONObject ic = invitecodeQueryService.getInvitecode(invitecode);
            if (null == ic) {
                context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidInvitecodeLabel")));
                context.abort();

                return;
            }

            if (Invitecode.STATUS_C_UNUSED != ic.optInt(Invitecode.STATUS)) {
                context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("usedInvitecodeLabel")));
                context.abort();

                return;
            }
        }

        // open register
        if (useInvitationLink || "0".equals(option.optString(Option.OPTION_VALUE))) {
            final String captcha = requestJSONObject.optString(CaptchaProcessor.CAPTCHA);
            if (CaptchaProcessor.invalidCaptcha(captcha)) {
                context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("captchaErrorLabel")));
                context.abort();

                return;
            }
        }

        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final int appRole = requestJSONObject.optInt(UserExt.USER_APP_ROLE);

        if (UserExt.isReservedUserName(name)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("reservedUserNameLabel")));
            context.abort();

            return;
        }

        if (invalidUserName(name)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidUserNameLabel")));
            context.abort();

            return;
        }

        if (!Strings.isEmail(email)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidEmailLabel")));
            context.abort();

            return;
        }

        if (!UserExt.isValidMailDomain(email)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidEmail1Label")));
            context.abort();

            return;
        }

        if (UserExt.USER_APP_ROLE_C_HACKER != appRole && UserExt.USER_APP_ROLE_C_PAINTER != appRole) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidAppRoleLabel")));
            context.abort();

            return;
        }

        context.handle();
    }
}
