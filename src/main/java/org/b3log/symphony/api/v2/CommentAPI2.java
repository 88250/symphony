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
package org.b3log.symphony.api.v2;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.processor.advice.validate.CommentAddValidation;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Comment API v2.
 * <p>
 * <ul>
 * <li>Adds a comment (/api/v2/comment), POST</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 9, 2016
 * @since 2.1.0
 */
@RequestProcessor
public class CommentAPI2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentAPI2.class);
    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;
    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;
    /**
     * Comment management service.
     */
    @Inject
    private CommentMgmtService commentMgmtService;

    /**
     * Adds a comment.
     * <p>
     * The request json object (a comment):
     * <pre>
     * {
     *     "articleId": "",
     *     "commentContent": "",
     *     "commentAnonymous": boolean,
     *     "commentOriginalCommentId": "", // optional
     *     "userCommentViewMode": int
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/api/v2/comment", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {CommentAddValidation.class, PermissionCheck.class})
    public void addComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        final String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);
        final String commentOriginalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
        final int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader(Common.USER_AGENT);

        final boolean isAnonymous = requestJSONObject.optBoolean(Comment.COMMENT_ANONYMOUS, false);

        final JSONObject comment = new JSONObject();
        comment.put(Comment.COMMENT_CONTENT, commentContent);
        comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
        comment.put(UserExt.USER_COMMENT_VIEW_MODE, commentViewMode);
        comment.put(Comment.COMMENT_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            comment.put(Comment.COMMENT_IP, ip);
        }
        comment.put(Comment.COMMENT_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            comment.put(Comment.COMMENT_UA, ua);
        }
        comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, commentOriginalCommentId);

        try {
            final JSONObject currentUser = userQueryService.getCurrentUser(request);
            if (null == currentUser) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            final String currentUserName = currentUser.optString(User.USER_NAME);
            final JSONObject article = articleQueryService.getArticle(articleId);
            final String articleContent = article.optString(Article.ARTICLE_CONTENT);
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final Set<String> userNames = userQueryService.getUserNames(articleContent);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                    && !articleAuthorName.equals(currentUserName)) {
                boolean invited = false;
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);

                    return;
                }
            }

            comment.put(Comment.COMMENT_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            comment.put(Comment.COMMENT_T_COMMENTER, currentUser);
            comment.put(Comment.COMMENT_ANONYMOUS, isAnonymous
                    ? Comment.COMMENT_ANONYMOUS_C_ANONYMOUS : Comment.COMMENT_ANONYMOUS_C_PUBLIC);

            commentMgmtService.addComment(comment);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }
}
