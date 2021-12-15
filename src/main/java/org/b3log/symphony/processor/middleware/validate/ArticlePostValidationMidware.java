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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.TagQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Validates for article adding locally.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class ArticlePostValidationMidware {

    /**
     * Max article title length.
     */
    public static final int MAX_ARTICLE_TITLE_LENGTH = 255;

    /**
     * Max article content length.
     */
    public static final int MAX_ARTICLE_CONTENT_LENGTH = 102400;

    /**
     * Min article content length.
     */
    public static final int MIN_ARTICLE_CONTENT_LENGTH = 4;

    /**
     * Max article reward content length.
     */
    public static final int MAX_ARTICLE_REWARD_CONTENT_LENGTH = 102400;

    /**
     * Min article reward content length.
     */
    public static final int MIN_ARTICLE_REWARD_CONTENT_LENGTH = 4;

    public void handle(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);
        final TagQueryService tagQueryService = beanManager.getReference(TagQueryService.class);
        final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);

        final JSONObject exception = new JSONObject();
        exception.put(Keys.CODE, StatusCodes.ERR);

        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        articleTitle = StringUtils.trim(articleTitle);
        articleTitle = Emotions.clear(articleTitle);
        if (StringUtils.isBlank(articleTitle) || articleTitle.length() > MAX_ARTICLE_TITLE_LENGTH) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("articleTitleErrorLabel")));
            context.abort();
            return;
        }

        if (optionQueryService.containReservedWord(articleTitle)) {
            final String msg = langPropsService.get("contentContainReservedWordLabel");
            context.renderJSON(new JSONObject().put(Keys.MSG, msg));
            context.abort();
            return;
        }

        requestJSONObject.put(Article.ARTICLE_TITLE, articleTitle);

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE);
        if (Article.isInvalidArticleType(articleType)) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("articleTypeErrorLabel")));
            context.abort();
            return;
        }

        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        articleTags = Tag.formatTags(articleTags);

        if (StringUtils.isBlank(articleTags)) {
            // 发帖时标签改为非必填 https://github.com/b3log/symphony/issues/811
            articleTags = "待分类";
        }

        if (StringUtils.isBlank(articleTags)) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("tagsEmptyErrorLabel")));
            context.abort();
            return;
        }
        if (optionQueryService.containReservedWord(articleTags)) {
            final String msg = langPropsService.get("contentContainReservedWordLabel");
            context.renderJSON(new JSONObject().put(Keys.MSG, msg));
            context.abort();
            return;
        }

        if (StringUtils.isNotBlank(articleTags)) {
            String[] tagTitles = articleTags.split(",");

            tagTitles = new LinkedHashSet<>(Arrays.asList(tagTitles)).toArray(new String[0]);
            final List<String> invalidTags = tagQueryService.getInvalidTags();

            final StringBuilder tagBuilder = new StringBuilder();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i].trim();

                if (StringUtils.isBlank(tagTitle)) {
                    context.renderJSON(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                    context.abort();
                    return;
                }

                if (!Tag.containsWhiteListTags(tagTitle)) {
                    if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                        context.renderJSON(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                        context.abort();
                        return;
                    }

                    if (tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                        context.renderJSON(exception.put(Keys.MSG, langPropsService.get("tagsErrorLabel")));
                        context.abort();
                        return;
                    }
                }

                final JSONObject currentUser = Sessions.getUser();
                if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    context.renderJSON(exception.put(Keys.MSG, langPropsService.get("articleTagReservedLabel") + " [" + tagTitle + "]"));
                    context.abort();
                    return;
                }

                if (invalidTags.contains(tagTitle)) {
                    continue;
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }
            requestJSONObject.put(Article.ARTICLE_TAGS, tagBuilder.toString());
        }

        String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        articleContent = StringUtils.trim(articleContent);
        if (StringUtils.isBlank(articleContent) || articleContent.length() > MAX_ARTICLE_CONTENT_LENGTH
                || articleContent.length() < MIN_ARTICLE_CONTENT_LENGTH) {
            String msg = langPropsService.get("articleContentErrorLabel");
            msg = msg.replace("{maxArticleContentLength}", String.valueOf(MAX_ARTICLE_CONTENT_LENGTH));

            context.renderJSON(exception.put(Keys.MSG, msg));
            context.abort();
            return;
        }

        if (optionQueryService.containReservedWord(articleContent)) {
            final String msg = langPropsService.get("contentContainReservedWordLabel");
            context.renderJSON(new JSONObject().put(Keys.MSG, msg));
            context.abort();
            return;
        }

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("invalidRewardPointLabel")));
            context.abort();
            return;
        }

        final int articleQnAOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT, 0);
        if (articleQnAOfferPoint < 0) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("invalidQnAOfferPointLabel")));
            context.abort();
            return;
        }

        final String articleRewardContnt = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        if (StringUtils.isNotBlank(articleRewardContnt) && 1 > rewardPoint) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("invalidRewardPointLabel")));
            context.abort();
            return;
        }

        if (rewardPoint > 0) {
            if (articleRewardContnt.length() > MAX_ARTICLE_CONTENT_LENGTH || StringUtils.length(articleRewardContnt) < 4) {
                String msg = langPropsService.get("articleRewardContentErrorLabel");
                msg = msg.replace("{maxArticleRewardContentLength}", String.valueOf(MAX_ARTICLE_REWARD_CONTENT_LENGTH));

                context.renderJSON(exception.put(Keys.MSG, msg));
                context.abort();
                return;
            }
        }

        context.handle();
    }
}
