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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.util.Sessions;
import org.json.JSONObject;

/**
 * CSRF check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Apr 19, 2016
 * @since 1.3.0
 */
@Named
@Singleton
public class CSRFCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CSRFCheck.class.getName());
    
    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, langPropsService.get("csrfCheckFailedLabel"));
        exception.put(Keys.STATUS_CODE, false);

        // 1. Check Referer
        final String referer = request.getHeader("Referer");
        if (!StringUtils.startsWith(referer, Latkes.getServePath())) {
            throw new RequestProcessAdviceException(exception);
        }

        // 2. Check Token
        final String clientToken = request.getHeader(Common.CSRF_TOKEN);
        final String serverToken = Sessions.getCSRFToken(request);

        if (!StringUtils.equals(clientToken, serverToken)) {
            throw new RequestProcessAdviceException(exception);
        }
    }
}
