/*
 * Copyright (c) 2012-2015, b3log.org
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
package org.b3log.symphony.event;

import java.util.Date;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.ThumbnailQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONObject;

/**
 * Sends a comment notification.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.2.10, Jun 20, 2015
 * @since 0.2.0
 */
@Named
public class CommentNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentNotifier.class.getName());

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
     * Thumbnail query service.
     */
    @Inject
    private ThumbnailQueryService thumbnailQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, CommentNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);
            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final JSONObject commenter = userQueryService.getUser(originalComment.optString(Comment.COMMENT_AUTHOR_ID));
            final String commenterName = commenter.optString(User.USER_NAME);

            // 0. Data channel (WebSocket)
            final JSONObject chData = new JSONObject();
            chData.put(Article.ARTICLE_T_ID, originalArticle.optString(Keys.OBJECT_ID));
            chData.put(Comment.COMMENT_T_ID, originalComment.optString(Keys.OBJECT_ID));
            chData.put(Comment.COMMENT_T_AUTHOR_NAME, commenterName);
            final int avatarType = commenter.optInt(UserExt.USER_AVATAR_TYPE);
            if (UserExt.USER_AVATAR_TYPE_C_GRAVATAR == avatarType) {
                final String userEmail = commenter.optString(User.USER_EMAIL);
                final String thumbnailURL = thumbnailQueryService.getGravatarURL(userEmail, "140");
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);
            } else if (UserExt.USER_AVATAR_TYPE_C_EXTERNAL_LINK == avatarType
                    || UserExt.USER_AVATAR_TYPE_C_UPLOAD == avatarType) {
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, commenter.optString(UserExt.USER_AVATAR_URL));
            }
            chData.put(Comment.COMMENT_CREATE_TIME,
                    DateFormatUtils.format(new Date(originalComment.optLong(Comment.COMMENT_CREATE_TIME)), "yyyy-MM-dd HH:mm"));
            String cc = commentQueryService.linkArticle(commentContent);
            cc = Emotions.convert(cc);
            cc = Markdowns.toHTML(cc);
            cc = Markdowns.clean(cc, "");
            try {
                final Set<String> userNames = userQueryService.getUserNames(commentContent);
                for (final String userName : userNames) {
                    cc = cc.replace('@' + userName,
                            "@<a href='" + Latkes.getStaticServePath()
                            + "/member/" + userName + "'>" + userName + "</a>");
                }
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Generates @username home URL for comment content failed", e);
            }
            chData.put(Comment.COMMENT_CONTENT, cc);

            ArticleChannel.notifyComment(chData);

            // + Article Heat
            final JSONObject articleHeat = new JSONObject();
            articleHeat.put(Article.ARTICLE_T_ID, originalArticle.optString(Keys.OBJECT_ID));
            articleHeat.put(Common.OPERATION, "+");
            ArticleListChannel.notifyHeat(articleHeat);

            // 1. 'Commented' Notification
            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);
            final boolean commenterIsArticleAuthor = articleAuthorId.equals(originalComment.optString(Comment.COMMENT_AUTHOR_ID));
            if (commenterIsArticleAuthor && atUserNames.isEmpty()) {
                return;
            }

            atUserNames.remove(commenterName); // Do not notify commenter itself

            if (!commenterIsArticleAuthor) {
                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, originalComment.optString(Keys.OBJECT_ID));

                notificationMgmtService.addCommentedNotification(requestJSONObject);
            }

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;
            final Set<String> articleContentAtUserNames = userQueryService.getUserNames(articleContent);

            // 2. 'At' Notification
            for (final String userName : atUserNames) {
                if (isDiscussion && !articleContentAtUserNames.contains(userName)) {
                    continue;
                }

                final JSONObject user = userQueryService.getUserByName(userName);

                if (null == user) {
                    LOGGER.log(Level.WARN, "Not found user by name [{0}]", userName);

                    continue;
                }

                if (user.optString(Keys.OBJECT_ID).equals(articleAuthorId)) {
                    continue; // Has added in step 1
                }

                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, user.optString(Keys.OBJECT_ID));
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, originalComment.optString(Keys.OBJECT_ID));

                notificationMgmtService.addAtNotification(requestJSONObject);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the comment notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#ADD_COMMENT_TO_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.ADD_COMMENT_TO_ARTICLE;
    }
}
