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
package org.b3log.symphony.processor.middleware;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.handler.RouteHandler;
import org.b3log.latke.http.handler.RouteResolution;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Permission check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.1, Feb 12, 2020
 * @since 1.8.0
 */
@Singleton
public class PermissionMidware {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(PermissionMidware.class);

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;
    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    public void check(final RequestContext context) {
        Stopwatchs.start("Check Permissions");
        try {
            final JSONObject exception = new JSONObject();
            exception.put(Keys.MSG, langPropsService.get("noPermissionLabel"));
            exception.put(Keys.STATUS_CODE, 403);

            final String prefix = "permission.rule.url.";
            final String requestURI = StringUtils.substringAfter(context.requestURI(), Latkes.getContextPath());
            final String method = context.method();
            String rule = prefix;

            try {
                final RouteResolution routeResolution = RouteHandler.doMatch(requestURI, method);
                rule += routeResolution.getMatchedUriTemplate() + "." + method;
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Match method failed", e);

                context.sendError(500);
                context.abort();

                return;
            }

            final Set<String> requisitePermissions = Symphonys.URL_PERMISSION_RULES.get(rule);
            if (null == requisitePermissions) {
                context.handle();

                return;
            }

            final JSONObject user = Sessions.getUser();
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final String userName = null != user ? " " + user.optString(User.USER_NAME) + " " : "";
            final Set<String> grantPermissions = roleQueryService.getPermissions(roleId);

            if (!Permission.hasPermission(requisitePermissions, grantPermissions)) {
                final Map<String, Object> errDataModel = new HashMap<>();
                String noPermissionLabel = langPropsService.get("noPermissionLabel");
                final JSONObject role = roleQueryService.getRole(roleId);
                noPermissionLabel = noPermissionLabel.replace("{roleName}", role.optString(Role.ROLE_NAME));
                noPermissionLabel = noPermissionLabel.replace("{user}", userName);
                errDataModel.put("noPermissionLabel", noPermissionLabel);

                context.sendError(403, errDataModel);
                context.abort();

                return;
            }

            context.handle();
        } finally {
            Stopwatchs.end();
        }
    }
}
