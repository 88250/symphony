/*
 * Copyright (c) 2012-2015, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.processor;

import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.JSONRenderer;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.validate.Activity1A0001Validation;
import org.b3log.symphony.service.ActivityMgmtService;
import org.b3log.symphony.util.Filler;
import org.json.JSONObject;

/**
 * Activity processor.
 *
 * <p>
 * <ul>
 * <li>Daily checkin (/activity/daily-checkin), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jul 16, 2015
 * @since 1.3.0
 */
@RequestProcessor
public class ActivityProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityProcessor.class);

    /**
     * Activity management service.
     */
    @Inject
    private ActivityMgmtService activityMgmtService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows activity page.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/activities", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void showActivities(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/activities.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }

    /**
     * Daily checkin.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/activity/daily-checkin", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void dailyCheckin(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        activityMgmtService.dailyCheckin(userId);

        response.sendRedirect("/member/" + user.optString(User.USER_NAME) + "/points");
    }

    /**
     * Shows 1A0001.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/activity/1A0001", method = HTTPRequestMethod.GET)
    @Before(adviceClass = LoginCheck.class)
    public void show1A0001(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/activity/1A0001.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        filler.fillHeaderAndFooter(request, response, dataModel);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);
        filler.fillSideTags(dataModel);
        filler.fillLatestCmts(dataModel);
    }

    /**
     * Bet.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/activity/1A0001", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {LoginCheck.class, Activity1A0001Validation.class})
    public void bet(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final JSONRenderer renderer = new JSONRenderer();
        context.setRenderer(renderer);

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final int smallOrLarge = requestJSONObject.optInt(Common.SMALL_OR_LARGE);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);

        final JSONObject ret = activityMgmtService.bet1A0001(fromId, amount, smallOrLarge);
        renderer.setJSONObject(ret);
    }
}
