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
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;

/**
 * User block check. Gets user from request attribute named "user".
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.3.1, Aug 2, 2016
 * @since 0.2.5
 */
@Named
@Singleton
public class UserBlockCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserBlockCheck.class.getName());

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_NOT_FOUND);
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_NOT_FOUND);

        JSONObject user;
        final String userName = (String) args.get("userName");
        if (UserExt.NULL_USER_NAME.equals(userName)) {
            exception.put(Keys.MSG, "Nil User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
            throw new RequestProcessAdviceException(exception);
        }

        try {
            user = userQueryService.getUserByName(userName);
            if (null == user) {
                exception.put(Keys.MSG, "Not found user [" + userName + ", requestURI=" + request.getRequestURI() + "]");
                throw new RequestProcessAdviceException(exception);
            }

            if (UserExt.USER_STATUS_C_NOT_VERIFIED == user.optInt(UserExt.USER_STATUS)) {
                exception.put(Keys.MSG, "Unverified User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
                throw new RequestProcessAdviceException(exception);
            }

            if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
                exception.put(Keys.MSG, "Blocked User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
                throw new RequestProcessAdviceException(exception);
            }

            request.setAttribute(User.USER, user);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "User block check failed");

            throw new RequestProcessAdviceException(exception);
        }
    }
}
