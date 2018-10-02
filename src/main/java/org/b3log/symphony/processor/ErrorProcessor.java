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
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.processor.advice.PermissionGrant;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.DataModelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Error processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.10, Jun 2, 2018
 * @since 0.2.0
 */
@RequestProcessor
public class ErrorProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ErrorProcessor.class);

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Handles the error.
     *
     * @param context    the specified context
     * @param request    the specified HTTP servlet request
     * @param response   the specified HTTP servlet response
     * @param statusCode the specified status code
     */
    @RequestProcessing(value = "/error/{statusCode}", method = {HTTPRequestMethod.GET, HTTPRequestMethod.POST})
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    public void handleErrorPage(final HTTPRequestContext context, final HttpServletRequest request,
                                final HttpServletResponse response, final String statusCode) {
        if (StringUtils.equals("GET", request.getMethod())) {
            final String requestURI = request.getRequestURI();
            final String templateName = statusCode + ".ftl";
            LOGGER.log(Level.TRACE, "Shows error page[requestURI={0}, templateName={1}]", requestURI, templateName);

            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
            renderer.setTemplateName("error/" + templateName);
            context.setRenderer(renderer);

            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillRandomArticles(dataModel);
            dataModelService.fillSideTags(dataModel);
        } else {
            context.renderJSON().renderMsg(statusCode);
        }
    }
}
