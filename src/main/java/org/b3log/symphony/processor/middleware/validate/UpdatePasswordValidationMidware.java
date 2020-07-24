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
import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.json.JSONObject;

/**
 * Validates for user password update.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class UpdatePasswordValidationMidware {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    public void handle(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final String oldPwd = requestJSONObject.optString(User.USER_PASSWORD);
        if (StringUtils.isBlank(oldPwd)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("emptyOldPwdLabel")));
            context.abort();
            return;
        }

        final String pwd = requestJSONObject.optString(User.USER_PASSWORD);
        if (UserRegister2ValidationMidware.invalidUserPassword(pwd)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("invalidPasswordLabel")));
            context.abort();
            return;
        }

        context.handle();
    }
}
