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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
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
 * @version 1.0.0.2, Aug 29, 2018
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
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/forward", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showForward(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        String to = request.getParameter(Common.GOTO);
        if (StringUtils.isBlank(to)) {
            to = Latkes.getServePath();
        }

        final String url = to;
        Symphonys.EXECUTOR_SERVICE.submit(() -> {
            try {
                linkMgmtService.addLink(url);
            } finally {
                JdbcRepository.dispose();
            }
        });

        final JSONObject user = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != user && UserExt.USER_XXX_STATUS_C_DISABLED == user.optInt(UserExt.USER_FORWARD_PAGE_STATUS)) {
            response.sendRedirect(to);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("forward.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModel.put("forwardURL", to);
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }
}
