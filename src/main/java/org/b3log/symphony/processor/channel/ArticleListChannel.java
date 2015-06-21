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
package org.b3log.symphony.processor.channel;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Article;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONObject;

/**
 * Article list channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jun 21, 2015
 * @since 1.3.0
 */
@WebSocket
public class ArticleListChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleListChannel.class.getName());

    /**
     * Session articles &lt;session, "articleId1,articleId2"&gt;.
     */
    public static final Map<Session, String> SESSIONS = Collections.synchronizedMap(new HashMap<Session, String>());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnWebSocketConnect
    public void onConnect(final Session session) {
        final String articleIds = (String) Channels.getHttpParameter(session, Article.ARTICLE_T_IDS);
        if (StringUtils.isBlank(articleIds)) {
            return;
        }

        SESSIONS.put(session, articleIds);
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
     * @param statusCode status code
     * @param reason reason
     */
    @OnWebSocketClose
    public void onClose(final Session session, final int statusCode, final String reason) {
        SESSIONS.remove(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @OnWebSocketMessage
    public void onMessage(final String message) {
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error error
     */
    @OnWebSocketError
    public void onError(final Session session, final Throwable error) {
        SESSIONS.remove(session);
    }

    /**
     * Notifies the specified article heat message to browsers.
     *
     * @param message the specified message, for example      <pre>
     * {
     *     "articleId": "",
     *     "operation": "" // "+"/"-"
     * }
     * </pre>
     */
    public static void notifyHeat(final JSONObject message) {
        final String articleId = message.optString(Article.ARTICLE_T_ID);
        final String msgStr = message.toString();

        for (final Map.Entry<Session, String> entry : SESSIONS.entrySet()) {
            final Session session = entry.getKey();
            final String articleIds = entry.getValue();

            if (!StringUtils.contains(articleIds, articleId)) {
                continue;
            }

            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(msgStr);
                }
            } catch (final IOException e) {
                LOGGER.log(Level.ERROR, "Notify comment error", e);
            }
        }
    }

    /**
     * Article list channel WebSocket servlet.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Jun 21, 2015
     * @since 1.3.0
     */
    @WebServlet("/article-list-channel")
    public static class ArticleChannelWebSocketServlet extends WebSocketServlet {

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(ArticleListChannel.class);
        }
    }
}
