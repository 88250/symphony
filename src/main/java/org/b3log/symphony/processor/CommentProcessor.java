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
package org.b3log.symphony.processor;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
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
import org.b3log.symphony.service.ClientMgmtService;
import org.b3log.symphony.service.ClientQueryService;
import org.b3log.symphony.service.CommentMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.QueryResults;
import org.json.JSONObject;

/**
 * Comment processor.
 *
 * <ul>
 * <li>Adds a comment (/comment) <em>locally</em>, POST</li>
 * <li>Adds a comment (/solo/comment) <em>remotely</em>, POST</li>
 * </ul>
 *
 * <p>
 * The '<em>locally</em>' means user post a comment on Symphony directly rather than receiving a comment from externally (for example
 * Solo).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.8, Apr 17, 2015
 * @since 0.2.0
 */
@RequestProcessor
public class CommentProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentProcessor.class.getName());

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
     * Client management service.
     */
    @Inject
    private ClientMgmtService clientMgmtService;

    /**
     * Client query service.
     */
    @Inject
    private ClientQueryService clientQueryService;

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
    @RequestProcessing(value = "/comment", method = HTTPRequestMethod.POST)
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

        final JSONObject comment = new JSONObject();
        comment.put(Comment.COMMENT_CONTENT, commentContent);
        comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);

        try {
            final JSONObject currentUser = userQueryService.getCurrentUser(request);
            if (null == currentUser) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            comment.put(Comment.COMMENT_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            final String authorEmail = currentUser.optString(User.USER_EMAIL);
            comment.put(Comment.COMMENT_AUTHOR_EMAIL, authorEmail);

            comment.put(Comment.COMMENT_T_COMMENTER, currentUser);

            commentMgmtService.addComment(comment);
            ret.put(Keys.STATUS_CODE, true);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, msg);

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
    @RequestProcessing(value = "/solo/comment", method = HTTPRequestMethod.POST)
    @Before(adviceClass = ClientCommentAddValidation.class)
    public void addCommentFromSolo(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        LOGGER.log(Level.DEBUG, "Adds a comment from solo");

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
        comment.put(Comment.COMMENT_T_COMMENTER, defaultCommenter);

        comment.put(Comment.COMMENT_T_AUTHOR_NAME, originalCmt.optString(Comment.COMMENT_T_AUTHOR_NAME));

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

        LOGGER.log(Level.DEBUG, "Added a comment from solo");
    }
}
