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
package org.b3log.symphony.processor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
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
 * Data channel via WebSocket.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 19, 2015
 * @since 1.3.0
 */
@WebSocket
public class DataChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(DataChannel.class.getName());

    /**
     * Session set.
     */
    public static final Set<Session> SESSIONS = Collections.synchronizedSet(new HashSet<Session>());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnWebSocketConnect
    public void onConnect(final Session session) {
        final HttpSession httpSession = (HttpSession) session.getUpgradeRequest().getSession();
        if (null == httpSession) {
            session.close();

            return;
        }

        final JSONObject user = (JSONObject) httpSession.getAttribute(User.USER);
        if (null == user) {
            session.close();

            return;
        }

        SESSIONS.add(session);
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
        LOGGER.log(Level.ERROR, "Data channel error", error);

        SESSIONS.remove(session);
    }

    /**
     * Broadcasts the specified message to all browsers.
     *
     * @param message the specified message
     */
    public static void broadcast(final String message) {
        for (final Session session : SESSIONS) {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(message);
                }
            } catch (final IOException e) {
                LOGGER.log(Level.ERROR, "Broadcast message error", e);
            }
        }
    }

    /**
     * Data channel WebSocket servlet.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Jun 19, 2015
     * @since 1.3.0
     */
    @WebServlet("/data-channel")
    public static class DataChannelWebSocketServlet extends WebSocketServlet {

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(DataChannel.class);
        }
    }
}
