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
package org.b3log.symphony.event;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.channel.ArticleListChannel;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends a comment notification.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.7.9.21, Feb 16, 2017
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
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

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

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, CommentNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);
            final boolean fromClient = data.optBoolean(Common.FROM_CLIENT);
            final int commentViewMode = data.optInt(UserExt.USER_COMMENT_VIEW_MODE);

            final String articleId = originalArticle.optString(Keys.OBJECT_ID);
            final String commentId = originalComment.optString(Keys.OBJECT_ID);
            final String originalCmtId = originalComment.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);

            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final JSONObject commenter = userQueryService.getUser(commenterId);
            final String commenterName = commenter.optString(User.USER_NAME);

            // 0. Data channel (WebSocket)
            final JSONObject chData = JSONs.clone(originalComment);
            chData.put(Comment.COMMENT_T_COMMENTER, commenter);
            chData.put(Keys.OBJECT_ID, commentId);
            chData.put(Article.ARTICLE_T_ID, articleId);
            chData.put(Comment.COMMENT_T_ID, commentId);
            chData.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, originalCmtId);

            String originalCmtAuthorId = null;
            if (StringUtils.isNotBlank(originalCmtId)) {
                final Query numQuery = new Query()
                        .setPageSize(Integer.MAX_VALUE).setCurrentPageNum(1).setPageCount(1);

                switch (commentViewMode) {
                    case UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL:
                        numQuery.setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN_OR_EQUAL, originalCmtId)
                        )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);

                        break;
                    case UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME:
                        numQuery.setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
                                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, originalCmtId)
                        )).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

                        break;
                }

                final long num = commentRepository.count(numQuery);
                final int page = (int) ((num / Symphonys.getInt("articleCommentsPageSize")) + 1);
                chData.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, page);

                final JSONObject originalCmt = commentRepository.get(originalCmtId);
                originalCmtAuthorId = originalCmt.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject originalCmtAuthor = userRepository.get(originalCmtAuthorId);

                if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == originalCmt.optInt(Comment.COMMENT_ANONYMOUS)) {
                    chData.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                            avatarQueryService.getAvatarURLByUser(
                                    UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, originalCmtAuthor, "20"));
                } else {
                    chData.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                            avatarQueryService.getDefaultAvatarURL("20"));
                }
            }

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == originalComment.optInt(Comment.COMMENT_ANONYMOUS)) {
                chData.put(Comment.COMMENT_T_AUTHOR_NAME, commenterName);
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, avatarQueryService.getAvatarURLByUser(
                        UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, commenter, "48"));
            } else {
                chData.put(Comment.COMMENT_T_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
                chData.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, avatarQueryService.getDefaultAvatarURL("48"));
            }

            chData.put(Common.THUMBNAIL_UPDATE_TIME, commenter.optLong(UserExt.USER_UPDATE_TIME));
            chData.put(Common.TIME_AGO, langPropsService.get("justNowLabel"));
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
                    cc = cc.replace('@' + userName + " ", "@<a href='" + Latkes.getServePath()
                            + "/member/" + userName + "'>" + userName + "</a> ");
                }
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Generates @username home URL for comment content failed", e);
            }

            cc = cc.replace("@participants ",
                    "@<a href='https://hacpai.com/article/1458053458339' class='ft-red'>participants</a> ");
            if (fromClient) {
                // "<i class='ft-small'>by 88250</i>"
                String syncCommenterName = StringUtils.substringAfter(cc, "<i class=\"ft-small\">by ");
                syncCommenterName = StringUtils.substringBefore(syncCommenterName, "</i>");

                if (UserRegisterValidation.invalidUserName(syncCommenterName)) {
                    syncCommenterName = UserExt.ANONYMOUS_USER_NAME;
                }

                cc = cc.replaceAll("<i class=\"ft-small\">by .*</i>", "");

                chData.put(Comment.COMMENT_T_AUTHOR_NAME, syncCommenterName);
            }

            chData.put(Comment.COMMENT_CONTENT, cc);
            chData.put(Comment.COMMENT_UA, originalComment.optString(Comment.COMMENT_UA));
            chData.put(Common.FROM_CLIENT, fromClient);

            ArticleChannel.notifyComment(chData);

            // + Article Heat
            final JSONObject articleHeat = new JSONObject();
            articleHeat.put(Article.ARTICLE_T_ID, articleId);
            articleHeat.put(Common.OPERATION, "+");

            ArticleListChannel.notifyHeat(articleHeat);
            ArticleChannel.notifyHeat(articleHeat);

            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            // Timeline
            if (!isDiscussion
                    && Comment.COMMENT_ANONYMOUS_C_PUBLIC == originalComment.optInt(Comment.COMMENT_ANONYMOUS)) {
                String articleTitle = Jsoup.parse(originalArticle.optString(Article.ARTICLE_TITLE)).text();
                articleTitle = Emotions.convert(articleTitle);
                final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

                final JSONObject timeline = new JSONObject();
                timeline.put(Common.USER_ID, commenterId);
                timeline.put(Common.TYPE, Comment.COMMENT);
                String content = langPropsService.get("timelineCommentLabel");

                if (fromClient) {
                    // "<i class='ft-small'>by 88250</i>"
                    String syncCommenterName = StringUtils.substringAfter(cc, "<i class=\"ft-small\">by ");
                    syncCommenterName = StringUtils.substringBefore(syncCommenterName, "</i>");

                    if (UserRegisterValidation.invalidUserName(syncCommenterName)) {
                        syncCommenterName = UserExt.ANONYMOUS_USER_NAME;
                    }

                    content = content.replace("{user}", syncCommenterName);
                } else {
                    content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                            + "/member/" + commenterName + "'>" + commenterName + "</a>");
                }

                content = content.replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                        + "'>" + articleTitle + "</a>")
                        .replace("{comment}", cc.replaceAll("<p>", "").replaceAll("</p>", ""));

                content = Jsoup.clean(content, Whitelist.none().addAttributes("a", "href", "rel", "target"));
                timeline.put(Common.CONTENT, content);

                if (StringUtils.isNotBlank(content)) {
                    timelineMgmtService.addTimeline(timeline);
                }
            }

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final boolean commenterIsArticleAuthor = articleAuthorId.equals(commenterId);

            // 1. '@participants' Notification
            if (commentContent.contains("@participants ")) {
                final List<JSONObject> participants = articleQueryService.getArticleLatestParticipants(
                        UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL, articleId, Integer.MAX_VALUE);
                int count = participants.size();
                if (count < 1) {
                    return;
                }

                count = 0;
                for (final JSONObject participant : participants) {
                    final String participantId = participant.optString(Keys.OBJECT_ID);
                    if (participantId.equals(commenterId)) {
                        continue;
                    }

                    count++;

                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, participantId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                    notificationMgmtService.addAtNotification(requestJSONObject);
                }

                final int sum = count * Pointtransfer.TRANSFER_SUM_C_AT_PARTICIPANTS;
                pointtransferMgmtService.transfer(commenterId, Pointtransfer.ID_C_SYS,
                        Pointtransfer.TRANSFER_TYPE_C_AT_PARTICIPANTS, sum, commentId, System.currentTimeMillis());

                return;
            }

            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);
            atUserNames.remove(commenterName);

            final Set<String> watcherIds = new HashSet<>();
            final int articleWatchCnt = originalArticle.optInt(Article.ARTICLE_WATCH_CNT);
            final JSONObject followerUsersResult =
                    followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL,
                            articleId, 1, Integer.MAX_VALUE);

            final List<JSONObject> watcherUsers = (List<JSONObject>) followerUsersResult.opt(Keys.RESULTS);
            for (final JSONObject watcherUser : watcherUsers) {
                final JSONObject requestJSONObject = new JSONObject();
                final String watcherUserId = watcherUser.optString(Keys.OBJECT_ID);

                watcherIds.add(watcherUserId);
            }
            watcherIds.remove(articleAuthorId);

            if (commenterIsArticleAuthor && atUserNames.isEmpty() && watcherIds.isEmpty() && StringUtils.isBlank(originalCmtId)) {
                return;
            }


            // 2. 'Commented' Notification
            if (!commenterIsArticleAuthor) {
                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addCommentedNotification(requestJSONObject);
            }

            // 3. 'Reply' Notification
            final Set<String> repliedIds = new HashSet<>();
            if (StringUtils.isNotBlank(originalCmtId)) {
                if (!articleAuthorId.equals(originalCmtAuthorId)) {
                    final JSONObject requestJSONObject = new JSONObject();
                    requestJSONObject.put(Notification.NOTIFICATION_USER_ID, originalCmtAuthorId);
                    requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                    notificationMgmtService.addReplyNotification(requestJSONObject);

                    repliedIds.add(originalCmtAuthorId);
                }
            }

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> articleContentAtUserNames = userQueryService.getUserNames(articleContent);

            // 4. 'At' Notification
            final Set<String> atIds = new HashSet<>();
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
                    continue; // Has notified in step 2
                }

                final String userId = user.optString(Keys.OBJECT_ID);
                if (repliedIds.contains(userId)) {
                    continue;
                }

                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, userId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addAtNotification(requestJSONObject);

                atIds.add(userId);
            }

            // 5. 'following - article comment' Notification
            for (final String userId : watcherIds) {
                final JSONObject watcher = userRepository.get(userId);
                final String watcherName = watcher.optString(User.USER_NAME);

                if ((isDiscussion && !articleContentAtUserNames.contains(watcherName)) || commenterName.equals(watcherName)
                        || repliedIds.contains(userId) || atIds.contains(userId)) {
                    continue;
                }

                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, userId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, commentId);

                notificationMgmtService.addFollowingArticleCommentNotification(requestJSONObject);
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
