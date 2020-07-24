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
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

/**
 * Validates for comment adding locally.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class CommentAddValidationMidware {

    public void handle(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        request.setAttribute(Keys.REQUEST, requestJSONObject);
        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);
        final OptionQueryService optionQueryService = beanManager.getReference(OptionQueryService.class);
        final ArticleQueryService articleQueryService = beanManager.getReference(ArticleQueryService.class);
        final CommentQueryService commentQueryService = beanManager.getReference(CommentQueryService.class);

        final JSONObject exception = new JSONObject();
        exception.put(Keys.CODE, StatusCodes.ERR);

        final String commentContent = StringUtils.trim(requestJSONObject.optString(Comment.COMMENT_CONTENT));
        if (StringUtils.isBlank(commentContent) || commentContent.length() > Comment.MAX_COMMENT_CONTENT_LENGTH) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("commentErrorLabel")));
            context.abort();
            return;
        }

        if (optionQueryService.containReservedWord(commentContent)) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
            context.abort();
            return;
        }

        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            context.abort();
            return;
        }

        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (null == article) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            context.abort();
            return;
        }

        if (!article.optBoolean(Article.ARTICLE_COMMENTABLE)) {
            context.renderJSON(exception.put(Keys.MSG, langPropsService.get("notAllowCmtLabel")));
            context.abort();
            return;
        }

        final String originalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
        if (StringUtils.isNotBlank(originalCommentId)) {
            final JSONObject originalCmt = commentQueryService.getComment(originalCommentId);
            if (null == originalCmt) {
                context.renderJSON(exception.put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
                context.abort();
                return;
            }
        }

        context.handle();
    }
}
