/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.event;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.ioc.inject.Named;
import org.b3log.latke.ioc.inject.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.*;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.net.URL;

/**
 * Sends an article URL to Baidu.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.1, Apr 19, 2017
 * @since 1.3.0
 */
@Named
@Singleton
public class ArticleBaiduSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleBaiduSender.class);

    /**
     * Baidu data token.
     */
    private static final String TOKEN = Symphonys.get("baidu.data.token");

    /**
     * Sends the specified URLs to Baidu.
     *
     * @param urls the specified URLs
     */
    public static void sendToBaidu(final String... urls) {
        if (ArrayUtils.isEmpty(urls)) {
            return;
        }

        new Thread(() -> {
            try {
                final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

                final HTTPRequest request = new HTTPRequest();
                request.setURL(new URL("http://data.zz.baidu.com/urls?site=" + Latkes.getServerHost() + "&token=" + TOKEN));
                request.setRequestMethod(HTTPRequestMethod.POST);
                request.addHeader(new HTTPHeader(Common.USER_AGENT, "curl/7.12.1"));
                request.addHeader(new HTTPHeader("Host", "data.zz.baidu.com"));
                request.addHeader(new HTTPHeader("Content-Type", "text/plain"));
                request.addHeader(new HTTPHeader("Connection", "close"));

                final String urlsStr = StringUtils.join(urls, "\n");
                request.setPayload(urlsStr.getBytes());

                final HTTPResponse response = urlFetchService.fetch(request);
                LOGGER.info(new String(response.getContent(), "UTF-8"));

                LOGGER.debug("Sent [" + urlsStr + "] to Baidu");
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Ping Baidu spider failed", e);
            }
        }).start();
    }

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={0}, data={1}]", event.getType(), data);

        if (Latkes.RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(TOKEN)) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String tags = article.optString(Article.ARTICLE_TAGS);
            if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
                return;
            }

            final String articlePermalink = Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK);

            sendToBaidu(articlePermalink);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to Baidu error", e);
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
