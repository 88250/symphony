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
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Follow;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.processor.middleware.LoginCheckMidware;
import org.b3log.symphony.processor.middleware.PermissionMidware;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.FollowMgmtService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.util.Sessions;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Follow processor.
 * <ul>
 * <li>Follows a user (/follow/user), POST</li>
 * <li>Unfollows a user (/follow/user), POST</li>
 * <li>Follows a tag (/follow/tag), POST</li>
 * <li>Unfollows a tag (/follow/tag), POST</li>
 * <li>Follows an article (/follow/article), POST</li>
 * <li>Unfollows an article (/follow/article), POST</li>
 * <li>Watches an article (/follow/article-watch), POST</li>
 * <li>Unwatches an article (/follow/article-watch), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.5
 */
@Singleton
public class FollowProcessor {

    /**
     * Holds follows.
     */
    private static final Set<String> FOLLOWS = new HashSet<>();
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
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Register request handlers.
     */
    public static void register() {
        final BeanManager beanManager = BeanManager.getInstance();
        final LoginCheckMidware loginCheck = beanManager.getReference(LoginCheckMidware.class);
        final PermissionMidware permissionMidware = beanManager.getReference(PermissionMidware.class);

        final FollowProcessor followProcessor = beanManager.getReference(FollowProcessor.class);
        Dispatcher.post("/follow/user", followProcessor::followUser, loginCheck::handle);
        Dispatcher.post("/unfollow/user", followProcessor::unfollowUser, loginCheck::handle);
        Dispatcher.post("/follow/tag", followProcessor::followTag, loginCheck::handle);
        Dispatcher.post("/unfollow/tag", followProcessor::unfollowTag, loginCheck::handle);
        Dispatcher.post("/follow/article", followProcessor::followArticle, loginCheck::handle, permissionMidware::check);
        Dispatcher.post("/unfollow/article", followProcessor::unfollowArticle, loginCheck::handle);
        Dispatcher.post("/follow/article-watch", followProcessor::watchArticle, loginCheck::handle, permissionMidware::check);
        Dispatcher.post("/unfollow/article-watch", followProcessor::unwatchArticle, loginCheck::handle, permissionMidware::check);
    }

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
     * @param context the specified context
     */
    public void followUser(final RequestContext context) {
        context.renderJSON();

        final JSONObject requestJSONObject = context.requestJSON();
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followUser(followerUserId, followingUserId);

        if (!FOLLOWS.contains(followingUserId + followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, followingUserId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followerUserId);

            notificationMgmtService.addNewFollowerNotification(notification);
        }

        FOLLOWS.add(followingUserId + followerUserId);

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
     * @param context the specified context
     */
    public void unfollowUser(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingUserId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
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
     * @param context the specified context
     */
    public void followTag(final RequestContext context) {
        context.renderJSON();
        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
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
     * @param context the specified context
     */
    public void unfollowTag(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingTagId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
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
     * @param context the specified context
     */
    public void followArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.followArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewFollowerNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

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
     * @param context the specified context
     */
    public void unfollowArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
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
     * @param context the specified context
     */
    public void watchArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.watchArticle(followerUserId, followingArticleId);

        final JSONObject article = articleQueryService.getArticle(followingArticleId);
        final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        if (!FOLLOWS.contains(articleAuthorId + followingArticleId + "-" + followerUserId) &&
                !articleAuthorId.equals(followerUserId)) {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, followingArticleId + "-" + followerUserId);

            notificationMgmtService.addArticleNewWatcherNotification(notification);
        }

        FOLLOWS.add(articleAuthorId + followingArticleId + "-" + followerUserId);

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
     * @param context the specified context
     */
    public void unwatchArticle(final RequestContext context) {
        context.renderJSON();

        final Request request = context.getRequest();
        final JSONObject requestJSONObject = context.requestJSON();
        final String followingArticleId = requestJSONObject.optString(Follow.FOLLOWING_ID);
        final JSONObject currentUser = Sessions.getUser();
        final String followerUserId = currentUser.optString(Keys.OBJECT_ID);

        followMgmtService.unwatchArticle(followerUserId, followingArticleId);

        context.renderTrueResult();
    }
}
