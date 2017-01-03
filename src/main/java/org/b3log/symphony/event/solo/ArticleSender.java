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
package org.b3log.symphony.event.solo;

import java.net.URL;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Networks;
import org.json.JSONObject;

/**
 * Sends article to client.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.0.2.3, Nov 22, 2016
 * @since 0.2.0
 */
public final class ArticleSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleSender.class.getName());

    /**
     * URL fetch service.
     */
    private URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, ArticleSender.class.getName()});

        if (Latkes.getServePath().contains("localhost") || Networks.isIPv4(Latkes.getServerHost())
                || RuntimeMode.DEVELOPMENT == Latkes.getRuntimeMode()) {
            LOGGER.log(Level.TRACE, "Do not sync in DEV env");

            return;
        }

        try {
            if (data.optBoolean(Common.FROM_CLIENT)) {
                return;
            }

            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);

            if (!originalArticle.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT)) {
                return;
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == originalArticle.optInt(Article.ARTICLE_TYPE)
                    || Article.ARTICLE_TYPE_C_THOUGHT == originalArticle.optInt(Article.ARTICLE_TYPE)) {
                return;
            }

            final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
            final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);

            final String authorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userQueryService.getUser(authorId);
            final String clientURL = author.optString(UserExt.USER_B3_CLIENT_ADD_ARTICLE_URL);

            if (Strings.isEmptyOrNull(clientURL)) {
                return;
            }

            final HTTPRequest httpRequest = new HTTPRequest();
            httpRequest.setURL(new URL(clientURL));
            httpRequest.setRequestMethod(HTTPRequestMethod.POST);
            final JSONObject requestJSONObject = new JSONObject();
            final JSONObject article = new JSONObject(originalArticle, new String[]{
                Article.ARTICLE_CONTENT,
                Article.ARTICLE_TAGS,
                Article.ARTICLE_TITLE,
                Keys.OBJECT_ID
            });

            article.put(UserExt.USER_B3_KEY, author.optString(UserExt.USER_B3_KEY));
            article.put(Article.ARTICLE_EDITOR_TYPE, "CodeMirror-Markdown");

            requestJSONObject.put(Article.ARTICLE, article);
            httpRequest.setPayload(requestJSONObject.toString().getBytes("UTF-8"));

            urlFetchService.fetchAsync(httpRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends an article to client error: {0}", e.getMessage());
        }

        LOGGER.log(Level.DEBUG, "Sent an article to client");
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
