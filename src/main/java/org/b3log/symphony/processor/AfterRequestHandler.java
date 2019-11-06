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
package org.b3log.symphony.processor;

import org.b3log.latke.http.Request;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.handler.Handler;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;

import java.util.Map;

/**
 * After request handler.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 3, 2019
 * @since 3.6.0
 */
public class AfterRequestHandler implements Handler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AfterRequestHandler.class);

    @Override
    public void handle(final RequestContext context) {
        Locales.setLocale(null);
        Sessions.clearThreadLocalData();
        Stopwatchs.end();
        final Request request = context.getRequest();
        final long elapsed = Stopwatchs.getElapsed("Request initialized [" + request.getRequestURI() + "]");
        final Map<String, Object> dataModel = context.getDataModel();
        if (null != dataModel) {
            dataModel.put(Common.ELAPSED, elapsed);
        }
        final int threshold = Symphonys.PERFORMANCE_THRESHOLD;
        if (0 < threshold) {
            if (elapsed >= threshold) {
                LOGGER.log(Level.INFO, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
            }
        }
        Stopwatchs.release();

        context.handle();
    }


}
