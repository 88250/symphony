/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
 * @version 1.1.2.1, Apr 21, 2016
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
        if (UserExt.DEFAULT_CMTER_NAME.equals(userName) || UserExt.NULL_USER_NAME.equals(userName)) {
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
