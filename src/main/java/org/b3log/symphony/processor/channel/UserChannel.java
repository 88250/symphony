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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.json.JSONObject;

/**
 * User channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.0, Jul 22, 2016
 * @since 1.4.0
 */
@ServerEndpoint(value = "/user-channel", configurator = Channels.WebSocketConfigurator.class)
public class UserChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserChannel.class.getName());

    /**
     * Session set.
     */
    public static final Map<String, Set<Session>> SESSIONS = new ConcurrentHashMap();

    /**
     * Called when the socket connection with the browser is established.
     *
     * @param session session
     */
    @OnOpen
    public void onConnect(final Session session) {
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);

        Set<Session> userSessions = SESSIONS.get(userId);
        if (null == userSessions) {
            userSessions = Collections.newSetFromMap(new ConcurrentHashMap());
        }
        userSessions.add(session);

        SESSIONS.put(userId, userSessions);

        updateUserOnlineFlag(userId, true);
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
     * @param session session
     */
    @OnMessage
    public void onMessage(final String message, final Session session) {
        JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        updateUserOnlineFlag(user.optString(Keys.OBJECT_ID), true);
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
     * Removes the specified session.
     *
     * @param session the specified session
     */
    private void removeSession(final Session session) {
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);

        Set<Session> userSessions = SESSIONS.get(userId);
        if (null == userSessions) {
            updateUserOnlineFlag(userId, false);

            return;
        }

        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            updateUserOnlineFlag(userId, false);

            return;
        }
    }

    private void updateUserOnlineFlag(final String userId, final boolean online) {
        final LatkeBeanManager beanManager = LatkeBeanManagerImpl.getInstance();
        final UserRepository userRepository = beanManager.getReference(UserRepository.class);

        final Transaction transaction = userRepository.beginTransaction();
        try {
            final JSONObject user = userRepository.get(userId);
            user.put(UserExt.USER_ONLINE_FLAG, online);
            user.put(UserExt.USER_LATEST_LOGIN_TIME, System.currentTimeMillis());

            userRepository.update(userId, user);

            transaction.commit();
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Update user error", e);
        } finally {
            JdbcRepository.dispose();
        }
    }
}
