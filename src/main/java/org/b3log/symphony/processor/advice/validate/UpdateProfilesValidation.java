/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.processor.advice.validate;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Validates for user profiles update.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.2.2.5, Sep 6, 2016
 * @since 0.2.0
 */
@Singleton
public class UpdateProfilesValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Max user nickname length.
     */
    public static final int MAX_USER_NICKNAME_LENGTH = 20;

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
        if (StringUtils.isNotBlank(userURL) && invalidUserURL(userURL)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    "URL" + langPropsService.get("colonLabel") + langPropsService.get("invalidUserURLLabel")));
        }

        final String userQQ = requestJSONObject.optString(UserExt.USER_QQ);
        if (StringUtils.isNotBlank(userQQ) && (!Strings.isNumeric(userQQ) || userQQ.length() > MAX_USER_QQ_LENGTH)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    langPropsService.get("invalidUserQQLabel")));
        }

        final String userNickname = requestJSONObject.optString(UserExt.USER_NICKNAME);
        if (StringUtils.isNotBlank(userNickname) && userNickname.length() > MAX_USER_NICKNAME_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidUserNicknameLabel")));
        }

        final String userIntro = requestJSONObject.optString(UserExt.USER_INTRO);
        if (StringUtils.isNotBlank(userIntro) && userIntro.length() > MAX_USER_INTRO_LENGTH) {
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
        if (StringUtils.isNotBlank(userTags)) {
            userTags = Tag.formatTags(userTags);
            String[] tagTitles = userTags.split(",");
            if (null == tagTitles || 0 == tagTitles.length) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
            }

            tagTitles = new LinkedHashSet<>(Arrays.asList(tagTitles)).toArray(new String[0]);

            final StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i].trim();

                if (StringUtils.isBlank(tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                if (Tag.containsWhiteListTags(tagTitle)) {
                    tagBuilder.append(tagTitle).append(",");

                    continue;
                }

                if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                if (tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, tagErrMsg));
                }

                final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
                if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
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
        if (!Strings.isURL(userURL)) {
            return true;
        }

        return userURL.length() > MAX_USER_URL_LENGTH;
    }
}
