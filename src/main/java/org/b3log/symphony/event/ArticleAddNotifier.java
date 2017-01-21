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
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.*;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sends article add related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.3.12, Jan 19, 2017
 * @since 0.2.0
 */
@Named
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
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                event.getType(), data, ArticleAddNotifier.class.getName());

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final Set<String> atedUserIds = new HashSet<>();

            // 'At' Notification
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

            // Timeline
            final String articleTitle = Jsoup.parse(originalArticle.optString(Article.ARTICLE_TITLE)).text();
            final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.USER_ID, articleAuthorId);
            timeline.put(Common.TYPE, Article.ARTICLE);
            String content = langPropsService.get("timelineArticleAddLabel");

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == originalArticle.optInt(Article.ARTICLE_ANONYMOUS)) {
                content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                        + "/member/" + articleAuthorName + "'>" + articleAuthorName + "</a>");
            } else {
                content = content.replace("{user}", UserExt.ANONYMOUS_USER_NAME);
            }
            content = content.replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                    + "'>" + articleTitle + "</a>");
            content = Emotions.convert(content);
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);

            // 'Broadcast' / 'Book' Notification
            if (Article.ARTICLE_TYPE_C_CITY_BROADCAST == originalArticle.optInt(Article.ARTICLE_TYPE)
                    || (Article.ARTICLE_TYPE_C_BOOK == originalArticle.optInt(Article.ARTICLE_TYPE) && !articleAuthorName.equals("book_share"))) {
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
