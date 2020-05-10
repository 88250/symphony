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
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ActivityQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Validates for activity 1A0001 collect.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 10, 2020
 * @since 1.3.0
 */
@Singleton
public class Activity1A0001CollectValidationMidware {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    public void handle(final RequestContext context) {
        if (Symphonys.ACTIVITY_1A0001_CLOSED) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityClosedLabel")));
            context.abort();
            return;
        }

        final Calendar calendar = Calendar.getInstance();

        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activity1A0001CloseLabel")));
            context.abort();
            return;
        }

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 16) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityCollectNotOpenLabel")));
            context.abort();
            return;
        }

        final JSONObject currentUser = Sessions.getUser();
        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
            context.abort();
            return;
        }

        if (!activityQueryService.is1A0001Today(currentUser.optString(Keys.OBJECT_ID))) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityNotParticipatedLabel")));
            context.abort();
            return;
        }

        context.handle();
    }
}
