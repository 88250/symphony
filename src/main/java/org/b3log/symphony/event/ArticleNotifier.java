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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.processor.channel.TimelineChannel;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Sends an article notification to the user who be &#64;username in the article content.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.5, Aug 18, 2015
 * @since 0.2.0
 */
@Named
public class ArticleNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleNotifier.class.getName());

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

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, ArticleNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> atUserNames = userQueryService.getUserNames(articleContent);
            atUserNames.remove(articleAuthorName); // Do not notify the author itself

            final Set<String> atedUserIds = new HashSet<String>();

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
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, originalArticle.optString(Keys.OBJECT_ID));

                notificationMgmtService.addAtNotification(requestJSONObject);

                atedUserIds.add(atedUserId);
            }
            // 'FollowingUser' Notification
            final JSONObject followerUsersResult = followQueryService.getFollowerUsers(articleAuthorId, 1, Integer.MAX_VALUE);
            @SuppressWarnings("unchecked")
            final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
            for (final JSONObject followerUser : followerUsers) {
                final JSONObject requestJSONObject = new JSONObject();
                final String followerUserId = followerUser.optString(Keys.OBJECT_ID);

                if (atedUserIds.contains(followerUserId)) {
                    continue;
                }

                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, followerUserId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, originalArticle.optString(Keys.OBJECT_ID));

                notificationMgmtService.addFollowingUserNotification(requestJSONObject);
            }

            // Timeline
            final String articleTitle = StringUtils.substring(Jsoup.parse(
                    originalArticle.optString(Article.ARTICLE_TITLE)).text(), 0, 28);
            final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.TYPE, Article.ARTICLE);
            String content = langPropsService.get("timelineArticleLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + articleAuthorName + "'>" + articleAuthorName + "</a>")
                    .replace("{article}", "<a target='_blank' rel='nofollow' href='" + articlePermalink
                            + "'>" + articleTitle + "</a>");
            timeline.put(Common.CONTENT, content);
            TimelineChannel.notifyTimeline(timeline);

//            final Set<String> qqSet = new HashSet<String>();
//            for (final String userName : atUserNames) {
//                final JSONObject user = userQueryService.getUserByName(userName);
//                final String qq = user.optString(UserExt.USER_QQ);
//                if (!Strings.isEmptyOrNull(qq)) {
//                    qqSet.add(qq);
//                }
//            }
//
//            if (qqSet.isEmpty()) {
//                return;
//            }
//            /*
//             * {
//             *     "key": "",
//             *     "messageContent": "",
//             *     "messageProcessor": "QQ",
//             *     "messageToAccounts": [
//             *         "", ....
//             *     ]
//             * }
//             */
//            final HTTPRequest httpRequest = new HTTPRequest();
//            httpRequest.setURL(new URL(Symphonys.get("imServePath")));
//            httpRequest.setRequestMethod(HTTPRequestMethod.PUT);
//            final JSONObject requestJSONObject = new JSONObject();
//            final JSONArray qqs = CollectionUtils.toJSONArray(qqSet);
//
//            requestJSONObject.put("messageProcessor", "QQ");
//            requestJSONObject.put("messageToAccounts", qqs);
//            requestJSONObject.put("key", Symphonys.get("keyOfSymphony"));
//
//            final StringBuilder msgContent = new StringBuilder("----\n");
//            msgContent.append(originalArticle.optString(Article.ARTICLE_TITLE)).append("\n").append(Latkes.getServePath())
//                    .append(originalArticle.optString(Article.ARTICLE_PERMALINK)).append("\n\n");
//
//            requestJSONObject.put("messageContent", msgContent.toString());
//
//            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));
//
//            urlFetchService.fetchAsync(httpRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article notification failed", e);
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
