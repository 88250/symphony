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
package org.b3log.symphony.event;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.NotificationRepository;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends article update related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.6, Apr 9, 2019
 * @since 2.0.0
 */
@Singleton
public class ArticleUpdateNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ArticleUpdateNotifier.class);

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

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
        LOGGER.log(Level.TRACE, "Processing an event [type={}, data={}]", event.getType(), data);

        try {
            final JSONObject articleUpdated = data.getJSONObject(Article.ARTICLE);
            final String articleId = articleUpdated.optString(Keys.OBJECT_ID);

            final String articleAuthorId = articleUpdated.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
            final boolean isDiscussion = articleUpdated.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            final String articleContent = articleUpdated.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(articleAuthorId, requisiteAtUserPermissions);
            final Set<String> atedUserIds = new HashSet<>();
            if (hasAtUserPerm) {
                // 'At' Notification
                for (final String userName : atUserNames) {
                    final JSONObject user = userQueryService.getUserByName(userName);
                    final JSONObject requestJSONObject = new JSONObject();
                    final String atedUserId = user.optString(Keys.OBJECT_ID);
                    if (!notificationRepository.hasSentByDataIdAndType(atedUserId, articleId, Notification.DATA_TYPE_C_AT)) {
                        requestJSONObject.put(Notification.NOTIFICATION_USER_ID, atedUserId);
                        requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);
                        notificationMgmtService.addAtNotification(requestJSONObject);
                    }

                    atedUserIds.add(atedUserId);
                }
            }

            final JSONObject oldArticle = data.optJSONObject(Common.OLD_ARTICLE);
            if (!Article.isDifferent(oldArticle, articleUpdated)) {
                // 更新帖子通知改进 https://github.com/b3log/symphony/issues/872
                LOGGER.log(Level.DEBUG, "The article [title=" + oldArticle.optString(Article.ARTICLE_TITLE) + "] has not changed, do not notify it's watchers");
                return;
            }

            // 'following - article update' Notification
            final boolean articleNotifyFollowers = data.optBoolean(Article.ARTICLE_T_NOTIFY_FOLLOWERS);
            if (articleNotifyFollowers) {
                final JSONObject followerUsersResult = followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId, 1, Integer.MAX_VALUE);
                final List<JSONObject> watcherUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
                for (final JSONObject watcherUser : watcherUsers) {
                    final String watcherName = watcherUser.optString(User.USER_NAME);
                    if ((isDiscussion && !atUserNames.contains(watcherName)) || articleAuthorName.equals(watcherName)) {
                        continue;
                    }

                    final JSONObject requestJSONObject = new JSONObject();
                    final String watcherUserId = watcherUser.optString(Keys.OBJECT_ID);
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, watcherUserId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, articleId);
                    notificationMgmtService.addFollowingArticleUpdateNotification(requestJSONObject);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article update notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_ARTICLE;
    }
}
