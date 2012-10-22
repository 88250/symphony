/*
 * Copyright (c) 2012, B3log Team
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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sends a comment notification to IM server.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 21, 2012
 * @since 0.2.0
 */
public final class CommentNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentNotifier.class.getName());
    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();
    /**
     * URL fetch service.
     */
    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.FINER, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, CommentNotifier.class.getName()});

//        if (Latkes.getServePath().contains("localhost")) {
//            return;
//        }

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);
            final Set<String> userNames = userQueryService.getUserNames(commentContent);
            userNames.add(articleAuthorName);

            final Set<String> qqSet = new HashSet<String>();
            for (final String userName : userNames) {
                final JSONObject user = userQueryService.getUserByName(userName);
                final String qq = user.optString(UserExt.USER_QQ);
                if (!Strings.isEmptyOrNull(qq)) {
                    qqSet.add(qq);
                }
            }

            if (qqSet.isEmpty()) {
                return;
            }

            /*
             * {
             *     "key": "",
             *     "messageContent": "",
             *     "messageProcessor": "QQ",
             *     "messageToAccounts": [
             *         "", ....
             *     ]
             * }
             */
            final HTTPRequest httpRequest = new HTTPRequest();
            httpRequest.setURL(new URL(Symphonys.get("imServePath")));
            httpRequest.setRequestMethod(HTTPRequestMethod.PUT);
            final JSONObject requestJSONObject = new JSONObject();
            final JSONArray qqs = CollectionUtils.toJSONArray(qqSet);

            requestJSONObject.put("messageProcessor", "QQ");
            requestJSONObject.put("messageToAccounts", qqs);
            requestJSONObject.put("key", Symphonys.get("keyOfSymphony"));

            final StringBuilder msgContent = new StringBuilder("----\n");
            msgContent.append(originalArticle.optString(Article.ARTICLE_TITLE)).append("\n").append(Latkes.getServePath())
                    .append(originalComment.optString(Comment.COMMENT_SHARP_URL)).append("\n\n")
                    .append(commentContent).append("\n----");

            requestJSONObject.put("messageContent", msgContent.toString());

            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));

            urlFetchService.fetchAsync(httpRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Sends an article to client error: {0}", e.getMessage());
        }

        LOGGER.log(Level.FINER, "Sent an article to client");
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
