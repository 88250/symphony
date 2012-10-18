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
package org.b3log.symphony.processor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.QueryResults;
import org.json.JSONObject;

/**
 * Comment processor.
 *
 * <ul> 
 *   <li>Adds a comment (/comment) <em>locally</em>, PUT</li> 
 *   <li>Adds a comment (/solo/comment) <em>remotely</em>, PUT</li> 
 * </ul> 
 *
 * <p> 
 * The '<em>locally</em>' means user post a comment on Symphony directly rather than receiving a comment from externally (for example
 * Solo). 
 * </p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 7, 2012
 * @since 0.2.0
 */
@RequestProcessor
public final class CommentProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentProcessor.class.getName());
    /**
     * User management service.
     */
    private UserMgmtService userMgmtService = UserMgmtService.getInstance();
    /**
     * Article management service.
     */
    private ArticleMgmtService articleMgmtService = ArticleMgmtService.getInstance();
    /**
     * Article query service.
     */
    private ArticleQueryService articleQueryService = ArticleQueryService.getInstance();
    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();
    /**
     * Comment management service.
     */
    private CommentMgmtService commentMgmtService = CommentMgmtService.getInstance();
    /**
     * Comment query service.
     */
    private CommentQueryService commentQueryService = CommentQueryService.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Adds a comment locally.
     *
     * <p>
     * The request json object (a comment): 
     * <pre>
     * {
     *     "articleId": "",
     *     "commentContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     * @throws ServletException servlet exception 
     */
    @RequestProcessing(value = "/comment", method = HTTPRequestMethod.PUT)
    public void addComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String articleId = requestJSONObject.optString(Common.ARTICLE_ID);
        final String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);

        // TODO: add comment validate

        final JSONObject comment = new JSONObject();
        comment.put(Comment.COMMENT_CONTENT, commentContent);
        comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);

        final JSONObject currentUser = LoginProcessor.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        comment.put(Comment.COMMENT_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

        final String authorEmail = currentUser.optString(User.USER_EMAIL);
        comment.put(Comment.COMMENT_AUTHOR_EMAIL, authorEmail);

        try {
            commentMgmtService.addComment(comment);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.SEVERE, msg, e);

            ret.put(Keys.MSG, msg);
        }
    }

    /**
     * Adds a comment remotely.
     * 
     * <p>
     * The request json object (a comment): 
     * <pre>
     * {
     *     "comment": {
     *         "articleId": "",
     *         "commentContent": "",
     *         "commentAuthorName": "",
     *         "commentAuthorEmail": ""
     *     },
     *     "blogTitle": "",
     *     "blogVersion": "",
     *     "blogHost": "",
     *     "runtimeEnv": "" // GAE, BAE, LOCAL
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/solo/comment", method = HTTPRequestMethod.PUT)
    public void addCommentFromSolo(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final String requestURI = request.getRequestURI();

        final String userName = requestURI.substring("/home/".length());

    }
}
