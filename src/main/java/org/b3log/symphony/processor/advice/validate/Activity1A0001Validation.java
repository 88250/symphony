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
package org.b3log.symphony.processor.advice.validate;

import java.util.Calendar;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ActivityQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for activity 1A0001.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 16, 2015
 */
@Named
@Singleton
public class Activity1A0001Validation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        if (Symphonys.getBoolean("activity1A0001Closed")) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityClosedLabel")));
        }

        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (hour > 14 || (hour == 14 && minute > 55) || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityEndLabel")));
        }

        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (200 != amount && 300 != amount && 400 != amount && 500 != amount) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
        }

        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);
        if (0 != smallOrLarge && 1 != smallOrLarge) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityBetFailLabel")));
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("reloginLabel")));
        }

        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        if (activityQueryService.is1A0001Today(currentUser.optString(Keys.OBJECT_ID))) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("activityParticipatedLabel")));
        }

        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < 0) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
        }
    }
}
