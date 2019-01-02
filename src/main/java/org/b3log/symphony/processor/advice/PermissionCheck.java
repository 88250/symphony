/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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
package org.b3log.symphony.processor.advice;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.servlet.RequestContext;
import org.b3log.latke.servlet.advice.ProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.servlet.handler.MatchResult;
import org.b3log.latke.servlet.handler.RouteHandler;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Permission check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.3, Dec 19, 2018
 * @since 1.8.0
 */
@Singleton
public class PermissionCheck extends ProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PermissionCheck.class);

    /**
     * URL permission rules.
     * <p>
     * &lt;"url:method", permissions&gt;
     * </p>
     */
    private static final Map<String, Set<String>> URL_PERMISSION_RULES = new HashMap<>();

    static {
        // Loads permission URL rules
        final String prefix = "permission.rule.url.";

        final Set<String> keys = Symphonys.CFG.stringPropertyNames();
        for (final String key : keys) {
            if (key.startsWith(prefix)) {
                final String value = Symphonys.CFG.getProperty(key);
                final Set<String> permissions = new HashSet<>(Arrays.asList(value.split(",")));

                URL_PERMISSION_RULES.put(key, permissions);
            }
        }
    }

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

    @Override
    public void doAdvice(final RequestContext context) throws RequestProcessAdviceException {
        Stopwatchs.start("Check Permissions");

        try {
            final HttpServletRequest request = context.getRequest();

            final JSONObject exception = new JSONObject();
            exception.put(Keys.MSG, langPropsService.get("noPermissionLabel"));
            exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

            final String prefix = "permission.rule.url.";
            final String requestURI = StringUtils.substringAfter(context.requestURI(), Latkes.getContextPath());
            final String method = context.method();
            String rule = prefix;

            try {
                final MatchResult matchResult = RouteHandler.doMatch(requestURI, method);
                rule += matchResult.getMatchedUriTemplate() + "." + method;
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Match method failed", e);

                throw new RequestProcessAdviceException(exception);
            }

            final Set<String> requisitePermissions = URL_PERMISSION_RULES.get(rule);
            if (null == requisitePermissions) {
                return;
            }

            final JSONObject user = (JSONObject) context.attr(Common.CURRENT_USER);
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Set<String> grantPermissions = roleQueryService.getPermissions(roleId);

            if (!Permission.hasPermission(requisitePermissions, grantPermissions)) {
                throw new RequestProcessAdviceException(exception);
            }
        } finally {
            Stopwatchs.end();
        }
    }
}
