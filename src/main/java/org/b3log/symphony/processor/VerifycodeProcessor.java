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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.service.VerifycodeMgmtService;
import org.b3log.symphony.util.Symphonys;

/**
 * Verifycode processor.
 *
 * <p>
 * <ul>
 * <li>Send email verifycode (/cron/verifycode/email), GET</li>
 * <li>Send email verifycode (/cron/verifycode/remove-expired), GET</li>
 * </ul>
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Oct 31, 2016
 * @since 1.3.0
 */
@RequestProcessor
public class VerifycodeProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(VerifycodeProcessor.class);

    /**
     * Verifycode management service.
     */
    @Inject
    private VerifycodeMgmtService verifycodeMgmtService;

    /**
     * Sends email register verifycode.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/verifycode/email", method = HTTPRequestMethod.GET)
    public void sendEmailRegisterVerifycode(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        verifycodeMgmtService.sendEmailVerifycode();

        context.renderJSON().renderTrueResult();
    }

    /**
     * Remove expired verifycodes.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/verifycode/remove-expired", method = HTTPRequestMethod.GET)
    public void removeExpriedVerifycodes(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        verifycodeMgmtService.removeExpiredVerifycodes();

        context.renderJSON().renderTrueResult();
    }
}
