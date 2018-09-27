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

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.List;
import java.util.Map;

/**
 * Channel utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.2.3, Sep 27, 2018
 * @since 1.4.0
 */
public final class Channels {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Channels.class);

    /**
     * WebSocket configurator.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.1, Sep 27, 2018
     * @since 1.4.0
     */
    public static class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

        @Override
        public void modifyHandshake(final ServerEndpointConfig config, final HandshakeRequest request, final HandshakeResponse response) {
            final HttpSession httpSession = (HttpSession) request.getHttpSession();
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
            final String skin = (String) httpSession.getAttribute(Keys.TEMAPLTE_DIR_NAME);
            config.getUserProperties().put(Keys.TEMAPLTE_DIR_NAME, skin);
        }
    }

    /**
     * Gets a parameter of the specified HTTP session by the given session.
     *
     * @param session       the given session
     * @param parameterName the specified parameter name
     * @return parameter value, returns {@code null} if the parameter does not exist
     */
    public static String getHttpParameter(final Session session, final String parameterName) {
        final Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        for (final String key : parameterMap.keySet()) {
            if (!key.equals(parameterName)) {
                continue;
            }

            final List<String> values = parameterMap.get(key);
            if (null != values && !values.isEmpty()) {
                return values.get(0);
            }
        }

        return null;
    }

    /**
     * Gets an attribute of the specified HTTP session by the given session.
     *
     * @param session       the given session
     * @param attributeName the specified attribute name
     * @return attribute, returns {@code null} if not found or occurred exception
     */
    public static Object getHttpSessionAttribute(final Session session, final String attributeName) {
        final HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());
        if (null == httpSession) {
            return null;
        }

        try {
            return httpSession.getAttribute(attributeName);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Private constructor.
     */
    private Channels() {
    }

}
