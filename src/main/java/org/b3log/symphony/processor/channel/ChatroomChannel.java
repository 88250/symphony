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

import org.b3log.latke.http.WebSocketChannel;
import org.b3log.latke.http.WebSocketSession;
import org.b3log.latke.ioc.Singleton;
import org.b3log.symphony.model.Common;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chatroom channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Nov 6, 2019
 * @since 1.4.0
 */
@Singleton
public class ChatroomChannel implements WebSocketChannel {

    /**
     * Session set.
     */
    public static final Set<WebSocketSession> SESSIONS = Collections.newSetFromMap(new ConcurrentHashMap());

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @Override
    public void onConnect(final WebSocketSession session) {
        SESSIONS.add(session);

        synchronized (SESSIONS) {
            final Iterator<WebSocketSession> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final WebSocketSession s = i.next();
                final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                s.sendText(msgStr);
            }
        }
    }

    /**
     * Called when the connection closed.
     *
     * @param session session
     */
    @Override
    public void onClose(final WebSocketSession session) {
        removeSession(session);
    }

    /**
     * Called when a message received from the browser.
     *
     * @param message message
     */
    @Override
    public void onMessage(final Message message) {
    }

    /**
     * Called when a error received.
     *
     * @param error error
     */
    @Override
    public void onError(final Error error) {
        removeSession(error.session);
    }

    /**
     * Notifies the specified chat message to browsers.
     *
     * @param message the specified message, for example,
     *                {
     *                "userName": "",
     *                "content": ""
     *                }
     */
    public static void notifyChat(final JSONObject message) {
        message.put(Common.TYPE, "msg");
        final String msgStr = message.toString();

        synchronized (SESSIONS) {
            final Iterator<WebSocketSession> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final WebSocketSession session = i.next();
                session.sendText(msgStr);
            }
        }
    }

    /**
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final WebSocketSession session) {
        SESSIONS.remove(session);

        synchronized (SESSIONS) {
            final Iterator<WebSocketSession> i = SESSIONS.iterator();
            while (i.hasNext()) {
                final WebSocketSession s = i.next();
                final String msgStr = new JSONObject().put(Common.ONLINE_CHAT_CNT, SESSIONS.size()).put(Common.TYPE, "online").toString();
                s.sendText(msgStr);
            }
        }
    }
}
