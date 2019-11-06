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
package org.b3log.symphony.processor.channel;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.http.WebSocketChannel;
import org.b3log.latke.http.WebSocketSession;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Article;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Article list channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.3.1, Apr 25, 2016
 * @since 1.3.0
 */
public class ArticleListChannel implements WebSocketChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleListChannel.class);

    /**
     * Session articles &lt;session, "articleId1,articleId2"&gt;.
     */
    public static final Map<WebSocketSession, String> SESSIONS = new ConcurrentHashMap<>();

    /**
     * Notifies the specified article heat message to browsers.
     *
     * @param message the specified message, for example
     *                {
     *                "articleId": "",
     *                "operation": "" // "+"/"-"
     *                }
     */
    public static void notifyHeat(final JSONObject message) {
        final String articleId = message.optString(Article.ARTICLE_T_ID);
        final String msgStr = message.toString();

        for (final Map.Entry<WebSocketSession, String> entry : SESSIONS.entrySet()) {
            final WebSocketSession session = entry.getKey();
            final String articleIds = entry.getValue();
            if (!StringUtils.contains(articleIds, articleId)) {
                continue;
            }

            session.sendText(msgStr);
        }
    }

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @Override
    public void onConnect(final WebSocketSession session) {
        final String articleIds = session.getParameter(Article.ARTICLE_T_IDS);
        if (StringUtils.isBlank(articleIds)) {
            return;
        }

        SESSIONS.put(session, articleIds);
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
     */
    @Override
    public void onClose(final WebSocketSession session) {
        SESSIONS.remove(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @Override
    public void onMessage(final Message message) {
    }
}
