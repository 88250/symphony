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
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ActivityQueryService;
import org.b3log.symphony.service.LivenessQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Validates for activity 1A0001.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 10, 2020
 * @since 1.3.0
 */
@Singleton
public class Activity1A0001ValidationMidware {

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

    /**
     * Liveness query service.
     */
    @Inject
    private LivenessQueryService livenessQueryService;

    public void handle(final RequestContext context) {
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int currentLiveness = livenessQueryService.getCurrentLivenessPoint(userId);
        final int livenessMax = Symphonys.ACTIVITY_YESTERDAY_REWARD_MAX;
        final float liveness = (float) currentLiveness / livenessMax * 100;
        final float livenessThreshold = Symphonys.ACTIVITY_1A0001_LIVENESS_THRESHOLD;
        if (liveness < livenessThreshold) {
            String msg = langPropsService.get("activityNeedLivenessLabel");
            msg = msg.replace("${liveness}", livenessThreshold + "%");
            msg = msg.replace("${current}", String.format("%.2f", liveness) + "%");
            context.renderJSON(new JSONObject().put(Keys.MSG, msg));
            context.abort();

            return;
        }

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
        final int minute = calendar.get(Calendar.MINUTE);
        if (hour > 14 || (hour == 14 && minute > 55)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityEndLabel")));
            context.abort();

            return;
        }

        final JSONObject requestJSONObject = context.requestJSON();
        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (200 != amount && 300 != amount && 400 != amount && 500 != amount) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
            context.abort();

            return;
        }

        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);
        if (0 != smallOrLarge && 1 != smallOrLarge) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
            context.abort();

            return;
        }

        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
            context.abort();

            return;
        }

        if (activityQueryService.is1A0001Today(userId)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("activityParticipatedLabel")));
        }

        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < 0) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
            context.abort();

            return;
        }

        context.handle();
    }
}