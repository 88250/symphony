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
package org.b3log.symphony;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.service.StatisticQueryService;
import org.b3log.symphony.util.Skins;

/**
 * B3log Symphony servlet listener.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Oct 6, 2011
 * @since 0.2.0
 */
public final class SymphonyServletListener extends AbstractServletListener {

    /**
     * B3log Symphony version.
     */
    public static final String VERSION = "0.2.0";
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SymphonyServletListener.class.getName());
    /**
     * JSONO print indent factor.
     */
    public static final int JSON_PRINT_INDENT_FACTOR = 4;
    /**
     * B3log Rhythm address.
     */
    public static final String B3LOG_RHYTHM_ADDRESS = "http://rhythm.b3log.org:80";

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Stopwatchs.start("Context Initialized");

        super.contextInitialized(servletContextEvent);

        Latkes.disablePageCache();
        Latkes.disableDataCache();

        Skins.loadSkin();

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.FINE, "Stopwatch: {0}{1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    // Note: This method will never invoked on GAE production environment
    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();
        final String requestURI = httpServletRequest.getRequestURI();
        Stopwatchs.start("Request Initialized[requestURI=" + requestURI + "]");

        if (Requests.searchEngineBotRequest(httpServletRequest)) {
            LOGGER.log(Level.FINER, "Request made from a search engine[User-Agent={0}]", httpServletRequest.getHeader("User-Agent"));
            httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, true);
        } else {
            // Gets the session of this request
            final HttpSession session = httpServletRequest.getSession();
            LOGGER.log(Level.FINE, "Gets a session[id={0}, remoteAddr={1}, User-Agent={2}, isNew={3}]",
                    new Object[]{session.getId(), httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"),
                        session.isNew()});
            // Online visitor count
            StatisticQueryService.onlineVisitorCount(httpServletRequest);
        }
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        Stopwatchs.end();

        LOGGER.log(Level.FINE, "Stopwatch: {0}    {1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
        Stopwatchs.release();

        super.requestDestroyed(servletRequestEvent);
    }
}
