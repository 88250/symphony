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

import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.RuntimeMode;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPHeader;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Sends an article URL to Baidu.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.2.0, May 16, 2016
 * @since 1.3.0
 */
@Named
@Singleton
public class ArticleBaiduSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleBaiduSender.class.getName());

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, ArticleBaiduSender.class.getName()});

        final String token = Symphonys.get("baidu.data.token");

        if (RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(token)) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);

            sendToBaidu(articlePermalink);

        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to Baidu error", e);
        }
    }

    /**
     * Sends the specified URLs to Baidu.
     *
     * @param urls the specified URLs
     */
    public static void sendToBaidu(final String... urls) {
        if (ArrayUtils.isEmpty(urls)) {
            return;
        }

        final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);
        request.addHeader(new HTTPHeader("User-Agent", "curl/7.12.1"));
        request.addHeader(new HTTPHeader("Host", "data.zz.baidu.com"));
        request.addHeader(new HTTPHeader("Content-Type", "text/plain"));
        request.addHeader(new HTTPHeader("Connection", "close"));

        final String urlsStr = StringUtils.join(urls, "\n");
        request.setPayload(urlsStr.getBytes());

        urlFetchService.fetchAsync(request);

        LOGGER.debug("Sent [" + urlsStr + "] to Baidu");
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
