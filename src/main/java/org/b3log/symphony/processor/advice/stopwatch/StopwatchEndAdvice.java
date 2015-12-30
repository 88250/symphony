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
