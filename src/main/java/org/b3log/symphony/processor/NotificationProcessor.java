/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Notification processor.
 *
 * <ul>
 * <li>Displays comments of my articles (/notifications/commented), GET</li>
 * <li>Displays at me (/notifications/at), GET</li>
 * <li>Displays following user's articles (/notifications/following-user), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.1.3, Jul 5, 2016
 * @since 0.2.5
 */
@RequestProcessor
public class NotificationProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationProcessor.class.getName());

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
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Navigates notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void navigateNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
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

        final int unreadAtNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_AT);
        if (unreadAtNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/at");

            return;
        }

        final int unreadPointNotificationCnt = notificationQueryService.getUnreadPointNotificationCount(userId);
        if (unreadPointNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/point");

            return;
        }

        final int unreadFollowingUserNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_FOLLOWING_USER);
        if (unreadFollowingUserNotificationCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/following-user");

            return;
        }

        final int unreadBroadcastCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        if (unreadBroadcastCnt > 0) {
            response.sendRedirect(Latkes.getServePath() + "/notifications/broadcast");

            return;
        }

        response.sendRedirect(Latkes.getServePath() + "/notifications/commented");
    }

    /**
     * Shows [point] notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/point", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showPointNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/point.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("pointNotificationsCnt");
        final int windowSize = Symphonys.getInt("pointNotificationsWindowSize");

        final JSONObject result = notificationQueryService.getPointNotifications(userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
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

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Fills notification count.
     *
     * @param userId the specified user id
     * @param dataModel the specified data model
     */
    private void fillNotificationCount(final String userId, final Map<String, Object> dataModel) {
        final int unreadCommentedNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_COMMENTED);
        dataModel.put(Common.UNREAD_COMMENTED_NOTIFICATION_CNT, unreadCommentedNotificationCnt);

        final int unreadAtNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_AT);
        dataModel.put(Common.UNREAD_AT_NOTIFICATION_CNT, unreadAtNotificationCnt);

        final int unreadFollowingUserNotificationCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_FOLLOWING_USER);
        dataModel.put(Common.UNREAD_FOLLOWING_USER_NOTIFICATION_CNT, unreadFollowingUserNotificationCnt);

        final int unreadPointNotificationCnt
                = notificationQueryService.getUnreadPointNotificationCount(userId);
        dataModel.put(Common.UNREAD_POINT_NOTIFICATION_CNT, unreadPointNotificationCnt);

        final int unreadBroadcastCnt
                = notificationQueryService.getUnreadNotificationCountByType(userId, Notification.DATA_TYPE_C_BROADCAST);
        dataModel.put(Common.UNREAD_BROADCAST_NOTIFICATION_CNT, unreadBroadcastCnt);
    }

    /**
     * Shows [commented] notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/commented", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showCommentedNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/commented.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("commentedNotificationsCnt");
        final int windowSize = Symphonys.getInt("commentedNotificationsWindowSize");

        final JSONObject result = notificationQueryService.getCommentedNotifications(userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        final List<JSONObject> commentedNotifications = (List<JSONObject>) result.get(Keys.RESULTS);
        dataModel.put(Common.COMMENTED_NOTIFICATIONS, commentedNotifications);

        fillNotificationCount(userId, dataModel);

        notificationMgmtService.makeRead(commentedNotifications);

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

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [at] notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/at", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showAtNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/at.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("atNotificationsCnt");
        final int windowSize = Symphonys.getInt("atNotificationsWindowSize");

        final JSONObject result = notificationQueryService.getAtNotifications(userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        final List<JSONObject> atNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.AT_NOTIFICATIONS, atNotifications);

        fillNotificationCount(userId, dataModel);

        notificationMgmtService.makeRead(atNotifications);

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

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [followingUser] notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/following-user", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showFollowingUserNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/following-user.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("followingUserNotificationsCnt");
        final int windowSize = Symphonys.getInt("followingUserNotificationsWindowSize");

        final JSONObject result = notificationQueryService.getFollowingUserNotifications(userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        final List<JSONObject> followingUserNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.FOLLOWING_USER_NOTIFICATIONS, followingUserNotifications);

        fillNotificationCount(userId, dataModel);

        notificationMgmtService.makeRead(followingUserNotifications);

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

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows [broadcast] notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/broadcast", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void showBroadcastNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/broadcast.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final String userId = currentUser.optString(Keys.OBJECT_ID);

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("broadcastNotificationsCnt");
        final int windowSize = Symphonys.getInt("broadcastNotificationsWindowSize");

        final JSONObject result = notificationQueryService.getBroadcastNotifications(userId, pageNum, pageSize);
        @SuppressWarnings("unchecked")
        final List<JSONObject> broadcastNotifications = (List<JSONObject>) result.get(Keys.RESULTS);

        dataModel.put(Common.BROADCAST_NOTIFICATIONS, broadcastNotifications);

        fillNotificationCount(userId, dataModel);

        notificationMgmtService.makeRead(broadcastNotifications);

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

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Gets unread count of notifications.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notification/unread/count", method = HTTPRequestMethod.GET)
    public void getUnreadNotificationCount(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON(true).renderJSONValue(Notification.NOTIFICATION_T_UNREAD_COUNT,
                notificationQueryService.getUnreadNotificationCount(currentUser.optString(Keys.OBJECT_ID))).
                renderJSONValue(UserExt.USER_NOTIFY_STATUS, currentUser.optInt(UserExt.USER_NOTIFY_STATUS));
    }
}
