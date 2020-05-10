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

import org.b3log.latke.Keys;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.PermissionMidware;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Sessions;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Vote processor.
 * <ul>
 * <li>Votes up an article (/vote/up/article), POST</li>
 * <li>Votes down an article (/vote/down/article), POST</li>
 * <li>Votes up a comment (/vote/up/comment), POST</li>
 * <li>Votes down a comment (/vote/down/comment), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.3.0
 */
@Singleton
public class VoteProcessor {

    /**
     * Holds votes.
     */
    private static final Set<String> VOTES = new HashSet<>();

    /**
     * Vote management service.
     */
    @Inject
    private VoteMgmtService voteMgmtService;

    /**
     * Vote query service.
     */
    @Inject
    private VoteQueryService voteQueryService;

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
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);

        final VoteProcessor voteProcessor = beanManager.getReference(VoteProcessor.class);
        Dispatcher.post("/vote/up/comment", voteProcessor::voteUpComment, loginCheck::handle, permissionMidware::check);
        Dispatcher.post("/vote/down/comment", voteProcessor::voteDownComment, loginCheck::handle, permissionMidware::check);
        Dispatcher.post("/vote/up/article", voteProcessor::voteUpArticle, loginCheck::handle, permissionMidware::check);
        Dispatcher.post("/vote/down/article", voteProcessor::voteDownArticle, loginCheck::handle, permissionMidware::check);
    }

    /**
     * Votes up a comment.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void voteUpComment(final RequestContext context) {
        context.renderJSON();

        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_COMMENT)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_COMMENT);
        } else {
            voteMgmtService.voteUp(userId, dataId, Vote.DATA_TYPE_C_COMMENT);

            final JSONObject comment = commentQueryService.getComment(dataId);
            final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);

            if (!VOTES.contains(userId + dataId) && !userId.equals(commenterId)) {
                final JSONObject notification = new JSONObject();
                notification.put(Notification.NOTIFICATION_USER_ID, commenterId);
                notification.put(Notification.NOTIFICATION_DATA_ID, dataId + "-" + userId);

                notificationMgmtService.addCommentVoteUpNotification(notification);
            }

            VOTES.add(userId + dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }

    /**
     * Votes down a comment.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void voteDownComment(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_COMMENT)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_DOWN == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_COMMENT);
        } else {
            voteMgmtService.voteDown(userId, dataId, Vote.DATA_TYPE_C_COMMENT);

            // https://github.com/b3log/symphony/issues/611
//            final JSONObject comment = commentQueryService.getComment(dataId);
//            final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);
//
//            if (!VOTES.contains(userId + dataId) && !userId.equals(commenterId)) {
//                final JSONObject notification = new JSONObject();
//                notification.put(Notification.NOTIFICATION_USER_ID, commenterId);
//                notification.put(Notification.NOTIFICATION_DATA_ID, dataId + "-" + userId);
//
//                notificationMgmtService.addCommentVoteDownNotification(notification);
//            }
//
//            VOTES.add(userId + dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }

    /**
     * Votes up an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void voteUpArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_ARTICLE)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_UP == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteUp(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);

            final JSONObject article = articleQueryService.getArticle(dataId);
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

            if (!VOTES.contains(userId + dataId) && !userId.equals(articleAuthorId)) {
                final JSONObject notification = new JSONObject();
                notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                notification.put(Notification.NOTIFICATION_DATA_ID, dataId + "-" + userId);

                notificationMgmtService.addArticleVoteUpNotification(notification);
            }

            VOTES.add(userId + dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }

    /**
     * Votes down an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "dataId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     */
    public void voteDownArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                && voteQueryService.isOwn(userId, dataId, Vote.DATA_TYPE_C_ARTICLE)) {
            context.renderFalseResult().renderMsg(langPropsService.get("cantVoteSelfLabel"));
            return;
        }

        final int vote = voteQueryService.isVoted(userId, dataId);
        if (Vote.TYPE_C_DOWN == vote) {
            voteMgmtService.voteCancel(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);
        } else {
            voteMgmtService.voteDown(userId, dataId, Vote.DATA_TYPE_C_ARTICLE);

            // https://github.com/b3log/symphony/issues/611
//            final JSONObject article = articleQueryService.getArticle(dataId);
//            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
//
//            if (!VOTES.contains(userId + dataId) && !userId.equals(articleAuthorId)) {
//                final JSONObject notification = new JSONObject();
//                notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
//                notification.put(Notification.NOTIFICATION_DATA_ID, dataId + "-" + userId);
//
//                notificationMgmtService.addArticleVoteDownNotification(notification);
//            }
//
//            VOTES.add(userId + dataId);
        }

        context.renderTrueResult().renderJSONValue(Vote.TYPE, vote);
    }
}
