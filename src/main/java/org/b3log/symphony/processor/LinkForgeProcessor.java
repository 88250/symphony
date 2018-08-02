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

import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Link;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.LinkForgeQueryService;
import org.b3log.symphony.service.OptionQueryService;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Link forge processor.
 * <ul>
 * <li>Shows link forge (/link-forge), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.1.0.10, Aug 2, 2018
 * @since 1.6.0
 */
@RequestProcessor
public class LinkForgeProcessor {

    /**
     * Link forge query service.
     */
    @Inject
    private LinkForgeQueryService linkForgeQueryService;

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Shows link forge.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/forge/link", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showLinkForge(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);

        renderer.setTemplateName("other/link-forge.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        // TODO https://github.com/b3log/symphony/issues/686

        dataModel.put(Common.SELECTED, Common.FORGE);

        final JSONObject statistic = optionQueryService.getStatistic();
        final int tagCnt = statistic.optInt(Option.ID_C_STATISTIC_TAG_COUNT);
        dataModel.put(Tag.TAG_T_COUNT, tagCnt);

        final int linkCnt = statistic.optInt(Option.ID_C_STATISTIC_LINK_COUNT);
        dataModel.put(Link.LINK_T_COUNT, linkCnt);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }
}
