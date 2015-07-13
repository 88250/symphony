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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.ArrayUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for article adding locally.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.6, Jul 13, 2015
 * @since 0.2.0
 */
@Named
@Singleton
public class ArticleAddValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private static LangPropsService langPropsService;

    /**
     * Max article title length.
     */
    public static final int MAX_ARTICLE_TITLE_LENGTH = 255;

    /**
     * Max tag title length.
     */
    public static final int MAX_TAG_TITLE_LENGTH = 50;

    /**
     * Max article content length.
     */
    public static final int MAX_ARTICLE_CONTENT_LENGTH = 1048576;

    /**
     * Min article content length.
     */
    public static final int MIN_ARTICLE_CONTENT_LENGTH = 4;

    /**
     * Max article reward content length.
     */
    public static final int MAX_ARTICLE_REWARD_CONTENT_LENGTH = 1048576;

    /**
     * Min article reward content length.
     */
    public static final int MIN_ARTICLE_REWARD_CONTENT_LENGTH = 4;

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

        validateArticleFields(requestJSONObject);
    }

    /**
     * Validates article fields.
     *
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException if validate failed
     */
    public static void validateArticleFields(final JSONObject requestJSONObject) throws RequestProcessAdviceException {
        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        if (Strings.isEmptyOrNull(articleTitle) || articleTitle.length() > MAX_ARTICLE_TITLE_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTitleErrorLabel")));
        }

        final String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        if (Strings.isEmptyOrNull(articleTags)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
        }

        String[] tagTitles = articleTags.split(",");
        if (null == tagTitles || 0 == tagTitles.length) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
        }

        tagTitles = new TreeSet<String>(Arrays.asList(tagTitles)).toArray(new String[0]);

        final StringBuilder tagBuilder = new StringBuilder();
        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i].trim();
            if (Strings.isEmptyOrNull(tagTitle)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
            }

            if (Strings.isEmptyOrNull(tagTitle) || tagTitle.length() > MAX_TAG_TITLE_LENGTH || tagTitle.length() < 1) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagsErrorLabel")));
            }

            if (ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagReservedLabel")
                        + " [" + tagTitle + "]"));
            }

            tagBuilder.append(tagTitle).append(",");
        }
        if (tagBuilder.length() > 0) {
            tagBuilder.deleteCharAt(tagBuilder.length() - 1);
        }
        requestJSONObject.put(Article.ARTICLE_TAGS, tagBuilder.toString());

        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        if (Strings.isEmptyOrNull(articleContent) || articleContent.length() > MAX_ARTICLE_CONTENT_LENGTH
                || articleContent.length() < MIN_ARTICLE_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    langPropsService.get("articleContentErrorLabel")));
        }

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "invalidRewardPointLabel"));
        }

        if (rewardPoint > 0) {
            final String articleRewardContnt = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
            if (Strings.isEmptyOrNull(articleRewardContnt) || articleRewardContnt.length() > MAX_ARTICLE_CONTENT_LENGTH
                    || articleRewardContnt.length() < MIN_ARTICLE_CONTENT_LENGTH) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                        langPropsService.get("articleRewardContentErrorLabel")));
            }
        }
    }
}
