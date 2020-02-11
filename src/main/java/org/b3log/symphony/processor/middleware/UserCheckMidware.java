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
package org.b3log.symphony.processor.middleware;

import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * User check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.5
 */
@Singleton
public class UserCheckMidware {

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    public void handle(final RequestContext context) {
        final String userName = context.pathVar("userName");
        if (UserExt.NULL_USER_NAME.equals(userName)) {
            context.sendError(404);
            context.abort();
        }

        final JSONObject user = userQueryService.getUserByName(userName);
        if (null == user) {
            context.sendError(404);
            context.abort();

            return;
        }

        if (UserExt.USER_STATUS_C_NOT_VERIFIED == user.optInt(UserExt.USER_STATUS)) {
            context.sendError(404);
            context.abort();

            return;
        }

        if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
            context.sendError(404);
            context.abort();

            return;
        }

        context.attr(User.USER, user);
        context.handle();
    }
}
