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
package org.b3log.symphony.processor.channel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;

/**
 * Timeline channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.3.0, Apr 25, 2016
 * @since 1.3.0
 */
@ServerEndpoint(value = "/timeline-channel", configurator = Channels.WebSocketConfigurator.class)
public class TimelineChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TimelineChannel.class.getName());

    /**
     * Session set.
     */
    public static final Set<Session> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        SESSIONS.add(session);
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
     * @param closeReason close reason
     */
    @OnClose
    public void onClose(final Session session, final CloseReason closeReason) {
        removeSession(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @OnMessage
    public void onMessage(final String message) {
    }

    /**
     * Called in case of an error.
     *
     * @param session session
     * @param error error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        removeSession(session);
    }

    /**
     * Notifies the specified timeline message to browsers.
     *
     * @param message the specified message, for example      <pre>
     * {
     *     "type": "article",
     *     "content": timelineArticleLabel
     * }
     * </pre>
     */
    public static void notifyTimeline(final JSONObject message) {
        final String msgStr = message.toString();

        final Iterator<Session> i = SESSIONS.iterator();
        while (i.hasNext()) {
            final Session session = i.next();

            if (session.isOpen()) {
                session.getAsyncRemote().sendText(msgStr);
            }
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
