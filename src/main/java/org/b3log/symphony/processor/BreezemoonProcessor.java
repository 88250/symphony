/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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

import org.b3log.latke.Keys;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.model.Breezemoon;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.PermissionCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.service.BreezemoonMgmtService;
import org.b3log.symphony.service.BreezemoonQueryService;
import org.b3log.symphony.util.Headers;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * Breezemoon processor. https://github.com/b3log/symphony/issues/507
 *
 * <ul>
 * <li>Adds a breezemoon (/bm), POST</li>
 * <li>Updates a breezemoon (/bm/{id}), PUT</li>
 * <li>Removes a breezemoon (/bm/{id}), DELETE</li>
 * <li>Shows a breezemoon (/bm/{id}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 21, 2018
 * @since 2.8.0
 */
@RequestProcessor
public class BreezemoonProcessor {

    /**
     * Breezemoon query service.
     */
    @Inject
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Breezemoon management service.
     */
    @Inject
    private BreezemoonMgmtService breezemoonMgmtService;

    /**
     * Adds a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/bm", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addBreezemoon(final HTTPRequestContext context, final HttpServletRequest request, final JSONObject requestJSONObject) {
        context.renderJSON();

        final JSONObject breezemoon = new JSONObject();
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

        try {
            breezemoonMgmtService.addBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Updates a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/bm/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateBreezemoon(final HTTPRequestContext context, final HttpServletRequest request, final JSONObject requestJSONObject,
                                 final String id) {
        context.renderJSON();

        final JSONObject breezemoon = new JSONObject();
        breezemoon.put(Keys.OBJECT_ID, id);
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

        try {
            breezemoonMgmtService.updateBreezemoon(breezemoon);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Removes a breezemoon.
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/bm/{id}", method = HTTPRequestMethod.DELETE)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void removeBreezemoon(final HTTPRequestContext context, final HttpServletRequest request, final JSONObject requestJSONObject,
                                 final String id) {
        context.renderJSON();

        try {
            breezemoonMgmtService.removeBreezemoon(id);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }
}
