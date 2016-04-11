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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.ArrayUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for user profiles update.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.1.1.4, Mar 28, 2016
 * @since 0.2.0
 */
@Named
@Singleton
public class UpdateProfilesValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

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

        final String userURL = requestJSONObject.optString(User.USER_URL);
        if (!Strings.isEmptyOrNull(userURL) && invalidUserURL(userURL)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    "URL" + langPropsService.get("colonLabel") + langPropsService.get("invalidUserURLLabel")));
        }

        final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
        if (!Strings.isEmptyOrNull(userQQ) && (!Strings.isNumeric(userQQ) || userQQ.length() > MAX_USER_QQ_LENGTH)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    langPropsService.get("invalidUserQQLabel")));
        }

        final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);
        if (!Strings.isEmptyOrNull(userIntro) && userIntro.length() > MAX_USER_INTRO_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserIntroLabel")));
        }

        final int userCommentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        if (userCommentViewMode != UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME
                && userCommentViewMode != UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL) {
            requestJSONObject.put(UserExt.USER_COMMENT_VIEW_MODE, UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL);
        }

        final String tagErrMsg = langPropsService.get("selfTagLabel") + langPropsService.get("colonLabel")
                + langPropsService.get("tagsErrorLabel");

        String userTags = requestJSONObject.optString(UserExt.USER_TAGS);
        if (!Strings.isEmptyOrNull(userTags)) {
            userTags = Tag.formatTags(userTags);
            String[] tagTitles = userTags.split(",");
            if (null == tagTitles || 0 == tagTitles.length) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
            }

            tagTitles = new LinkedHashSet<String>(Arrays.asList(tagTitles)).toArray(new String[0]);

            final StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i].trim();

                if (Strings.isEmptyOrNull(tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                if (Strings.isEmptyOrNull(tagTitle) || tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH || tagTitle.length() < 1) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
                if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                            langPropsService.get("selfTagLabel") + langPropsService.get("colonLabel")
                            + langPropsService.get("articleTagReservedLabel") + " [" + tagTitle + "]"));
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }

            requestJSONObject.put(UserExt.USER_TAGS, tagBuilder.toString());
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
