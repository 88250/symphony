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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.logging.Logger;
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
 * Timeline channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Sep 11, 2015
 * @since 1.3.0
 */
@WebSocket
public class TimelineChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TimelineChannel.class.getName());

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
        removeSession(session);
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
        removeSession(session);
    }

    /**
     * Notifies the specified comment message to browsers.
     *
     * @param message the specified message, for example      <pre>
     * {
     *     "commentContent": ""
     * }
     * </pre>
     */
    public static void notifyTimeline(final JSONObject message) {
        final String msgStr = message.toString();

        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();

        synchronized (SESSIONS) {
            for (final Session session : SESSIONS) {
                if (session.isOpen()) {
                    session.getRemote().sendStringByFuture(msgStr);
                }
            }
        }
    }

    /**
     * Timeline channel WebSocket servlet.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Aug 18, 2015
     * @since 1.3.0
     */
    @WebServlet("/timeline-channel")
    public static class TimelineChannelWebSocketServlet extends WebSocketServlet {

        @Override
        public void configure(final WebSocketServletFactory factory) {
            factory.register(TimelineChannel.class);
        }
    }

    /**
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
        SESSIONS.remove(session);
    }
}
