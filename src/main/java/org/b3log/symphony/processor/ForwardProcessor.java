/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.servlet.HttpMethod;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.LinkMgmtService;
import org.b3log.symphony.util.Headers;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Forward processor.
 * <ul>
 * <li>Shows forward page (/forward), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.5, Jan 17, 2019
 * @since 2.3.0
 */
@RequestProcessor
public class ForwardProcessor {

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Link management service.
     */
    @Inject
    private LinkMgmtService linkMgmtService;

    /**
     * Shows jump page.
     *
     * @param context the specified context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/forward", method = HttpMethod.GET)
    @Before({StopwatchStartAdvice.class})
    @After({PermissionGrant.class, StopwatchEndAdvice.class})
    public void showForward(final RequestContext context) {
        final HttpServletRequest request = context.getRequest();
        final HttpServletResponse response = context.getResponse();

        String to = context.param(Common.GOTO);
        if (StringUtils.isBlank(to)) {
            to = Latkes.getServePath();
        }

        final String referer = Headers.getHeader(request, "referer", "");
        if (!StringUtils.startsWith(referer, Latkes.getServePath())) {
            context.sendRedirect(Latkes.getServePath());

            return;
        }

        final String url = to;
        Symphonys.EXECUTOR_SERVICE.submit(() -> linkMgmtService.addLink(url));

        final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
        if (null != user && UserExt.USER_XXX_STATUS_C_DISABLED == user.optInt(UserExt.USER_FORWARD_PAGE_STATUS)) {
            context.sendRedirect(to);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "forward.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put("forwardURL", to);
        dataModelService.fillHeaderAndFooter(context, dataModel);
    }
}
