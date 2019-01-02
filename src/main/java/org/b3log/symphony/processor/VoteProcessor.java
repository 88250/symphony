/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.service.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
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
 * @version 1.3.0.6, Jun 27, 2018
 * @since 1.3.0
 */
@RequestProcessor
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
    @RequestProcessing(value = "/vote/up/comment", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void voteUpComment(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/vote/down/comment", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void voteDownComment(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/vote/up/article", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void voteUpArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) context.attr(Common.CURRENT_USER);
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
    @RequestProcessing(value = "/vote/down/article", method = HttpMethod.POST)
    @Before({LoginCheck.class, PermissionCheck.class})
    public void voteDownArticle(final RequestContext context) {
        context.renderJSON();

        final HttpServletRequest request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String dataId = requestJSONObject.optString(Common.DATA_ID);

        final JSONObject currentUser = (JSONObject) context.attr(Common.CURRENT_USER);
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
