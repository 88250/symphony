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
package org.b3log.symphony.processor.advice;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.logging.Level;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * Login check. Gets user from request attribute named "user" if logged in.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.2, Jun 28, 2015
 * @since 0.2.5
 */
@Named
@Singleton
public class LoginCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginCheck.class.getName());

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        try {
            JSONObject currentUser = userQueryService.getCurrentUser(request);
            if (null == currentUser && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                throw new RequestProcessAdviceException(exception);
            }

            currentUser = userQueryService.getCurrentUser(request);
            final int point = currentUser.optInt(UserExt.USER_POINT);
            final int appRole = currentUser.optInt(UserExt.USER_APP_ROLE);
            if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
                currentUser.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                currentUser.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
            }

            request.setAttribute(User.USER, currentUser);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Login check failed");

            throw new RequestProcessAdviceException(exception);
        }
    }
}
