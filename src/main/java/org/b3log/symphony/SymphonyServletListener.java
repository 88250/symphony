/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.b3log.latke.Keys;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.StaticResources;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.event.ArticleNotifier;
import org.b3log.symphony.event.CommentNotifier;
import org.b3log.symphony.event.solo.ArticleSender;
import org.b3log.symphony.event.solo.ArticleUpdater;
import org.b3log.symphony.event.solo.CommentSender;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.util.Skins;
import org.json.JSONObject;

/**
 * B3log Symphony servlet listener.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.8, Mar 7, 2013
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

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Stopwatchs.start("Context Initialized");
        
        // For CloudFoundry 
        // System.out.println(System.getenv("VCAP_SERVICES"));
        // LOGGER.info(System.getenv("VCAP_SERVICES"));

        super.contextInitialized(servletContextEvent);

        Skins.loadSkin();

        // Register event listeners
        final EventManager eventManager = EventManager.getInstance();
        eventManager.registerListener(new ArticleSender());
        eventManager.registerListener(new ArticleUpdater());
        eventManager.registerListener(new CommentSender());
        eventManager.registerListener(new CommentNotifier());
        eventManager.registerListener(new ArticleNotifier());

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.DEBUG, "Stopwatch: {0}{1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
        Stopwatchs.release();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
        final HttpSession session = httpSessionEvent.getSession();

        final Object userObj = session.getAttribute(User.USER);
        if (null != userObj) { // User logout
            final JSONObject user = (JSONObject) userObj;

            try {
                UserMgmtService.getInstance().updateOnlineStatus(user.optString(Keys.OBJECT_ID), false);
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Changes user online from [true] to [false] failed", e);
            }
        }
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();

        if (Requests.searchEngineBotRequest(httpServletRequest)) {
            LOGGER.log(Level.DEBUG, "Request made from a search engine[User-Agent={0}]", httpServletRequest.getHeader("User-Agent"));
            httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, true);
        } else {
            httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, false);
            
            if (StaticResources.isStatic(httpServletRequest)) {
                return;
            }
            
            // Gets the session of this request
            final HttpSession session = httpServletRequest.getSession();
            LOGGER.log(Level.DEBUG, "Gets a session[id={0}, remoteAddr={1}, User-Agent={2}, isNew={3}]",
                       new Object[]{session.getId(), httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"),
                                    session.isNew()});
            // Online visitor count
            OptionQueryService.onlineVisitorCount(httpServletRequest);
        }
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        super.requestDestroyed(servletRequestEvent);
        Stopwatchs.release();
    }
}
