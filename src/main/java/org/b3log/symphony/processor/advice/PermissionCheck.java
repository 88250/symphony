/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
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
package org.b3log.symphony.processor.advice;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.symphony.model.Permission;
import org.b3log.symphony.model.Role;
import org.b3log.symphony.service.RoleQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Permission check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Dec 12, 2016
 * @since 1.8.0
 */
@Named
@Singleton
public class PermissionCheck extends BeforeRequestProcessAdvice {

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

        final Set<String> keys = Symphonys.CFG.keySet();
        for (final String key : keys) {
            if (key.startsWith(prefix)) {
                final String urlMethod = key.substring(prefix.length());
                final String value = Symphonys.CFG.getString(key);
                final Set<String> permissions = new HashSet<>(Arrays.asList(value.split(",")));

                URL_PERMISSION_RULES.put(urlMethod, permissions);
            }
        }
    }

    /**
     * Role query service.
     */
    @Inject
    private RoleQueryService roleQueryService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        final String prefix = "permission.rule.url.";
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();
        final String rule = prefix + requestURI + ":" + method;

        final Set<String> requisitePermissions = URL_PERMISSION_RULES.get(rule);

        Set<String> grantPermissions;
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        if (null != user) {
            grantPermissions = roleQueryService.getUserPermissions(user.optString(Keys.OBJECT_ID));
        } else {
            grantPermissions = roleQueryService.getPermissions(Role.ROLE_ID_C_VISITOR);
        }

        if (!Permission.hasPermission(requisitePermissions, grantPermissions)) {
            throw new RequestProcessAdviceException(exception);
        }
    }
}
