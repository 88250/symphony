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
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for chat message adding.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.4.0
 */
@Singleton
public class ChatMsgAddValidationMidware {

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
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    public void handle(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        request.setAttribute(Keys.REQUEST, requestJSONObject);
        final JSONObject currentUser = Sessions.getUser();
        if (System.currentTimeMillis() - currentUser.optLong(UserExt.USER_LATEST_CMT_TIME) < Symphonys.MIN_STEP_CHAT_TIME
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("tooFrequentCmtLabel")));
            context.abort();
            return;
        }

        String content = requestJSONObject.optString(Common.CONTENT);
        content = StringUtils.trim(content);
        if (StringUtils.isBlank(content) || content.length() > 4096) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("commentErrorLabel")));
            context.abort();
            return;
        }

        if (optionQueryService.containReservedWord(content)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
            context.abort();
            return;
        }

        requestJSONObject.put(Common.CONTENT, content);
        context.handle();
    }
}
