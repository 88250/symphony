/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.b3log.latke.Keys;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Notification;
import org.b3log.symphony.service.NotificationMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * Sends a comment notification.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.9, Apr 4, 2014
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
     * URL fetch service.
     */
    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.INFO, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                   new Object[]{event.getType(), data, CommentNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);

            final Set<String> atUserNames = userQueryService.getUserNames(commentContent);
            final boolean commenterIsArticleAuthor = articleAuthorId.equals(originalComment.optString(Comment.COMMENT_AUTHOR_ID));
            if (commenterIsArticleAuthor && atUserNames.isEmpty()) {
                return;
            }

            final JSONObject commenter = userQueryService.getUser(originalComment.optString(Comment.COMMENT_AUTHOR_ID));
            final String commenterName = commenter.optString(User.USER_NAME);
            atUserNames.remove(commenterName); // Do not notify commenter itself

            // 1. 'Commented' Notification
            if (!commenterIsArticleAuthor) {
                final JSONObject requestJSONObject = new JSONObject();
                requestJSONObject.put(Notification.NOTIFICATION_USER_ID, articleAuthorId);
                requestJSONObject.put(Notification.NOTIFICATION_DATA_ID, originalComment.optString(Keys.OBJECT_ID));

                notificationMgmtService.addCommentedNotification(requestJSONObject);
            }

            // 2. 'At' Notification
            for (final String userName : atUserNames) {
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

//            final Set<String> qqSet = new HashSet<String>();
//
//            if (!commenterIsArticleAuthor) {
//                final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
//                final String qq = articleAuthor.optString(UserExt.USER_QQ);
//                if (!Strings.isEmptyOrNull(qq)) {
//                    qqSet.add(qq);
//                }
//            }
//
//            for (final String userName : atUserNames) {
//                final JSONObject user = userQueryService.getUserByName(userName);
//                final String q = user.optString(UserExt.USER_QQ);
//                if (!Strings.isEmptyOrNull(q)) {
//                    qqSet.add(q);
//                }
//            }
//
//            if (qqSet.isEmpty()) {
//                return;
//            }
//
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
//                    .append(originalComment.optString(Comment.COMMENT_SHARP_URL)).append("\n\n")
//                    .append(Jsoup.clean(commentContent.replace("&gt;", ">").replace("&lt;", "<"), Whitelist.none())).append("\n----");
//
//            requestJSONObject.put("messageContent", msgContent.toString());
//
//            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));
//
//            urlFetchService.fetchAsync(httpRequest);
//
//            LOGGER.debug("Sent QQ message [qqs=" + qqs.toString() + ", content=" + requestJSONObject.toString() + "]");
//            
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
