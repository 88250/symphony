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

import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.OptionQueryService;
import org.json.JSONObject;

/**
 * UserRegister2Validation for validate {@link org.b3log.symphony.processor.LoginProcessor} register2(Type POST) method.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.3.0
 */
@Singleton
public class UserRegister2ValidationMidware {

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
     * Max password length.
     *
     * <p>
     * MD5 32
     * </p>
     */
    private static final int MAX_PWD_LENGTH = 32;

    /**
     * Min password length.
     */
    private static final int MIN_PWD_LENGTH = 1;

    public void handle(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();

        // check if admin allow to register
        final JSONObject option = optionQueryService.getOption(Option.ID_C_MISC_ALLOW_REGISTER);
        if ("1".equals(option.optString(Option.OPTION_VALUE))) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("notAllowRegisterLabel")));
            context.abort();

            return;
        }

        final int appRole = requestJSONObject.optInt(UserExt.USER_APP_ROLE);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);
        if (UserExt.USER_APP_ROLE_C_HACKER != appRole && UserExt.USER_APP_ROLE_C_PAINTER != appRole) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidAppRoleLabel")));
            context.abort();

            return;
        }


        if (invalidUserPassword(password)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("registerFailLabel") + " - " + langPropsService.get("invalidPasswordLabel")));
            context.abort();

            return;
        }

        context.handle();
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

}
