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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.middleware.CSRFMidware;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.PermissionMidware;
import org.b3log.symphony.processor.middleware.validate.CommentAddValidationMidware;
import org.b3log.symphony.processor.middleware.validate.CommentUpdateValidationMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Comment processor.
 * <ul>
 * <li>Adds a comment (/comment) <em>locally</em>, POST</li>
 * <li>Updates a comment (/comment/{id}) <em>locally</em>, PUT</li>
 * <li>Gets a comment's content (/comment/{id}/content), GET</li>
 * <li>Thanks a comment (/comment/thank), POST</li>
 * <li>Gets a comment's replies (/comment/replies), GET </li>
 * <li>Gets a comment's revisions (/commment/{id}/revisions), GET</li>
 * <li>Removes a comment (/comment/{id}/remove), POST</li>
 * <li>Accepts a comment (/comment/accept), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class CommentProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(CommentProcessor.class);

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
     * Follow management service.
     */
    @Inject
    private FollowMgmtService followMgmtService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);
        final CSRFMidware csrfMidware = beanManager.getReference(CSRFMidware.class);
        final CommentUpdateValidationMidware commentUpdateValidationMidware = beanManager.getReference(CommentUpdateValidationMidware.class);
        final CommentAddValidationMidware commentAddValidationMidware = beanManager.getReference(CommentAddValidationMidware.class);

        final CommentProcessor commentProcessor = beanManager.getReference(CommentProcessor.class);
        Dispatcher.post("/comment/accept", commentProcessor::acceptComment, loginCheck::handle, csrfMidware::check, permissionMidware::check);
        Dispatcher.post("/comment/{id}/remove", commentProcessor::removeComment, loginCheck::handle, permissionMidware::check);
        Dispatcher.get("/comment/{id}/revisions", commentProcessor::getCommentRevisions, loginCheck::handle, permissionMidware::check);
        Dispatcher.get("/comment/{id}/content", commentProcessor::getCommentContent, loginCheck::handle);
        Dispatcher.put("/comment/{id}", commentProcessor::updateComment, loginCheck::handle, csrfMidware::check, permissionMidware::check, commentUpdateValidationMidware::handle);
        Dispatcher.post("/comment/original", commentProcessor::getOriginalComment);
        Dispatcher.post("/comment/replies", commentProcessor::getReplies);
        Dispatcher.post("/comment", commentProcessor::addComment, loginCheck::handle, csrfMidware::check, permissionMidware::check, commentAddValidationMidware::handle);
        Dispatcher.post("/comment/thank", commentProcessor::thankComment, loginCheck::handle, csrfMidware::check, permissionMidware::check);
    }

    /**
     * Accepts a comment.
     *
     * @param context the specified context
     */
    public void acceptComment(final RequestContext context) {
        context.renderJSON();

        final JSONObject requestJSONObject = context.requestJSON();
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);

        try {
            final JSONObject comment = commentQueryService.getComment(commentId);
            if (null == comment) {
                context.renderFalseResult().renderMsg("Not found comment to accept");
                return;
            }
            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            if (StringUtils.equals(userId, commentAuthorId)) {
                context.renderFalseResult().renderMsg(langPropsService.get("thankSelfLabel"));
                return;
            }

            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final JSONObject article = articleQueryService.getArticle(articleId);
            if (!StringUtils.equals(userId, article.optString(Article.ARTICLE_AUTHOR_ID))) {
                context.renderFalseResult().renderMsg(langPropsService.get("sc403Label"));
                return;
            }

            commentMgmtService.acceptComment(commentId);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Removes a comment.
     *
     * @param context the specified context
     */
    public void removeComment(final RequestContext context) {
        final String id = context.pathVar("id");
        if (StringUtils.isBlank(id)) {
            context.sendError(404);
            return;
        }

        final JSONObject currentUser = Sessions.getUser();
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject comment = commentQueryService.getComment(id);
        if (null == comment) {
            context.sendError(404);
            return;
        }

        final String authorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
        if (!authorId.equals(currentUserId)) {
            context.sendError(403);
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
     */
    public void getCommentRevisions(final RequestContext context) {
        final String id = context.pathVar("id");
        final JSONObject viewer = Sessions.getUser();
        final List<JSONObject> revisions = revisionQueryService.getCommentRevisions(viewer, id);
        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Revision.REVISIONS, (Object) revisions);

        context.renderJSON(ret);
    }

    /**
     * Gets a comment's content.
     *
     * @param context the specified context
     */
    public void getCommentContent(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

        final JSONObject comment = commentQueryService.getComment(id);
        if (null == comment) {
            LOGGER.warn("Not found comment [id=" + id + "] to update");
            return;
        }

        final JSONObject currentUser = Sessions.getUser();
        if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.optString(Comment.COMMENT_AUTHOR_ID))) {
            context.sendError(403);
            return;
        }

        context.renderJSONValue(Comment.COMMENT_CONTENT, comment.optString(Comment.COMMENT_CONTENT));
        context.renderJSONValue(Comment.COMMENT_VISIBLE, comment.optInt(Comment.COMMENT_VISIBLE));
        context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
    }

    /**
     * Updates a comment locally.
     * <p>
     * The request json object:
     * <pre>
     * {
     *     "commentContent": "",
     *     "commentVisible": boolean
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void updateComment(final RequestContext context) {
        final String id = context.pathVar("id");
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

        final Request request = context.getRequest();

        try {
            final JSONObject comment = commentQueryService.getComment(id);
            if (null == comment) {
                LOGGER.warn("Not found comment [id=" + id + "] to update");
                return;
            }

            final JSONObject currentUser = Sessions.getUser();
            if (!currentUser.optString(Keys.OBJECT_ID).equals(comment.optString(Comment.COMMENT_AUTHOR_ID))) {
                context.sendError(403);
                return;
            }

            final JSONObject requestJSONObject = (JSONObject) context.attr(Keys.REQUEST);

            String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);
            final boolean isOnlyAuthorVisible = requestJSONObject.optBoolean(Comment.COMMENT_VISIBLE);
            final String ip = Requests.getRemoteAddr(request);
            final String ua = Headers.getHeader(request, Common.USER_AGENT, "");

            comment.put(Comment.COMMENT_CONTENT, commentContent);
            comment.put(Comment.COMMENT_IP, "");
            if (StringUtils.isNotBlank(ip)) {
                comment.put(Comment.COMMENT_IP, ip);
            }
            comment.put(Comment.COMMENT_UA, "");
            if (StringUtils.isNotBlank(ua)) {
                comment.put(Comment.COMMENT_UA, ua);
            }
            comment.put(Comment.COMMENT_VISIBLE, isOnlyAuthorVisible
                    ? Comment.COMMENT_VISIBLE_C_AUTHOR : Comment.COMMENT_VISIBLE_C_ALL);

            commentMgmtService.updateComment(comment.optString(Keys.OBJECT_ID), comment);

            commentContent = comment.optString(Comment.COMMENT_CONTENT);
            commentContent = shortLinkQueryService.linkArticle(commentContent);
            commentContent = Emotions.toAliases(commentContent);
            commentContent = Emotions.convert(commentContent);
            commentContent = Markdowns.toHTML(commentContent);
            commentContent = Markdowns.clean(commentContent, "");
            commentContent = AudioPlayers.render(commentContent);
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
     * @param context the specified context
     */
    public void getOriginalComment(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final JSONObject currentUser = Sessions.getUser();
        String currentUserId = null;
        if (null != currentUser) {
            currentUserId = currentUser.optString(Keys.OBJECT_ID);
        }

        final JSONObject originalCmt = commentQueryService.getOriginalComment(currentUserId, commentViewMode, commentId);

        // Fill thank
        final String originalCmtId = originalCmt.optString(Keys.OBJECT_ID);

        if (null != currentUser) {
            originalCmt.put(Common.REWARDED,
                    rewardQueryService.isRewarded(currentUser.optString(Keys.OBJECT_ID),
                            originalCmtId, Reward.TYPE_C_COMMENT));
        }

        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, originalCmt);
    }

    /**
     * Gets a comment's replies.
     *
     * @param context the specified context
     */
    public void getReplies(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);
        int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final JSONObject currentUser = Sessions.getUser();
        String currentUserId = null;
        if (null != currentUser) {
            currentUserId = currentUser.optString(Keys.OBJECT_ID);
        }

        if (StringUtils.isBlank(commentId)) {
            context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, Collections.emptyList());
            return;
        }

        final List<JSONObject> replies = commentQueryService.getReplies(currentUserId, commentViewMode, commentId);

        // Fill reply thank
        for (final JSONObject reply : replies) {
            final String replyId = reply.optString(Keys.OBJECT_ID);

            if (null != currentUser) {
                reply.put(Common.REWARDED,
                        rewardQueryService.isRewarded(currentUser.optString(Keys.OBJECT_ID),
                                replyId, Reward.TYPE_C_COMMENT));
            }

            final int rewardCount = reply.optInt(Comment.COMMENT_THANK_CNT);
            reply.put(Common.REWARED_COUNT, rewardCount);
        }

        context.renderJSON(true).renderJSONValue(Comment.COMMENT_T_REPLIES, replies);
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
     *     "commentVisible": boolean,
     *     "commentOriginalCommentId": "", // optional
     *     "userCommentViewMode": int
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void addComment(final RequestContext context) {
        context.renderJSON().renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = (JSONObject) context.attr(Keys.REQUEST);

        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        final String commentContent = requestJSONObject.optString(Comment.COMMENT_CONTENT);
        final String commentOriginalCommentId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
        final int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");

        final boolean isAnonymous = requestJSONObject.optBoolean(Comment.COMMENT_ANONYMOUS);
        final boolean isOnlyAuthorVisible = requestJSONObject.optBoolean(Comment.COMMENT_VISIBLE);

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
            final JSONObject currentUser = Sessions.getUser();
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
                    context.sendError(403);
                    return;
                }
            }

            final String commentAuthorId = currentUser.optString(Keys.OBJECT_ID);
            comment.put(Comment.COMMENT_AUTHOR_ID, commentAuthorId);
            comment.put(Comment.COMMENT_ANONYMOUS, isAnonymous
                    ? Comment.COMMENT_ANONYMOUS_C_ANONYMOUS : Comment.COMMENT_ANONYMOUS_C_PUBLIC);
            comment.put(Comment.COMMENT_VISIBLE, isOnlyAuthorVisible
                    ? Comment.COMMENT_VISIBLE_C_AUTHOR : Comment.COMMENT_VISIBLE_C_ALL);

            commentMgmtService.addComment(comment);

            if ((!commentAuthorId.equals(articleAuthorId) &&
                    UserExt.USER_XXX_STATUS_C_ENABLED == currentUser.optInt(UserExt.USER_REPLY_WATCH_ARTICLE_STATUS))
                    || Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)) {
                followMgmtService.watchArticle(commentAuthorId, articleId);
            }

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }

    /**
     * Thanks a comment.
     *
     * @param context the specified context
     */
    public void thankComment(final RequestContext context) {
        context.renderJSON();

        final JSONObject requestJSONObject = context.requestJSON();
        final JSONObject currentUser = Sessions.getUser();
        final String commentId = requestJSONObject.optString(Comment.COMMENT_T_ID);

        try {
            commentMgmtService.thankComment(commentId, currentUser.optString(Keys.OBJECT_ID));

            context.renderTrueResult().renderMsg(langPropsService.get("thankSentLabel"));
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
        }
    }
}
