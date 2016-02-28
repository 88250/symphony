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

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.b3log.latke.logging.Logger;

/**
 * Channel utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.2.2, Feb 28, 2016
 * @since 1.4.0
 */
public final class Channels {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Channels.class.getName());

    /**
     * WebSocket configurator.
     *
     * @author <a href="http://88250.b3log.org">Liang Ding</a>
     * @version 1.0.0.0, Feb 28, 2016
     * @since 1.4.0
     */
    public static class WebSocketConfigurator extends ServerEndpointConfig.Configurator {

        @Override
        public void modifyHandshake(final ServerEndpointConfig config,
                final HandshakeRequest request, final HandshakeResponse response) {
            final HttpSession httpSession = (HttpSession) request.getHttpSession();

            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        }
    }

    /**
     * Gets a parameter of the specified HTTP session by the given session.
     *
     * @param session the given session
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
     * @param session the given session
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
