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

import java.net.URL;
import java.net.URLEncoder;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Sends an article to QQ qun via <a href="https://github.com/b3log/xiaov">XiaoV</a>.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.2, May 29, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class ArticleQQSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleQQSender.class.getName());

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, ArticleQQSender.class.getName()});

        if (!Symphonys.getBoolean("xiaov.enabled")) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String title = article.optString(Article.ARTICLE_TITLE);
            final String permalink = article.optString(Article.ARTICLE_PERMALINK);
            final String msg = title + " " + Latkes.getServePath() + permalink;
            sendToXiaoV(msg);

        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to QQ group error", e);
        }
    }

    private void sendToXiaoV(final String msg) {
        final String xiaovAPI = Symphonys.get("xiaov.api");
        final String xiaovKey = Symphonys.get("xiaov.key");

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(xiaovAPI + "/qq"));

            final String body = "key=" + URLEncoder.encode(xiaovKey, "UTF-8")
                    + "&msg=" + URLEncoder.encode(msg, "UTF-8")
                    + "&user=" + URLEncoder.encode("sym", "UTF-8");
            request.setPayload(body.getBytes("UTF-8"));

            final HTTPResponse response = URL_FETCH_SVC.fetch(request);
            final int sc = response.getResponseCode();
            if (HttpServletResponse.SC_OK != sc) {
                LOGGER.warn("Sends message to XiaoV status code is [" + sc + "]");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends message to XiaoV failed: " + e.getMessage());
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
