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
package org.b3log.symphony.processor.validate;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdiceException;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.processor.LoginProcessor;
import org.json.JSONObject;

/**
 * UserRegisterValidation for validate {@link LoginProcessor}  register(Type POST) method.
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * @version 1.0.0.0, Oct 14, 2012 
 */
public class UserRegisterValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    @Override
    public void doAdvice(HTTPRequestContext context, Map<String, Object> args) throws RequestProcessAdiceException {

        final HttpServletRequest request = context.getRequest();

        JSONObject requestJSONObject;
        try {
            String json =  (String) request.getParameterMap().keySet().iterator().next();
            requestJSONObject = new JSONObject(json);
        } catch (final Exception e) {
            throw new RequestProcessAdiceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }
        final String name = requestJSONObject.optString(User.USER_NAME);
        final String email = requestJSONObject.optString(User.USER_EMAIL);
        final String password = requestJSONObject.optString(User.USER_PASSWORD);

        checkField(invalidUserName(name), "registerFailLabel", "invalidUserNameLabel");
        checkField(invalidUserEmail(email), "registerFailLabel", "invalidEmailLabel");
        checkField(invalidUserPassword(password), "registerFailLabel", "invalidPasswordLabel");

    }

    /**
     * Checks whether the specified name is invalid.
     * 
     * <p>
     * A valid user name:
     *   <ul>
     *     <li>length [1, 20]</li>
     *     <li>content {a-z, A-Z, 0-9, _}</li>
     *   </ul>
     * </p>
     * 
     * @param name the specified name
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    private boolean invalidUserName(final String name) {
        final int length = name.length();
        if (length < 1 || length > 20) {
            return true;
        }

        char c;
        for (int i = 0; i < length; i++) {
            c = name.charAt(i);

            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || '0' <= c && c <= '9' || '_' == c) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * check email,length(1,255),Standard email pattern.
     * @param email email
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    private boolean invalidUserEmail(final String email) {
        if (email.length() < 1 || email.length() > 255) {
            return true;
        }
        final String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern regex = Pattern.compile(check);
        final Matcher matcher = regex.matcher(email);
        final boolean isMatched = matcher.matches();
        return !isMatched;
    }

    /**
     * check password(1,16).
     * @param password the specific password
     * @return {@code true} if it is invalid, returns {@code false} otherwise
     */
    private boolean invalidUserPassword(final String password) {

        if (password.length() < 1 || password.length() > 16) {
            return true;
        }
        return false;
    }

    private void checkField(final boolean invalidUserName, final String failLabel, final String fieldLabel)
            throws RequestProcessAdiceException {
        if (invalidUserName) {
            throw new RequestProcessAdiceException(new JSONObject().put(Keys.MSG, langPropsService.get(failLabel)
                    + " - "
                    + langPropsService.get(fieldLabel)));
        }
    }

}
