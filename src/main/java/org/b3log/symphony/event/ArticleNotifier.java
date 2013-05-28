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
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sends an article notification to the user who be &#64;username in the article content.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Nov 6, 2012
 * @since 0.2.0
 */
public final class ArticleNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleNotifier.class.getName());
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
                new Object[]{event.getType(), data, ArticleNotifier.class.getName()});

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);

            final String articleAuthorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject articleAuthor = userQueryService.getUser(articleAuthorId);
            final String articleAuthorName = articleAuthor.optString(User.USER_NAME);

            final String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);
            final Set<String> userNames = userQueryService.getUserNames(articleContent);
            userNames.remove(articleAuthorName); // Do not notify the author itself

            if (userNames.isEmpty()) {
                return;
            }

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
                    .append(originalArticle.optString(Article.ARTICLE_PERMALINK)).append("\n\n");

            requestJSONObject.put("messageContent", msgContent.toString());

            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));

            urlFetchService.fetchAsync(httpRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Sends the article notification failed", e);
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
