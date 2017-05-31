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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ClientCommentAddValidation;
import org.b3log.symphony.processor.advice.validate.CommentAddValidation;
import org.b3log.symphony.processor.advice.validate.CommentUpdateValidation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Comment processor.
 * <p>
 * <ul>
 * <li>Adds a comment (/comment) <em>locally</em>, POST</li>
 * <li>Updates a comment (/comment/{id}) <em>locally</em>, PUT</li>
 * <li>Gets a comment's content (/comment/{id}/content), GET</li>
 * <li>Adds a comment (/solo/comment) <em>remotely</em>, POST</li>
 * <li>Thanks a comment (/comment/thank), POST</li>
 * <li>Gets a comment's replies (/comment/replies), GET </li>
 * <li>Gets a comment's revisions (/commment/{id}/revisions), GET</li>
 * <li>Removes a comment (/comment/{id}/remove), POST</li>
 * </ul>
 * </p>
 * <p>
 * The '<em>locally</em>' means user post a comment on Symphony directly rather than receiving a comment from externally
 * (for example Solo).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.1.13, May 8, 2017
 * @since 0.2.0
 */
@RequestProcessor
public class CommentProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentProcessor.class);

    /**
     * Revision query service.
     */
    @Inject
    private RevisionQueryService revisionQueryService;

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
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

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
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Reward query service.
     */
    @Inject
    private RewardQueryService rewardQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Removes a comment.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/comment/{id}/remove", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void removeComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject comment = commentQueryService.getComment(id);
        if (null == comment) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final String authorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
        if (!authorId.equals(currentUserId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON();
        try {
            commentMgmtService.removeComment(id);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
            context.renderJSONValue(Comment.COMMENT_T_ID, id);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Gets a comment's revisions.
     *
     * @param context the specified context
     * @param id      the specified comment id
     */
    @RequestProcessing(value = "/comment/{id}/revisions", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getCommentRevisions(final HTTPRequestContext context, final String id) {
        final List<JSONObject> revisions = revisionQueryService.getCommentRevisions(id);
        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Revision.REVISIONS, (Object) revisions);

        context.renderJSON(ret);
    }

    /**
     * Gets a comment's content.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/comment/{id}/content", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {LoginCheck.class})
    public void getCommentContent(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                                  final String id) throws IOException {
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

        try {
            final JSONObject comment = commentQueryService.getComment(id);
            if (null == comment) {
                LOGGER.warn("Not found comment [id=" + id + "] to update");

                return;
            }

            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.optString(Comment.COMMENT_AUTHOR_ID))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            context.renderJSONValue(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Updates a comment locally.
     * <p>
     * The request json object:
     * <pre>
     * {
     *     "commentContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws IOException io exception
     */
    @RequestProcessing(value = "/comment/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {CSRFCheck.class, LoginCheck.class, CommentUpdateValidation.class, PermissionCheck.class})
    public void updateComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws IOException {
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

        try {
            final JSONObject comment = commentQueryService.getComment(id);
            if (null == comment) {
                LOGGER.warn("Not found comment [id=" + id + "] to update");

                return;
            }

            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
            if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.optString(Comment.COMMENT_AUTHOR_ID))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

            String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);
            final String ip = Requests.getRemoteAddr(request);
            final String ua = request.getHeader(Common.USER_AGENT);

            comment.put(Comment.COMMENT_CONTENT, commentContent);
            comment.put(Comment.COMMENT_IP, "");
            if (StringUtils.isNotBlank(ip)) {
                comment.put(Comment.COMMENT_IP, ip);
            }
            comment.put(Comment.COMMENT_UA, "");
            if (StringUtils.isNotBlank(ua)) {
                comment.put(Comment.COMMENT_UA, ua);
            }

            commentMgmtService.updateComment(comment.optString(Keys.OBJECT_ID), comment);

            commentContent = comment.optString(Comment.COMMENT_CONTENT);
            commentContent = shortLinkQueryService.linkArticle(commentContent);
            commentContent = shortLinkQueryService.linkTag(commentContent);
            commentContent = Emotions.toAliases(commentContent);
            commentContent = Emotions.convert(commentContent);
            commentContent = Markdowns.toHTML(commentContent);
            commentContent = Markdowns.clean(commentContent, "");
            commentContent = MP3Players.render(commentContent);
            commentContent = VideoPlayers.render(commentContent);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
            context.renderJSONValue(Comment.COMMENT_CONTENT, commentContent);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Gets a comment's original comment.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/comment/original", method = HTTPRequestMethod.POST)
    public void getOriginalComment(final HTTPRequestContext context,
                                   final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        int avatarViewMode = UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null != currentUser) {
            avatarViewMode = currentUser.optInt(UserExt.USER_AVATAR_VIEW_MODE);
        }

        final JSONObject originalCmt = commentQueryService.getOriginalComment(avatarViewMode, commentViewMode, commentId);

        // Fill thank
        final String originalCmtId = originalCmt.optString(Keys.OBJECT_ID);

        if (null != currentUser) {
            originalCmt.put(Common.REWARDED,
                    rewardQueryService.isRewarded(currentUser.optString(Keys.OBJECT_ID),
                            originalCmtId, Reward.TYPE_C_COMMENT));
        }

        originalCmt.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(originalCmtId, Reward.TYPE_C_COMMENT));

        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, (Object) originalCmt);
    }

    /**
     * Gets a comment's replies.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/comment/replies", method = HTTPRequestMethod.POST)
    public void getReplies(final HTTPRequestContext context,
                           final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        int avatarViewMode = UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL;
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null != currentUser) {
            avatarViewMode = currentUser.optInt(UserExt.USER_AVATAR_VIEW_MODE);
        }

        final List<JSONObject> replies = commentQueryService.getReplies(avatarViewMode, commentViewMode, commentId);

        // Fill reply thank
        for (final JSONObject reply : replies) {
            final String replyId = reply.optString(Keys.OBJECT_ID);

            if (null != currentUser) {
                reply.put(Common.REWARDED,
                        rewardQueryService.isRewarded(currentUser.optString(Keys.OBJECT_ID),
                                replyId, Reward.TYPE_C_COMMENT));
            }

            reply.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(replyId, Reward.TYPE_C_COMMENT));
        }

        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, (Object) replies);
    }

    /**
     * Adds a comment locally.
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
     * @throws IOException      io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/comment", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {CSRFCheck.class, CommentAddValidation.class, PermissionCheck.class})
    public void addComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

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
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Thanks a comment.
     * <p>
     * The request json object:
     * <pre>
     * {
     *     "commentId": "",
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws IOException      io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/comment/thank", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    public void thankComment(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON();

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
            request.setAttribute(Keys.REQUEST, requestJSONObject);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Thank comment error", e);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);

        try {
            commentMgmtService.thankComment(commentId, currentUser.optString(Keys.OBJECT_ID));

            context.renderTrueResult().renderMsg(langPropsService.get("thankSentLabel"));
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Adds a comment remotely.
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
     *     "clientRuntimeEnv": "" // LOCAL
     *     "clientAdminEmail": "",
     *     "userB3Key": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
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
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader(Common.USER_AGENT);

        final JSONObject defaultCommenter = userQueryService.getDefaultCommenter();
        final JSONObject comment = new JSONObject();
        comment.put(Comment.COMMENT_AUTHOR_ID, defaultCommenter.optString(Keys.OBJECT_ID));
        comment.put(Comment.COMMENT_CLIENT_COMMENT_ID, originalCmt.optString(Comment.COMMENT_T_ID));
        comment.put(Comment.COMMENT_CONTENT, originalCmt.optString(Comment.COMMENT_CONTENT));
        comment.put(Comment.COMMENT_ON_ARTICLE_ID, article.optString(Keys.OBJECT_ID));
        comment.put(Comment.COMMENT_T_COMMENTER, defaultCommenter);
        comment.put(Comment.COMMENT_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            comment.put(Comment.COMMENT_IP, ip);
        }
        comment.put(Comment.COMMENT_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            comment.put(Comment.COMMENT_UA, ua);
        }

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
