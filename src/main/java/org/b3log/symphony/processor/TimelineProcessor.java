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
package org.b3log.symphony.processor;

import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.advice.AnonymousViewCheck;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.util.Symphonys;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Timeline processor.
 * <p>
 * <ul>
 * <li>Shows timeline (/timeline), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.9, Dec 24, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class TimelineProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TimelineProcessor.class.getName());

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Shows timeline.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/timeline", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void showTimeline(final HTTPRequestContext context,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        ;
        context.setRenderer(renderer);
        renderer.setTemplateName("timeline.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.SELECTED, Common.TIMELINE);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(avatarViewMode, dataModel);
        dataModelService.fillSideHotArticles(avatarViewMode, dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put("timelineCnt", Symphonys.getInt("timelineCnt"));
        dataModel.put(Common.TIMELINES, timelineMgmtService.getTimelines());
    }
}
