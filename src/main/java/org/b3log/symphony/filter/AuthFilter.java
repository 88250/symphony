/*
 * Copyright (c) 2012, B3log Team
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
package org.b3log.symphony.filter;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.user.GeneralUser;
import org.b3log.latke.user.UserService;
import org.b3log.latke.user.UserServiceFactory;

/**
 * Authentication filter.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 16, 2012
 * @since 0.2.0
 */
public final class AuthFilter implements Filter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    /**
     * User service.
     */
    private UserService userService = UserServiceFactory.getUserService();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /**
     * If the specified request is NOT made by an authenticated user, sends 
     * error 403.
     *
     * @param request the specified request
     * @param response the specified response
     * @param chain filter chain
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        try {
            // TODO: 88250, LoginProcessor.tryLogInWithCookie(httpServletRequest, httpServletResponse);

            final GeneralUser currentUser = userService.getCurrentUser(httpServletRequest);
            if (null == currentUser) {
                LOGGER.warning("The request has been forbidden");
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);

                return;
            }

            chain.doFilter(request, response);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Auth filter failed", e);
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    public void destroy() {
    }
}
