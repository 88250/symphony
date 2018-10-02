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
package org.b3log.symphony.processor.advice.stopwatch;

import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Stopwatchs;

import java.util.Map;

/**
 * Stopwatch start advice for request processors.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Oct 17, 2012
 * @since 0.2.0
 */
@Service
public class StopwatchStartAdvice extends BeforeRequestProcessAdvice {

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) {
        final String requestURI = context.getRequest().getRequestURI();
        Stopwatchs.start("Request URI [" + requestURI + ']');
    }
}
