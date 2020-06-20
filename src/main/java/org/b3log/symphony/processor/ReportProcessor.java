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
package org.b3log.symphony.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Report;
import org.b3log.symphony.service.ReportMgmtService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

/**
 * Report processor.
 * <ul>
 * <li>Reports content or users (/report), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 3.1.0
 */
@Singleton
public class ReportProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ReportProcessor.class);

    /**
     * Report management service.
     */
    @Inject
    private ReportMgmtService reportMgmtService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Reports content or users.
     *
     * @param context the specified context
     */
    public void report(final RequestContext context) {
        context.renderJSON(StatusCodes.ERR);

        final JSONObject requestJSONObject = context.requestJSON();

        final JSONObject currentUser = Sessions.getUser();
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String dataId = requestJSONObject.optString(Report.REPORT_DATA_ID);
        final int dataType = requestJSONObject.optInt(Report.REPORT_DATA_TYPE);
        final int type = requestJSONObject.optInt(Report.REPORT_TYPE);
        final String memo = StringUtils.trim(requestJSONObject.optString(Report.REPORT_MEMO));

        final JSONObject report = new JSONObject();
        report.put(Report.REPORT_USER_ID, userId);
        report.put(Report.REPORT_DATA_ID, dataId);
        report.put(Report.REPORT_DATA_TYPE, dataType);
        report.put(Report.REPORT_TYPE, type);
        report.put(Report.REPORT_MEMO, memo);

        try {
            reportMgmtService.addReport(report);

            context.renderJSONValue(Keys.CODE, StatusCodes.SUCC);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());
            context.renderJSONValue(Keys.CODE, StatusCodes.ERR);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Adds a report failed", e);

            context.renderMsg(langPropsService.get("systemErrLabel"));
            context.renderJSONValue(Keys.CODE, StatusCodes.ERR);
        }
    }

}
