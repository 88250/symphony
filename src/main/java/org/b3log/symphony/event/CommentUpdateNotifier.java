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
package org.b3log.symphony.event;

import org.b3log.latke.Keys;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.repository.NotificationRepository;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Sends comment update related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Nov 17, 2018
 * @since 2.1.0
 */
@Singleton
public class CommentUpdateNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentUpdateNotifier.class);

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
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

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
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);
            final String commentId = originalComment.optString(Keys.OBJECT_ID);
            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);
            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final JSONObject commenter = userQueryService.getUser(commenterId);
            final String commenterName = commenter.optString(User.USER_NAME);
            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);
            atUserNames.remove(commenterName);
            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;
            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> articleContentAtUserNames = userQueryService.getUserNames(articleContent);
            final Set<String> requisiteAtUserPermissions = new HashSet<>();
            requisiteAtUserPermissions.add(Permission.PERMISSION_ID_C_COMMON_AT_USER);
            final boolean hasAtUserPerm = roleQueryService.userHasPermissions(commenterId, requisiteAtUserPermissions);
            final Set<String> atIds = new HashSet<>();
            if (hasAtUserPerm) {
                // 'At' Notification
                for (final String userName : atUserNames) {
                    if (isDiscussion && !articleContentAtUserNames.contains(userName)) {
                        continue;
                    }

                    final JSONObject atUser = userQueryService.getUserByName(userName);
                    if (atUser.optString(Keys.OBJECT_ID).equals(articleAuthorId)) {
                        continue; // Has notified in step 2
                    }

                    final String atUserId = atUser.optString(Keys.OBJECT_ID);
                    if (!notificationRepository.hasSentByDataIdAndType(atUserId, commentId, Notification.DATA_TYPE_C_AT)) {
                        final JSONObject requestJSONObject = new JSONObject();
                        requestJSONObject.put(Notification.NOTIFICATION_USER_ID, atUserId);
                        requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);
                        notificationMgmtService.addAtNotification(requestJSONObject);
                    }

                    atIds.add(atUserId);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the comment update notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_COMMENT}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_COMMENT;
    }
}
