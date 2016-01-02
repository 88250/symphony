/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
