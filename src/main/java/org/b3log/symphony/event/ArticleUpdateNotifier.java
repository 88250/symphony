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
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Sends article update related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jan 20, 2017
 * @since 2.0.0
 */
@Named
@Singleton
public class ArticleUpdateNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleUpdateNotifier.class);

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
                event.getType(), data, ArticleUpdateNotifier.class.getName());

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final String articleId = originalArticle.optString(Keys.OBJECT_ID);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final String tags = originalArticle.optString(Article.ARTICLE_TAGS);

            // 'following - article update' Notification
            final JSONObject followerUsersResult =
                    followQueryService.getArticleWatchers(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL,
                            articleId, 1, Integer.MAX_VALUE);

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

            // Timeline
            final String articleTitle = Jsoup.parse(originalArticle.optString(Article.ARTICLE_TITLE)).text();
            final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.USER_ID, articleAuthorId);
            timeline.put(Common.TYPE, Article.ARTICLE);
            String content = langPropsService.get("timelineArticleUpdateLabel");

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
