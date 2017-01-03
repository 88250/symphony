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
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.json.JSONObject;

/**
 * Validates for comment adding locally.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.1, Aug 28, 2016
 * @since 0.2.0
 */
@Named
@Singleton
public class CommentAddValidation extends BeforeRequestProcessAdvice {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Max comment content length.
     */
    public static final int MAX_COMMENT_CONTENT_LENGTH = 2000;

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

        final String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);
        if (Strings.isEmptyOrNull(commentContent) || commentContent.length() > MAX_COMMENT_CONTENT_LENGTH) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("commentErrorLabel")));
        }

        if (optionQueryService.containReservedWord(commentContent)) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("contentContainReservedWordLabel")));
        }

        try {
            final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
            if (Strings.isEmptyOrNull(articleId)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            }

            final JSONObject article = articleQueryService.getArticleById(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId);
            if (null == article) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
            }

            if (!article.optBoolean(Article.ARTICLE_COMMENTABLE)) {
                throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("notAllowCmtLabel")));
            }

            final String originalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            if (StringUtils.isNotBlank(originalCommentId)) {
                final JSONObject originalCmt = commentQueryService.getComment(originalCommentId);
                if (null == originalCmt) {
                    throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, langPropsService.get("commentArticleErrorLabel")));
                }
            }
        } catch (final ServiceException e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, "Unknown Error"));
        }
    }
}
