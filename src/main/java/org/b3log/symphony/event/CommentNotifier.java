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
package org.b3log.symphony.event;

import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.AvatarQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.PointtransferMgmtService;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Sends a comment notification.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.5.14, May 15, 2016
 * @since 0.2.0
 */
@Named
@Singleton
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
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, CommentNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);

            final String articleId = originalArticle.optString(Keys.OBJECT_ID);
            final String commentId = originalComment.optString(Keys.OBJECT_ID);
            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);

            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final JSONObject commenter = userQueryService.getUser(commenterId);
            final String commenterName = commenter.optString(User.USER_NAME);

            // 0. Data channel (WebSocket)
            final JSONObject chData = new JSONObject();
            chData.put(Article.ARTICLE_T_ID, articleId);
            chData.put(Comment.COMMENT_T_ID, commentId);
            chData.put(Comment.COMMENT_T_AUTHOR_NAME, commenterName);

            chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, avatarQueryService.getAvatarURLByUser(commenter));
            chData.put(Common.THUMBNAIL_UPDATE_TIME, commenter.optLong(UserExt.USER_UPDATE_TIME));

            chData.put(Comment.COMMENT_CREATE_TIME,
                    DateFormatUtils.format(new Date(originalComment.optLong(Comment.COMMENT_CREATE_TIME)), "yyyy-MM-dd HH:mm"));
            chData.put(Common.TIME_AGO, langPropsService.get("justNowLabel"));
            chData.put("thankLabel", langPropsService.get("thankLabel"));
            chData.put("thankedLabel", langPropsService.get("thankedLabel"));
            String thankTemplate = langPropsService.get("thankConfirmLabel");
            thankTemplate = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                    .replace("{user}", commenterName);
            chData.put(Comment.COMMENT_T_THANK_LABEL, thankTemplate);
            String cc = shortLinkQueryService.linkArticle(commentContent);
            cc = shortLinkQueryService.linkTag(cc);
            cc = Emotions.convert(cc);
            cc = Markdowns.toHTML(cc);
            cc = Markdowns.clean(cc, "");
            try {
                final Set<String> userNames = userQueryService.getUserNames(commentContent);
                for (final String userName : userNames) {
                    cc = cc.replace('@' + userName, "@<a href='" + Latkes.getServePath()
                            + "/member/" + userName + "'>" + userName + "</a>");
                }
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Generates @username home URL for comment content failed", e);
            }

            cc = cc.replace("@participants ",
                    "@<a href='https://hacpai.com/article/1458053458339' class='ft-red'>participants</a> ");

            chData.put(Comment.COMMENT_CONTENT, cc);
            chData.put(Comment.COMMENT_UA, originalComment.optString(Comment.COMMENT_UA));

            ArticleChannel.notifyComment(chData);

            // + Article Heat
            final JSONObject articleHeat = new JSONObject();
            articleHeat.put(Article.ARTICLE_T_ID, articleId);
            articleHeat.put(Common.OPERATION, "+");

            ArticleListChannel.notifyHeat(articleHeat);
            ArticleChannel.notifyHeat(articleHeat);

            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            // Timeline
            if (!isDiscussion) {
                String articleTitle = Jsoup.parse(originalArticle.optString(Article.ARTICLE_TITLE)).text();
                articleTitle = Emotions.convert(articleTitle);
                final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

                final JSONObject timeline = new JSONObject();
                timeline.put(Common.TYPE, Comment.COMMENT);
                String content = langPropsService.get("timelineCommentLabel");
                content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                        + "/member/" + commenterName + "'>" + commenterName + "</a>")
                        .replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                                + "'>" + articleTitle + "</a>")
                        .replace("{comment}", cc.replaceAll("<p>", "").replaceAll("</p>", ""));
                timeline.put(Common.CONTENT, content);

                timelineMgmtService.addTimeline(timeline);
            }

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final boolean commenterIsArticleAuthor = articleAuthorId.equals(commenterId);

            // 1. '@participants' Notification
            if (commentContent.contains("@participants ")) {
                final List<JSONObject> participants
                        = articleQueryService.getArticleLatestParticipants(articleId, Integer.MAX_VALUE);
                final int count = participants.size();
                if (count < 1) {
                    return;
                }

                final int sum = count * Pointtransfer.TRANSFER_SUM_C_AT_PARTICIPANTS;
                final boolean succ = null != pointtransferMgmtService.transfer(commenterId, Pointtransfer.ID_C_SYS,
                        Pointtransfer.TRANSFER_TYPE_C_AT_PARTICIPANTS, sum, commentId);
                if (!succ) {
                    return;
                }

                for (final JSONObject participant : participants) {
                    final String participantId = participant.optString(Keys.OBJECT_ID);
                    if (participantId.equals(articleAuthorId) || participantId.equals(commenterId)) {
                        continue;
                    }

                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, participantId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                    notificationMgmtService.addAtNotification(requestJSONObject);
                }

                return;
            }

            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);

            // 2. 'Commented' Notification
            if (commenterIsArticleAuthor && atUserNames.isEmpty()) {
                return;
            }

            atUserNames.remove(commenterName); // Do not notify commenter itself

            if (!commenterIsArticleAuthor) {
                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addCommentedNotification(requestJSONObject);
            }

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> articleContentAtUserNames = userQueryService.getUserNames(articleContent);

            // 3. 'At' Notification
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
                    continue; // Has added in step 2
                }

                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, user.optString(Keys.OBJECT_ID));
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

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
