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
package org.b3log.symphony.processor.channel;

import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Common;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Char room channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Apr 25, 2016
 * @since 1.4.0
 */
@ServerEndpoint(value = "/chat-room-channel", configurator = Channels.WebSocketConfigurator.class)
public class ChatRoomChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ChatRoomChannel.class);

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

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session s = i.next();

                if (s.isOpen()) {
                    final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                    s.getAsyncRemote().sendText(msgStr);
                }
            }
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session     session
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
     * @param error   error
     */
    @OnError
    public void onError(final Session session, final Throwable error) {
        removeSession(session);
    }

    /**
     * Notifies the specified chat message to browsers.
     *
     * @param message the specified message, for example      <pre>
     *                {
     *                    "userName": "",
     *                    "content": ""
     *                }
     *                </pre>
     */
    public static void notifyChat(final JSONObject message) {
        message.put(Common.TYPE, "msg");
        final String msgStr = message.toString();

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session session = i.next();

                if (session.isOpen()) {
                    session.getAsyncRemote().sendText(msgStr);
                }
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

        synchronized (SESSIONS) {
            final Iterator<Session> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final Session s = i.next();

                if (s.isOpen()) {
                    final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                    s.getAsyncRemote().sendText(msgStr);
                }
            }
        }
    }
}
