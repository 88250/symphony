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
package org.b3log.symphony.event;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.*;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Escapes;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends article add related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.4.16, Aug 6, 2018
 * @since 0.2.0
 */
@Singleton
public class ArticleAddNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleAddNotifier.class);

    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    @Override
    public void action(final Event<JSONObject> event) {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={0}, data={1}]", event.getType(), data);

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);
            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(articleAuthorId, requisiteAtUserPermissions);
            final Set<String> atedUserIds = new HashSet<>();

            if (hasAtUserPerm) {
                // 'At' Notification
                final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
                final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
                atUserNames.remove(articleAuthorName); // Do not notify the author itself

                for (final String userName : atUserNames) {
                    final JSONObject user = userQueryService.getUserByName(userName);

                    if (null == user) {
                        LOGGER.log(Level.WARN, "Not found user by name [{0}]", userName);

                        continue;
                    }

                    final JSONObject requestJSONObject = new JSONObject();
                    final String atedUserId = user.optString(Keys.OBJECT_ID);
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, atedUserId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addAtNotification(requestJSONObject);

                    atedUserIds.add(atedUserId);
                }
            }

            final String tags = originalArticle.optString(Article.ARTICLE_TAGS);

            // 'following - user' Notification
            if (Article.ARTICLE_TYPE_C_DISCUSSION != originalArticle.optInt(Article.ARTICLE_TYPE)
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == originalArticle.optInt(Article.ARTICLE_ANONYMOUS)
                    && !Tag.TAG_TITLE_C_SANDBOX.equals(tags)
                    && !StringUtils.containsIgnoreCase(tags, Symphonys.get("systemAnnounce"))) {
                final JSONObject followerUsersResult = followQueryService.getFollowerUsers(
                        UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleAuthorId, 1, Integer.MAX_VALUE);

                final List<JSONObject> followerUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
                for (final JSONObject followerUser : followerUsers) {
                    final JSONObject requestJSONObject = new JSONObject();
                    final String followerUserId = followerUser.optString(Keys.OBJECT_ID);

                    if (atedUserIds.contains(followerUserId)) {
                        continue;
                    }

                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, followerUserId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addFollowingUserNotification(requestJSONObject);
                }
            }

            final String articleTitle = Escapes.escapeHTML(originalArticle.optString(Article.ARTICLE_TITLE));

            // 'Broadcast' Notification
            if (Article.ARTICLE_TYPE_C_CITY_BROADCAST == originalArticle.optInt(Article.ARTICLE_TYPE)
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == originalArticle.optInt(Article.ARTICLE_ANONYMOUS)) {
                final String city = originalArticle.optString(Article.ARTICLE_CITY);

                if (StringUtils.isNotBlank(city)) {
                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, 1);
                    requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, Integer.MAX_VALUE);
                    requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, Integer.MAX_VALUE);

                    final long latestLoginTime = DateUtils.addDays(new Date(), -15).getTime();
                    requestJSONObject.put(UserExt.USER_LATEST_LOGIN_TIME, latestLoginTime);
                    requestJSONObject.put(UserExt.USER_CITY, city);

                    final JSONObject result = userQueryService.getUsersByCity(requestJSONObject);
                    final JSONArray users = result.optJSONArray(User.USERS);

                    for (int i = 0; i < users.length(); i++) {
                        final String userId = users.optJSONObject(i).optString(Keys.OBJECT_ID);

                        if (userId.equals(articleAuthorId)) {
                            continue;
                        }

                        final JSONObject notification = new JSONObject();
                        notification.put(Notification.NOTIFICATION_USER_ID, userId);
                        notification.put(Notification.NOTIFICATION_DATA_ID, articleId);

                        notificationMgmtService.addBroadcastNotification(notification);
                    }

                    LOGGER.info("City [" + city + "] broadcast [users=" + users.length() + "]");
                }
            }

            // 'Sys Announce' Notification
            if (StringUtils.containsIgnoreCase(tags, Symphonys.get("systemAnnounce"))) {
                final long latestLoginTime = DateUtils.addDays(new Date(), -15).getTime();

                final JSONObject result = userQueryService.getLatestLoggedInUsers(
                        latestLoginTime, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
                final JSONArray users = result.optJSONArray(User.USERS);

                for (int i = 0; i < users.length(); i++) {
                    final String userId = users.optJSONObject(i).optString(Keys.OBJECT_ID);

                    final JSONObject notification = new JSONObject();
                    notification.put(Notification.NOTIFICATION_USER_ID, userId);
                    notification.put(Notification.NOTIFICATION_DATA_ID, articleId);

                    notificationMgmtService.addSysAnnounceArticleNotification(notification);
                }

                LOGGER.info("System announcement [" + articleTitle + "] broadcast [users=" + users.length() + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article add notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#ADD_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.ADD_ARTICLE;
    }
}
