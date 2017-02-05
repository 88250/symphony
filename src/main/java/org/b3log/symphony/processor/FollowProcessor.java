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

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.util.Requests;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.service.FollowMgmtService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Follow processor.
 * <p>
 * <ul>
 * <li>Follows a user (/follow/user), POST</li>
 * <li>Unfollows a user (/follow/user), DELETE</li>
 * <li>Follows a tag (/follow/tag), POST</li>
 * <li>Unfollows a tag (/follow/tag), DELETE</li>
 * <li>Follows an article (/follow/article), POST</li>
 * <li>Unfollows an article (/follow/article), DELETE</li>
 * <li>Watches an article (/follow/article-watch), POST</li>
 * <li>Unwatches an article (/follow/article-watch), DELETE</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.3, Jan 18, 2017
 * @since 0.2.5
 */
@RequestProcessor
public class FollowProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(FollowProcessor.class.getName());

    /**
     * Follow management service.
     */
    @Inject
    private FollowMgmtService followMgmtService;

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Holds follow relations.
     * <p>
     * &lt;followerId, followingId&gt;
     * </p>
     */
    private static final Map<String, String> FOLLOWS = new HashMap<>();


    /**
     * Follows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/user", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void followUser(final HTTPRequestContext context, final HttpServletRequest request,
                           final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followUser(followerUserId, followingUserId);

        if (null == FOLLOWS.get(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, followingUserId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followerUserId);

            notificationMgmtService.addNewFollowerNotification(notification);
        }

        FOLLOWS.put(followerUserId, followingUserId);

        context.renderTrueResult();
    }

    /**
     * Unfollows a user.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/user", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unfollowUser(final HTTPRequestContext context, final HttpServletRequest request,
                             final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowUser(followerUserId, followingUserId);

        context.renderTrueResult();
    }

    /**
     * Follows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/tag", method = HTTPRequestMethod.POST)
    @Before(adviceClass = LoginCheck.class)
    public void followTag(final HTTPRequestContext context, final HttpServletRequest request,
                          final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followTag(followerUserId, followingTagId);

        context.renderTrueResult();
    }

    /**
     * Unfollows a tag.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/tag", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unfollowTag(final HTTPRequestContext context, final HttpServletRequest request,
                            final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowTag(followerUserId, followingTagId);

        context.renderTrueResult();
    }

    /**
     * Follows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    public void followArticle(final HTTPRequestContext context, final HttpServletRequest request,
                              final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }

    /**
     * Unfollows an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/article", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unfollowArticle(final HTTPRequestContext context, final HttpServletRequest request,
                                final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unfollowArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }

    /**
     * Watches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/article-watch", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, PermissionCheck.class})
    public void watchArticle(final HTTPRequestContext context, final HttpServletRequest request,
                             final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.watchArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }

    /**
     * Unwatches an article.
     * <p>
     * The request json object:
     * <pre>
     * {
     *   "followingId": ""
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/follow/article-watch", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = LoginCheck.class)
    public void unwatchArticle(final HTTPRequestContext context, final HttpServletRequest request,
                               final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unwatchArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }
}
