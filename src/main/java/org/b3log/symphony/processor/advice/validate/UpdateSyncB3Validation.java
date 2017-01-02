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
package org.b3log.symphony.processor.advice.validate;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.UserExt;
import org.json.JSONObject;

/**
 * Validates for user sync B3 update.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 26, 2012
 * @since 0.2.0
 */
@Named
@Singleton
public class UpdateSyncB3Validation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Max user B3 key length.
     */
    public static final int MAX_USER_B3_KEY_LENGTH = 20;

    /**
     * Max user B3 client interface URL length.
     */
    public static final int MAX_USER_B3_CLIENT_URL = 150;

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

        final String b3Key = requestJSONObject.optString(UserExt.USER_B3_KEY);
        if (!Strings.isEmptyOrNull(b3Key) && b3Key.length() > MAX_USER_B3_KEY_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3KeyLabel")));
        }

        final String clientAddArticleURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);
        if (!Strings.isEmptyOrNull(clientAddArticleURL) && clientAddArticleURL.length() > MAX_USER_B3_CLIENT_URL) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3ClientURLLabel")));
        }

        final String clientAddCommentURL = requestJSONObject.optString(UserExt.USER_B3_CLIENT_ADD_COMMENT_URL);
        if (!Strings.isEmptyOrNull(clientAddCommentURL) && clientAddCommentURL.length() > MAX_USER_B3_CLIENT_URL) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserB3ClientURLLabel")));
        }
    }
}
