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
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Validates for user point transfer.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.3.0
 */
@Singleton
public class PointTransferValidationMidware {

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

    public void handle(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject requestJSONObject = context.requestJSON();
        final String userName = requestJSONObject.optString(User.USER_NAME);
        if (StringUtils.isBlank(userName) || UserExt.COM_BOT_NAME.equals(userName)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
            context.abort();
            return;
        }

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        if (amount < 1 || amount > 5000) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("amountInvalidLabel")));
            context.abort();
            return;
        }

        JSONObject toUser = userQueryService.getUserByName(userName);
        if (null == toUser) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("notFoundUserLabel")));
            context.abort();
            return;
        }

        if (UserExt.USER_STATUS_C_VALID != toUser.optInt(UserExt.USER_STATUS)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
            context.abort();
            return;
        }

        request.setAttribute(Common.TO_USER, toUser);

        final JSONObject currentUser = Sessions.getUser();
        if (UserExt.USER_STATUS_C_VALID != currentUser.optInt(UserExt.USER_STATUS)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("userStatusInvalidLabel")));
            context.abort();
            return;
        }

        if (currentUser.optString(User.USER_NAME).equals(toUser.optString(User.USER_NAME))) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("cannotTransferSelfLabel")));
            context.abort();
            return;
        }

        final int balanceMinLimit = Symphonys.POINT_TRANSER_MIN;
        final int balance = currentUser.optInt(UserExt.USER_POINT);
        if (balance - amount < balanceMinLimit) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("insufficientBalanceLabel")));
            context.abort();
            return;
        }

        String memo = StringUtils.trim(requestJSONObject.optString(Pointtransfer.MEMO));
        if (128 < StringUtils.length(memo)) {
            context.renderJSON(new JSONObject().put(Keys.MSG, langPropsService.get("memoTooLargeLabel")));
            context.abort();
            return;
        }
        memo = Jsoup.clean(memo, Whitelist.none());
        request.setAttribute(Pointtransfer.MEMO, memo);

        context.handle();
    }
}
