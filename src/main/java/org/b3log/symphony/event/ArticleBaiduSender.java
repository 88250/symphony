/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.ioc.Singleton;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Sends an article URL to Baidu.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.4, Jan 12, 2019
 * @since 1.3.0
 */
@Singleton
public class ArticleBaiduSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ArticleBaiduSender.class);

    /**
     * Sends the specified URLs to Baidu.
     *
     * @param urls the specified URLs
     */
    public static void sendToBaidu(final String... urls) {
        if (Latkes.RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(Symphonys.BAIDU_DATA_TOKEN)) {
            return;
        }

        if (ArrayUtils.isEmpty(urls)) {
            return;
        }

        Symphonys.EXECUTOR_SERVICE.submit(() -> {
            try {
                final String urlsStr = StringUtils.join(urls, "\n");
                final HttpResponse response = HttpRequest.post("http://data.zz.baidu.com/urls?site=" + Latkes.getServerHost() + "&token=" + Symphonys.BAIDU_DATA_TOKEN).
                        header(Common.USER_AGENT, "curl/7.12.1").
                        header("Host", "data.zz.baidu.com").
                        header("Content-Type", "text/plain").
                        header("Connection", "close").body(urlsStr.getBytes(), MimeTypes.MIME_TEXT_PLAIN).timeout(30000).send();
                response.charset("UTF-8");
                LOGGER.info("Sent [" + urlsStr + "] to Baidu [response=" + response.bodyText() + "]");
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Ping Baidu spider failed", e);
            }
        });
    }

    @Override
    public void action(final Event<JSONObject> event) {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event [type={}, data={}]", event.getType(), data);

        if (Latkes.RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(Symphonys.BAIDU_DATA_TOKEN)) {
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
            LOGGER.log(Level.ERROR, "Sends the article to Baidu failed", e);
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
