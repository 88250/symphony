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
package org.b3log.symphony.api;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ActivityMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity API processor.
 *
 * <ul>
 * <li>Daily checkin (/api/v1/activities/checkin), POST</li>
 * </ul>
 *
 * @author <a href="http://wdx.me">DX</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jan 2, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class ActivityProcessor {

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Activity management service.
     */
    @Inject
    private ActivityMgmtService activityMgmtService;

    /**
     * Checkin.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws JSONException json exception
     * @throws IOException io exception
     * @throws ServiceException service exception
     */
    @RequestProcessing(value = "/api/v1/activities/checkin", method = HTTPRequestMethod.POST)
    public void checkin(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws JSONException, IOException, ServiceException {
        final String auth = request.getHeader("Authorization");
        if (auth == null) {//TODO validate
            return;
        }
        final String email = new JSONObject(auth.substring("Bearer ".length())).optString("userEmail");

        final JSONObject currentUser = userQueryService.getUserByEmail(email);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int checkinReward = activityMgmtService.dailyCheckin(userId);
        final JSONObject checkedinUser = userQueryService.getUserByEmail(email);
        final int balance = checkedinUser.optInt(UserExt.USER_POINT);

        final JSONObject ret = new JSONObject();
        context.renderJSON(ret);

        final JSONObject checkin = new JSONObject();
        checkin.put("balance", balance);
        checkin.put("reward", checkinReward);
        checkin.put("streak", checkedinUser.optInt(UserExt.USER_CURRENT_CHECKIN_STREAK));
        checkin.put("streaked", checkedinUser.optInt(UserExt.USER_LONGEST_CHECKIN_STREAK));
        ret.put("checkin", checkin);
    }
}
