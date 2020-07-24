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
import org.b3log.latke.Latkes;
import org.b3log.latke.http.Dispatcher;
import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.*;

/**
 * Notification processor.
 * <ul>
 * <li>Shows [commented] notifications (/notifications/commented), GET</li>
 * <li>Shows [reply] notifications (/notifications/reply), GET</li>
 * <li>Shows [at] notifications (/notifications/at), GET</li>
 * <li>Shows [following] notifications (/notifications/following), GET</li>
 * <li>Shows [point] notifications (/notifications/point), GET </li>
 * <li>Shows [broadcast] notifications (/notifications/broadcast), GET</li>
 * <li>Shows [sysAnnounce] notifications (/notifications/sys-announce), GET</li>
 * <li>Makes article/comment read (/notifications/read), GET</li>
 * <li>Gets unread count of notifications (/notifications/unread/count), GET</li>
 * <li>Makes all notifications as read (/notifications/all-read), GET</li>
 * <li>Makes the specified type notifications as read (/notifications/read/{type}), GET</li>
 * <li>Removes a notification (/notifications/remove), POST</li>
 * <li>Remove notifications by the specified type (/notifications/remove/{type}), GET </li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.5
 */
@Singleton
public class NotificationProcessor {

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);

        final NotificationProcessor notificationProcessor = beanManager.getReference(NotificationProcessor.class);
        Dispatcher.get("/notifications/remove/{type}", notificationProcessor::removeNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/remove", notificationProcessor::removeNotification, loginCheck::handle);
        Dispatcher.get("/notifications/sys-announce", notificationProcessor::showSysAnnounceNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/all-read", notificationProcessor::makeAllNotificationsRead, loginCheck::handle);
        Dispatcher.get("/notifications/make-read/{type}", notificationProcessor::makeNotificationReadByType, loginCheck::handle);
        Dispatcher.post("/notifications/make-read", notificationProcessor::makeNotificationRead, loginCheck::handle);
        Dispatcher.get("/notifications", notificationProcessor::navigateNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/point", notificationProcessor::showPointNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/commented", notificationProcessor::showCommentedNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/reply", notificationProcessor::showReplyNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/at", notificationProcessor::showAtNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/following", notificationProcessor::showFollowingNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/broadcast", notificationProcessor::showBroadcastNotifications, loginCheck::handle);
        Dispatcher.get("/notifications/unread/count", notificationProcessor::getUnreadNotificationCount, loginCheck::handle);
    }

    /**
     * Remove notifications by the specified type.
     *
     * @param context the specified context
     */
    public void removeNotifications(final RequestContext context) {
        final String type = context.pathVar("type"); // commented/reply/at/following/point/broadcast

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        switch (type) {
            case "commented":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_COMMENTED);
                break;
            case "reply":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_REPLY);
                break;
            case "at":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_AT);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_UP);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_UP);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN);
                break;
            case "following":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_FOLLOWING_USER);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT);
                break;
            case "point":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_ARTICLE_REWARD);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_ARTICLE_THANK);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_CHARGE);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_COMMENT_ACCEPT);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_COMMENT_THANK);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_EXCHANGE);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_PERFECT_ARTICLE);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_POINT_TRANSFER);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_ABUSE_POINT_DEDUCT);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_INVITECODE_USED);
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_INVITATION_LINK_USED);
                break;
            case "broadcast":
                notificationMgmtService.removeNotifications(userId, Notification.DATA_TYPE_C_BROADCAST);
                break;
            default:
                context.renderJSON(StatusCodes.ERR);
                return;
        }

        context.renderJSON(StatusCodes.SUCC);
    }

    /**
     * Removes a notification.
     *
     * @param context the specified context
     */
    public void removeNotification(final RequestContext context) {
        context.renderJSON(StatusCodes.SUCC);

        final JSONObject requestJSONObject = context.requestJSON();
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String notificationId = requestJSONObject.optString(Common.ID);

        final JSONObject notification = notificationQueryService.getNotification(notificationId);
        if (null == notification) {
            return;
        }

        if (!notification.optString(Notification.NOTIFICATION_USER_ID).equals(userId)) {
            return;
        }

        notificationMgmtService.removeNotification(notificationId);
    }

    /**
     * Shows [sysAnnounce] notifications.
     *
     * @param context the specified context
     */
    public void showSysAnnounceNotifications(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject currentUser = Sessions.getUser();

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/sys-announce.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getSysAnnounceNotifications(userId, pageNum, pageSize);
        final List<JSONObject> notifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.SYS_ANNOUNCE_NOTIFICATIONS, notifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Makes all notifications as read.
     *
     * @param context the specified context
     */
    public void makeAllNotificationsRead(final RequestContext context) {
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        notificationMgmtService.makeAllRead(userId);

        context.renderJSON(StatusCodes.SUCC);
    }

    /**
     * Makes the specified type notifications as read.
     *
     * @param context the specified context
     */
    public void makeNotificationReadByType(final RequestContext context) {
        final String type = context.pathVar("type"); // "commented"/"at"/"following"

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        switch (type) {
            case "commented":
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_COMMENTED);
                break;
            case "reply":
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_REPLY);
                break;
            case "at":
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_AT);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_UP);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_UP);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN);
                break;
            case "following":
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_FOLLOWING_USER);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_UPDATE);
                notificationMgmtService.makeRead(userId, Notification.DATA_TYPE_C_FOLLOWING_ARTICLE_COMMENT);
                break;
            default:
                context.renderJSON(StatusCodes.ERR);
                return;
        }


        context.renderJSON(StatusCodes.SUCC);
    }

    /**
     * Makes article/comment read.
     *
     * @param context the specified context
     */
    public void makeNotificationRead(final RequestContext context) {
        final JSONObject requestJSONObject = context.requestJSON();
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        final List<String> commentIds = Arrays.asList(requestJSONObject.optString(Comment.COMMENT_T_IDS).split(","));

        notificationMgmtService.makeRead(userId, articleId, commentIds);

        context.renderJSON(StatusCodes.SUCC);
    }

    /**
     * Navigates notifications.
     *
     * @param context the specified context
     */
    public void navigateNotifications(final RequestContext context) {
        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final int unreadCommentedNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENTED);
        if (unreadCommentedNotificationCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/commented");
            return;
        }

        final int unreadReplyNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_REPLY);
        if (unreadReplyNotificationCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/reply");
            return;
        }

        final int unreadAtNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_AT)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_UP)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_UP)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN);
        if (unreadAtNotificationCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/at");
            return;
        }

        final int unreadPointNotificationCnt = notificationQueryService.getUnreadPointNotificationCount(userId);
        if (unreadPointNotificationCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/point");
            return;
        }

        final int unreadFollowingNotificationCnt = notificationQueryService.getUnreadFollowingNotificationCount(userId);
        if (unreadFollowingNotificationCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/following");
            return;
        }

        final int unreadBroadcastCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        if (unreadBroadcastCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/broadcast");
            return;
        }

        final int unreadSysAnnounceCnt = notificationQueryService.getUnreadSysAnnounceNotificationCount(userId);
        if (unreadSysAnnounceCnt > 0) {
            context.sendRedirect(Latkes.getServePath() + "/notifications/sys-announce");
            return;
        }

        context.sendRedirect(Latkes.getServePath() + "/notifications/commented");
    }

    /**
     * Shows [point] notifications.
     *
     * @param context the specified context
     */
    public void showPointNotifications(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/point.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getPointNotifications(userId, pageNum, pageSize);
        final List<JSONObject> pointNotifications = (List<JSONObject>) result.get(Keys.RESULTS);
        dataModel.put(Common.POINT_NOTIFICATIONS, pointNotifications);

        fillNotificationCount(userId, dataModel);

        notificationMgmtService.makeRead(pointNotifications);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Fills notification count.
     *
     * @param userId    the specified user id
     * @param dataModel the specified data model
     */
    private void fillNotificationCount(final String userId, final Map<String, Object> dataModel) {
        final int unreadCommentedNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENTED);
        dataModel.put(Common.UNREAD_COMMENTED_NOTIFICATION_CNT, unreadCommentedNotificationCnt);

        final int unreadReplyNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_REPLY);
        dataModel.put(Common.UNREAD_REPLY_NOTIFICATION_CNT, unreadReplyNotificationCnt);

        final int unreadAtNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_AT)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_FOLLOWER)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_NEW_WATCHER)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_UP)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENT_VOTE_DOWN)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_UP)
                + notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_ARTICLE_VOTE_DOWN);
        dataModel.put(Common.UNREAD_AT_NOTIFICATION_CNT, unreadAtNotificationCnt);

        final int unreadFollowingNotificationCnt = notificationQueryService.getUnreadFollowingNotificationCount(userId);
        dataModel.put(Common.UNREAD_FOLLOWING_NOTIFICATION_CNT, unreadFollowingNotificationCnt);

        final int unreadPointNotificationCnt = notificationQueryService.getUnreadPointNotificationCount(userId);
        dataModel.put(Common.UNREAD_POINT_NOTIFICATION_CNT, unreadPointNotificationCnt);

        final int unreadBroadcastNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        dataModel.put(Common.UNREAD_BROADCAST_NOTIFICATION_CNT, unreadBroadcastNotificationCnt);

        final int unreadSysAnnounceNotificationCnt = notificationQueryService.getUnreadSysAnnounceNotificationCount(userId);
        dataModel.put(Common.UNREAD_SYS_ANNOUNCE_NOTIFICATION_CNT, unreadSysAnnounceNotificationCnt);

        final int unreadNewFollowerNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
        dataModel.put(Common.UNREAD_NEW_FOLLOWER_NOTIFICATION_CNT, unreadNewFollowerNotificationCnt);

        dataModel.put(Common.UNREAD_NOTIFICATION_CNT, unreadAtNotificationCnt + unreadBroadcastNotificationCnt
                + unreadCommentedNotificationCnt + unreadFollowingNotificationCnt + unreadPointNotificationCnt
                + unreadReplyNotificationCnt + unreadSysAnnounceNotificationCnt + unreadNewFollowerNotificationCnt);
    }

    /**
     * Shows [commented] notifications.
     *
     * @param context the specified context
     */
    public void showCommentedNotifications(final RequestContext context) {
        final Request request = context.getRequest();
        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/commented.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getCommentedNotifications(userId, pageNum, pageSize);
        final List<JSONObject> commentedNotifications = (List<JSONObject>) result.get(Keys.RESULTS);
        dataModel.put(Common.COMMENTED_NOTIFICATIONS, commentedNotifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows [reply] notifications.
     *
     * @param context the specified context
     */
    public void showReplyNotifications(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/reply.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getReplyNotifications(userId, pageNum, pageSize);
        final List<JSONObject> replyNotifications = (List<JSONObject>) result.get(Keys.RESULTS);
        dataModel.put(Common.REPLY_NOTIFICATIONS, replyNotifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows [at] notifications.
     *
     * @param context the specified context
     */
    public void showAtNotifications(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/at.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getAtNotifications(userId, pageNum, pageSize);
        final List<JSONObject> atNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.AT_NOTIFICATIONS, atNotifications);

        final List<JSONObject> articleFollowAndWatchNotifications = new ArrayList<>();
        for (final JSONObject notification : atNotifications) {
            if (Notification.DATA_TYPE_C_AT != notification.optInt(Notification.NOTIFICATION_DATA_TYPE)) {
                articleFollowAndWatchNotifications.add(notification);
            }
        }
        notificationMgmtService.makeRead(articleFollowAndWatchNotifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows [following] notifications.
     *
     * @param context the specified context
     */
    public void showFollowingNotifications(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/following.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getFollowingNotifications(userId, pageNum, pageSize);
        final List<JSONObject> followingNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.FOLLOWING_NOTIFICATIONS, followingNotifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Shows [broadcast] notifications.
     *
     * @param context the specified context
     */
    public void showBroadcastNotifications(final RequestContext context) {
        final Request request = context.getRequest();

        final JSONObject currentUser = Sessions.getUser();
        if (null == currentUser) {
            context.sendError(403);
            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "home/notifications/broadcast.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.NOTIFICATION_LIST_CNT;
        final int windowSize = Symphonys.NOTIFICATION_LIST_WIN_SIZE;

        final JSONObject result = notificationQueryService.getBroadcastNotifications(userId, pageNum, pageSize);
        final List<JSONObject> broadcastNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.BROADCAST_NOTIFICATIONS, broadcastNotifications);

        fillNotificationCount(userId, dataModel);

        final int recordCnt = result.getInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) recordCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(context, dataModel);
    }

    /**
     * Gets unread count of notifications.
     *
     * @param context the specified context
     */
    public void getUnreadNotificationCount(final RequestContext context) {
        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final Map<String, Object> dataModel = new HashMap<>();

        fillNotificationCount(userId, dataModel);

        context.renderJSON(new JSONObject(dataModel)).renderJSONValue(Keys.CODE, StatusCodes.SUCC).
                renderJSONValue(UserExt.USER_NOTIFY_STATUS, currentUser.optInt(UserExt.USER_NOTIFY_STATUS));
    }
}
