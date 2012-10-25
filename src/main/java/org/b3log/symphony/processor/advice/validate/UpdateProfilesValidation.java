/*
 * Copyright (c) 2012, B3log Team
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.UserExt;
import org.json.JSONObject;

/**
 * Validates for user profiles update.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 25, 2012 
 */
public class UpdateProfilesValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();
    /**
     * Max user URL length.
     */
    public static final int MAX_USER_URL_LENGTH = 100;
    /**
     * Max user QQ length.
     */
    public static final int MAX_USER_QQ_LENGTH = 12;
    /**
     * Max user intro length.
     */
    public static final int MAX_USER_INTRO_LENGTH = 255;

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

        try {
            final String userName = requestJSONObject.optString(User.USER_NAME);
            if (UserRegisterValidation.invalidUserName(userName)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserNameLabel")));
            }

            final String userURL = requestJSONObject.optString(User.USER_URL);
            if (!Strings.isEmptyOrNull(userURL) && invalidUserURL(userURL)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserURLLabel")));
            }


            final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
            if (!Strings.isEmptyOrNull(userQQ) && (!Strings.isNumeric(userQQ) || userQQ.length() > MAX_USER_QQ_LENGTH)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserURLLabel")));
            }

            final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);
            if (!Strings.isEmptyOrNull(userIntro) && userIntro.length() > MAX_USER_INTRO_LENGTH) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserIntroLabel")));
            }
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }
    }

    /**
     * Checks whether the specified user URL is invalid.
     * 
     * @param userURL the specified user URL
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    private boolean invalidUserURL(final String userURL) {
        try {
            new URL(userURL);
        } catch (final MalformedURLException e) {
            return true;
        }

        return userURL.length() > MAX_USER_URL_LENGTH;
    }
}
