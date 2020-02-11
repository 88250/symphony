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
package org.b3log.symphony.processor.middleware;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractResponseRenderer;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.util.Sessions;
import org.json.JSONObject;

import java.util.Map;

/**
 * Fills and checks CSRF token.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 1.3.0
 */
@Singleton
public class CSRFMidware {

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    public void fill(final RequestContext context) {
        context.handle();

        final AbstractResponseRenderer renderer = context.getRenderer();
        if (null == renderer) {
            return;
        }
        final Map<String, Object> dataModel = renderer.getRenderDataModel();
        dataModel.put(Common.CSRF_TOKEN, Sessions.getCSRFToken(context));
    }

    public void check(final RequestContext context) {
        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, langPropsService.get("csrfCheckFailedLabel"));
        exception.put(Keys.STATUS_CODE, false);

        // 1. Check Referer
        final String referer = context.header("Referer");
        if (!StringUtils.startsWith(referer, StringUtils.substringBeforeLast(Latkes.getServePath(), ":"))) {
            context.renderJSON(exception);
            context.abort();

            return;
        }

        // 2. Check Token
        final String clientToken = context.header(Common.CSRF_TOKEN);
        final String serverToken = Sessions.getCSRFToken(context);

        if (!StringUtils.equals(clientToken, serverToken)) {
            context.renderJSON(exception);
            context.abort();

            return;
        }

        context.handle();
    }
}
