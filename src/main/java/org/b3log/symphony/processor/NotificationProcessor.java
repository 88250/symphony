/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @version 1.12.0.2, Aug 12, 2018
 * @since 0.2.5
 */
@RequestProcessor
public class NotificationProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationProcessor.class);

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
     * Remove notifications by the specified type.
     *
     * @param context the specified context
     * @param request the specified request
     * @param type    the specified type: commented/reply/at/following/point/broadcast
     */
    @RequestProcessing(value = "/notifications/remove/{type}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeNotifications(final HTTPRequestContext context, final HttpServletRequest request, final String type) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
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
                context.renderJSON(false);

                return;
        }

        context.renderJSON(true);
    }

    /**
     * Removes a notification.
     *
     * @param context           the specified context
     * @param request           the specified request
     * @param requestJSONObject the specified request json object
     */
    @RequestProcessing(value = "/notifications/remove", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeNotification(final HTTPRequestContext context, final HttpServletRequest request, final JSONObject requestJSONObject) {
        context.renderJSON(true);

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
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
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/sys-announce", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showSysAnnounceNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                             final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/sys-announce.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("sysAnnounceNotificationsCnt");
        final int windowSize = Symphonys.getInt("sysAnnounceNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getSysAnnounceNotifications(
                avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Makes all notifications as read.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/notifications/all-read", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void makeAllNotificationsRead(final HTTPRequestContext context, final HttpServletRequest request) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);

        notificationMgmtService.makeAllRead(userId);

        context.renderJSON(true);
    }

    /**
     * Makes the specified type notifications as read.
     *
     * @param context the specified context
     * @param request the specified request
     * @param type    the specified type: "commented"/"at"/"following"
     */
    @RequestProcessing(value = "/notifications/read/{type}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void makeNotificationRead(final HTTPRequestContext context, final HttpServletRequest request, final String type) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
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
                context.renderJSON(false);

                return;
        }


        context.renderJSON(true);
    }

    /**
     * Makes article/comment read.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/notifications/read", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void makeNotificationRead(final HTTPRequestContext context, final HttpServletRequest request,
                                     final HttpServletResponse response) {
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String articleId = requestJSONObject.optString(Article.ARTICLE_T_ID);
        final List<String> commentIds = Arrays.asList(requestJSONObject.optString(Comment.COMMENT_T_IDS).split(","));

        notificationMgmtService.makeRead(userId, articleId, commentIds);

        context.renderJSON(true);
    }

    /**
     * Navigates notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void navigateNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        final int unreadCommentedNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENTED);
        if (unreadCommentedNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/commented");

            return;
        }

        final int unreadReplyNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_REPLY);
        if (unreadReplyNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/reply");

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
            response.sendRedirect(Latkes.getServePath() + "/notifications/at");

            return;
        }

        final int unreadPointNotificationCnt = notificationQueryService.getUnreadPointNotificationCount(userId);
        if (unreadPointNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/point");

            return;
        }

        final int unreadFollowingNotificationCnt = notificationQueryService.getUnreadFollowingNotificationCount(userId);
        if (unreadFollowingNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/following");

            return;
        }

        final int unreadBroadcastCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        if (unreadBroadcastCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/broadcast");

            return;
        }

        final int unreadSysAnnounceCnt = notificationQueryService.getUnreadSysAnnounceNotificationCount(userId);
        if (unreadSysAnnounceCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/sys-announce");

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/notifications/commented");
    }

    /**
     * Shows [point] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/point", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showPointNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                       final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/point.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("pointNotificationsCnt");
        final int windowSize = Symphonys.getInt("pointNotificationsWindowSize");

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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Fills notification count.
     *
     * @param userId    the specified user id
     * @param dataModel the specified data model
     */
    private void fillNotificationCount(final String userId, final Map<String, Object> dataModel) {
        final int unreadCommentedNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENTED);
        dataModel.put(Common.UNREAD_COMMENTED_NOTIFICATION_CNT, unreadCommentedNotificationCnt);

        final int unreadReplyNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_REPLY);
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

        final int unreadFollowingNotificationCnt
                = notificationQueryService.getUnreadFollowingNotificationCount(userId);
        dataModel.put(Common.UNREAD_FOLLOWING_NOTIFICATION_CNT, unreadFollowingNotificationCnt);

        final int unreadPointNotificationCnt
                = notificationQueryService.getUnreadPointNotificationCount(userId);
        dataModel.put(Common.UNREAD_POINT_NOTIFICATION_CNT, unreadPointNotificationCnt);

        final int unreadBroadcastNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        dataModel.put(Common.UNREAD_BROADCAST_NOTIFICATION_CNT, unreadBroadcastNotificationCnt);

        final int unreadSysAnnounceNotificationCnt = notificationQueryService.getUnreadSysAnnounceNotificationCount(userId);
        dataModel.put(Common.UNREAD_SYS_ANNOUNCE_NOTIFICATION_CNT, unreadSysAnnounceNotificationCnt);

        final int unreadNewFollowerNotificationCnt = notificationQueryService.getUnreadNotificationCountByType(
                userId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
        dataModel.put(Common.UNREAD_NEW_FOLLOWER_NOTIFICATION_CNT, unreadNewFollowerNotificationCnt);

        dataModel.put(Common.UNREAD_NOTIFICATION_CNT, unreadAtNotificationCnt + unreadBroadcastNotificationCnt
                + unreadCommentedNotificationCnt + unreadFollowingNotificationCnt + unreadPointNotificationCnt
                + unreadReplyNotificationCnt + unreadSysAnnounceNotificationCnt + unreadNewFollowerNotificationCnt);
    }

    /**
     * Shows [commented] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/commented", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showCommentedNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/commented.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("commentedNotificationsCnt");
        final int windowSize = Symphonys.getInt("commentedNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getCommentedNotifications(
                avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [reply] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/reply", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showReplyNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                       final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/reply.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("replyNotificationsCnt");
        final int windowSize = Symphonys.getInt("replyNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getReplyNotifications(
                avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [at] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/at", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showAtNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                    final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/at.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("atNotificationsCnt");
        final int windowSize = Symphonys.getInt("atNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getAtNotifications(avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [following] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/following", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showFollowingNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/following.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("followingNotificationsCnt");
        final int windowSize = Symphonys.getInt("followingNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getFollowingNotifications(
                avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [broadcast] notifications.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/broadcast", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showBroadcastNotifications(final HTTPRequestContext context, final HttpServletRequest request,
                                           final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/notifications/broadcast.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("broadcastNotificationsCnt");
        final int windowSize = Symphonys.getInt("broadcastNotificationsWindowSize");

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject result = notificationQueryService.getBroadcastNotifications(
                avatarViewMode, userId, pageNum, pageSize);
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

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Gets unread count of notifications.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/notifications/unread/count", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getUnreadNotificationCount(final HTTPRequestContext context, final HttpServletRequest request) {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final Map<String, Object> dataModel = new HashMap<>();

        fillNotificationCount(userId, dataModel);

        context.renderJSON(new JSONObject(dataModel)).renderTrueResult().
                renderJSONValue(UserExt.USER_NOTIFY_STATUS, currentUser.optInt(UserExt.USER_NOTIFY_STATUS));
    }
}
