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
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Locales;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.service.DataModelService;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.StatusCodes;
import org.json.JSONObject;

import java.util.Map;

/**
 * Error processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Feb 11, 2020
 * @since 0.2.0
 */
@Singleton
public class ErrorProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ErrorProcessor.class);

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
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    /**
     * Handles the error.
     *
     * @param context the specified context
     */
    public void handle(final RequestContext context) {
        final String statusCode = context.pathVar("statusCode");
        if (StringUtils.equals("GET", context.method())) {
            final String requestURI = context.requestURI();
            final String templateName = statusCode + ".ftl";
            LOGGER.log(Level.TRACE, "Shows error page[requestURI={}, templateName={}]", requestURI, templateName);

            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(context, "error/" + templateName);
            final Map<String, Object> dataModel = renderer.getDataModel();
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
            dataModelService.fillHeaderAndFooter(context, dataModel);
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillRandomArticles(dataModel);
            dataModelService.fillSideTags(dataModel);

            final JSONObject user = Sessions.getUser();
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Map<String, JSONObject> permissionsGrant = roleQueryService.getPermissionsGrantMap(roleId);
            dataModel.put(Permission.PERMISSIONS, permissionsGrant);

            dataModel.put(Common.ELAPSED, 0);

            final Map<String, Object> contextDataModel = (Map<String, Object>) context.attr("dataModel");
            if (null != contextDataModel) {
                dataModel.putAll(contextDataModel);
            }
        } else {
            context.renderJSON(StatusCodes.ERR).renderMsg(statusCode);
        }
    }
}
