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
package org.b3log.symphony.processor.advice.validate;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for user point transfer.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Mar 8, 2016
 * @since 1.3.0
 */
@Named
@Singleton
public class PointTransferValidation extends BeforeRequestProcessAdvice {

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

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final String userName = requestJSONObject.optString(User.USER_NAME);
        if (Strings.isEmptyOrNull(userName)
                || UserExt.DEFAULT_CMTER_NAME.equals(userName) || UserExt.NULL_USER_NAME.equals(userName)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
        }

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (amount < 1 || amount > 5000) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("amountInvalidLabel")));
        }

        JSONObject toUser;
        try {
            toUser = userQueryService.getUserByName(userName);
        } catch (final ServiceException e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
        }

        if (null == toUser) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
        }

        if (UserExt.USER_STATUS_C_VALID != toUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        request.setAttribute(Common.TO_USER, toUser);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("reloginLabel")));
        }

        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
        }

        if (currentUser.optString(User.USER_NAME).equals(toUser.optString(User.USER_NAME))) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("cannotTransferSelfLabel")));
        }

        final int balanceMinLimit = Symphonys.getInt("pointTransferMin");
        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < balanceMinLimit) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
        }
    }
}
