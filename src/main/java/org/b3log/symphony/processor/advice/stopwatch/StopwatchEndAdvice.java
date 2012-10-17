/*
 * Copyright (c) 2012, B3log Team
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.AfterRequestProcessAdvice;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;

/**
 * Stopwatch end advice for request processors.
 * 
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Oct 17, 2012
 * @since 0.2.0
 */
public final class StopwatchEndAdvice extends AfterRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(StopwatchEndAdvice.class.getName());

    @Override
    public void doAdvice(final HTTPRequestContext context, final Object ret) {
        Stopwatchs.end();
        LOGGER.log(Level.FINE, "Stopwatch: {0}    {1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
    }
}
