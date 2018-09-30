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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserMgmtService;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User channel.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.2, Sep 3, 2018
 * @since 1.4.0
 */
@ServerEndpoint(value = "/user-channel", configurator = Channels.WebSocketConfigurator.class)
public class UserChannel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserChannel.class);

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

        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);
        try {
            userMgmtService.updateOnlineStatus(userId, ip, true, true);
        } finally {
            JdbcRepository.dispose();
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
     * @param session session
     */
    @OnMessage
    public void onMessage(final String message, final Session session) {
        JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);
        try {
            userMgmtService.updateOnlineStatus(userId, ip, true, true);
        } finally {
            JdbcRepository.dispose();
        }
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
     * Sends command to browsers.
     *
     * @param message the specified message, for example,
     *                "userId": "",
     *                "cmd": ""
     */
    public static void sendCmd(final JSONObject message) {
        final String recvUserId = message.optString(UserExt.USER_T_ID);
        if (StringUtils.isBlank(recvUserId)) {
            return;
        }

        final String msgStr = message.toString();

        for (final String userId : SESSIONS.keySet()) {
            if (userId.equals(recvUserId)) {
                final Set<Session> sessions = SESSIONS.get(userId);

                for (final Session session : sessions) {
                    if (session.isOpen()) {
                        session.getAsyncRemote().sendText(msgStr);
                    }
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
        final JSONObject user = (JSONObject) Channels.getHttpSessionAttribute(session, User.USER);
        if (null == user) {
            return;
        }

        final String userId = user.optString(Keys.OBJECT_ID);
        final BeanManager beanManager = BeanManager.getInstance();
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);
        final String ip = (String) Channels.getHttpSessionAttribute(session, Common.IP);

        Set<Session> userSessions = SESSIONS.get(userId);
        if (null == userSessions) {
            try {
                userMgmtService.updateOnlineStatus(userId, ip, false, false);
            } finally {
                JdbcRepository.dispose();
            }

            return;
        }

        userSessions.remove(session);
        if (userSessions.isEmpty()) {
            try {
                userMgmtService.updateOnlineStatus(userId, ip, false, false);
            } finally {
                JdbcRepository.dispose();
            }

            return;
        }
    }
}
