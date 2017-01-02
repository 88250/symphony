/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.processor.advice.stopwatch;

import java.util.Map;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.AfterRequestProcessAdvice;
import org.b3log.latke.servlet.renderer.AbstractHTTPResponseRenderer;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;

/**
 * Stopwatch end advice for request processors.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Aug 2, 2015
 * @since 0.2.0
 */
@Service
public class StopwatchEndAdvice extends AfterRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StopwatchEndAdvice.class.getName());

    @Override
    public void doAdvice(final HTTPRequestContext context, final Object ret) {
        Stopwatchs.end();

        final AbstractHTTPResponseRenderer renderer = context.getRenderer();
        if (null != renderer) {
            final Map<String, Object> dataModel = renderer.getRenderDataModel();
            final String requestURI = context.getRequest().getRequestURI();

            final long elapsed = Stopwatchs.getElapsed("Request URI [" + requestURI + ']');
            dataModel.put(Common.ELAPSED, elapsed);
        }

        LOGGER.log(Level.TRACE, "Stopwatch: {0}    {1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
    }
}
