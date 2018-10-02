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
import org.b3log.latke.ioc.Inject;
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
import org.b3log.symphony.service.ManQueryService;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Man processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.0.4, Jul 3, 2017
 * @since 1.8.0
 */
@RequestProcessor
public class ManProcessor {

    /**
     * TLDR query service.
     */
    @Inject
    private ManQueryService manQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Shows man.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/man", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showMan(final HTTPRequestContext context,
                        final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        if (!ManQueryService.TLDR_ENABLED) {
            response.sendRedirect("https://hacpai.com/man");

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("other/man.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        String cmd = request.getParameter(Common.CMD);
        if (StringUtils.isBlank(cmd)) {
            cmd = "man";
        }

        List<JSONObject> mans = manQueryService.getMansByCmdPrefix(cmd);
        if (mans.isEmpty()) {
            mans = manQueryService.getMansByCmdPrefix("man");
        }

        dataModel.put(Common.MANS, mans);
    }


    /**
     * Lists mans.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/man/cmd", method = HTTPRequestMethod.GET)
    public void listMans(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        context.renderJSON().renderTrueResult();

        final String cmdPrefix = request.getParameter(Common.NAME);
        if (StringUtils.isBlank(cmdPrefix)) {
            return;
        }

        final List<JSONObject> mans = manQueryService.getMansByCmdPrefix(cmdPrefix);

        context.renderJSONValue(Common.MANS, mans);
    }
}
