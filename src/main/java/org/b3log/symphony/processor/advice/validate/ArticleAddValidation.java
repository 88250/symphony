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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Validates for article adding locally.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.3.7, Apr 5, 2016
 * @since 0.2.0
 */
@Named
@Singleton
public class ArticleAddValidation extends BeforeRequestProcessAdvice {

    /**
     * Max article title length.
     */
    public static final int MAX_ARTICLE_TITLE_LENGTH = 255;

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

        validateArticleFields(request, requestJSONObject);
    }

    /**
     * Validates article fields.
     *
     * @param request the specified HTTP servlet request
     * @param requestJSONObject the specified request object
     * @throws RequestProcessAdviceException if validate failed
     */
    public static void validateArticleFields(final HttpServletRequest request,
            final JSONObject requestJSONObject) throws RequestProcessAdviceException {
        final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsServiceImpl.class);
        final TagQueryService tagQueryService = beanManager.getReference(TagQueryService.class);
        final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);

        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        articleTitle = StringUtils.trim(articleTitle);
        if (Strings.isEmptyOrNull(articleTitle) || articleTitle.length() > MAX_ARTICLE_TITLE_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTitleErrorLabel")));
        }
        if (optionQueryService.containReservedWord(articleTitle)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        requestJSONObject.put(Article.ARTICLE_TITLE, articleTitle);

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE);
        if (Article.isInvalidArticleType(articleType)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTypeErrorLabel")));
        }

        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        articleTags = Tag.formatTags(articleTags);

        if (optionQueryService.containReservedWord(articleTags)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        if (StringUtils.isNotBlank(articleTags)) {
            String[] tagTitles = articleTags.split(",");

            tagTitles = new LinkedHashSet<String>(Arrays.asList(tagTitles)).toArray(new String[0]);
            final List<String> invalidTags = tagQueryService.getInvalidTags();

            final StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i].trim();

                if (Strings.isEmptyOrNull(tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                }

                if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                }

                if (Strings.isEmptyOrNull(tagTitle) || tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH || tagTitle.length() < 1) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                }

                final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
                if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagReservedLabel")
                            + " [" + tagTitle + "]"));
                }

                if (invalidTags.contains(tagTitle)) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("articleTagInvalidLabel")
                            + " [" + tagTitle + "]"));
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }
            requestJSONObject.put(Article.ARTICLE_TAGS, tagBuilder.toString());
        }

        String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        articleContent = StringUtils.strip(articleContent);
        if (Strings.isEmptyOrNull(articleContent) || articleContent.length() > MAX_ARTICLE_CONTENT_LENGTH
                || articleContent.length() < MIN_ARTICLE_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG,
                    langPropsService.get("articleContentErrorLabel")));
        }

        if (optionQueryService.containReservedWord(articleContent)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("invalidRewardPointLabel")));
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
