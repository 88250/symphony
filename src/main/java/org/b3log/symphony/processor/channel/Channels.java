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

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Channel utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Jun 24, 2015
 * @since 1.3.0
 */
public final class Channels {

    /**
     * Gets a parameter of the specified HTTP session by the given session.
     *
     * @param session the given session
     * @param parameterName the specified parameter name
     * @return parameter value, returns {@code null} if the parameter does not exist
     */
    public static String getHttpParameter(final Session session, final String parameterName) {
        final Map<String, List<String>> parameterMap = session.getUpgradeRequest().getParameterMap();
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
     * @return attribute, returns {@code null} if not found
     */
    public static Object getHttpSessionAttribute(final Session session, final String attributeName) {
        final HttpSession httpSession = (HttpSession) session.getUpgradeRequest().getSession();
        if (null == httpSession) {
            return null;
        }

        return httpSession.getAttribute(attributeName);
    }

    /**
     * Private constructor.
     */
    private Channels() {
    }

}
