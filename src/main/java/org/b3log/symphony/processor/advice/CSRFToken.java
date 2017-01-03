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
package org.b3log.symphony.processor.advice;

import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.AfterRequestProcessAdvice;
import org.b3log.latke.servlet.renderer.AbstractHTTPResponseRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.util.Sessions;

/**
 * Fills CSRF token.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 27, 2015
 * @since 1.3.0
 */
@Named
@Singleton
public class CSRFToken extends AfterRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CSRFToken.class.getName());

    @Override
    public void doAdvice(final HTTPRequestContext context, final Object ret) {
        final AbstractHTTPResponseRenderer renderer = context.getRenderer();
        if (null != renderer) {
            final Map<String, Object> dataModel = renderer.getRenderDataModel();

            dataModel.put(Common.CSRF_TOKEN, Sessions.getCSRFToken(context.getRequest()));
        }
    }
}
