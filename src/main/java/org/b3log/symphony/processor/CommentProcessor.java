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
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.processor.advice.validate.ClientCommentAddValidation;
import org.b3log.symphony.processor.advice.validate.CommentAddValidation;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.ClientMgmtService;
import org.b3log.symphony.service.ClientQueryService;
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
     * Client management service.
     */
    private ClientMgmtService clientMgmtService = ClientMgmtService.getInstance();
    /**
     * Client query service.
     */
    private ClientQueryService clientQueryService = ClientQueryService.getInstance();
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
    @Before(adviceClass = CommentAddValidation.class)
    public void addComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject ret = QueryResults.falseResult();
        renderer.setJSONObject(ret);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
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
        
        comment.put(Comment.COMMENT_T_COMMENTER, currentUser);
 
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
     *         "commentId": "", // client comment id
     *         "articleId": "",
     *         "commentContent": "",
     *         "commentAuthorName": "", // optional, 'default commenter'
     *         "commentAuthorEmail": "" // optional, 'default commenter'
     *     },
     *     "clientName": "",
     *     "clientVersion": "",
     *     "clientHost": "",
     *     "clientRuntimeEnv": "" // GAE, BAE, LOCAL
     *     "clientAdminEmail": "",
     *     "userB3Key": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/solo/comment", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = ClientCommentAddValidation.class)
    public void addCommentFromSolo(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        final JSONObject originalCmt = requestJSONObject.optJSONObject(Comment.COMMENT);
        final JSONObject article = (JSONObject) request.getAttribute(Article.ARTICLE);

        final JSONObject defaultCommenter = userQueryService.getDefaultCommenter();
        final JSONObject comment = new JSONObject();
        comment.put(Comment.COMMENT_AUTHOR_EMAIL, defaultCommenter.optString(User.USER_EMAIL));
        comment.put(Comment.COMMENT_AUTHOR_ID, defaultCommenter.optString(Keys.OBJECT_ID));
        comment.put(Comment.COMMENT_CLIENT_COMMENT_ID, originalCmt.optString(Comment.COMMENT_T_ID));
        comment.put(Comment.COMMENT_CONTENT, originalCmt.optString(Comment.COMMENT_CONTENT));
        comment.put(Comment.COMMENT_ON_ARTICLE_ID, article.optString(Keys.OBJECT_ID));

        commentMgmtService.addComment(comment);

        // Updates client record
        final String clientAdminEmail = requestJSONObject.optString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.optString(Client.CLIENT_NAME);
        final String clientVersion = requestJSONObject.optString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.optString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.optString(Client.CLIENT_RUNTIME_ENV);

        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, System.currentTimeMillis());
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, 0L);

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }
}
