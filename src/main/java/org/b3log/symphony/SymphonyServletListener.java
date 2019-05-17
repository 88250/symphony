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
package org.b3log.symphony;

import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.*;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.event.*;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.service.CronMgmtService;
import org.b3log.symphony.service.InitMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.Locale;

/**
 * Symphony servlet listener.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @version 3.19.10.34, May 17, 2019
 * @since 0.2.0
 */
public final class SymphonyServletListener extends AbstractServletListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SymphonyServletListener.class);

    /**
     * Symphony version.
     */
    public static final String VERSION = "3.5.0";

    /**
     * Bean manager.
     */
    private BeanManager beanManager;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        System.setProperty("java.awt.headless", "true");

        Stopwatchs.start("Context Initialized");
        Latkes.setScanPath("org.b3log.symphony");
        super.contextInitialized(servletContextEvent);

        final Latkes.RuntimeDatabase runtimeDatabase = Latkes.getRuntimeDatabase();
        final Latkes.RuntimeMode runtimeMode = Latkes.getRuntimeMode();
        final String jdbcUsername = Latkes.getLocalProperty("jdbc.username");
        final String jdbcURL = Latkes.getLocalProperty("jdbc.URL");
        final boolean markdownHttpAvailable = Markdowns.MARKDOWN_HTTP_AVAILABLE;

        LOGGER.log(Level.INFO, "Sym is booting [ver=" + VERSION + ", servletContainer=" + Latkes.getServletInfo(servletContextEvent.getServletContext())
                + ", os=" + Latkes.getOperatingSystemName() + ", isDocker=" + Latkes.isDocker() + ", markdownHttpAvailable=" + markdownHttpAvailable + ", pid=" + Latkes.currentPID()
                + ", runtimeDatabase=" + runtimeDatabase + ", runtimeMode=" + runtimeMode + ", jdbc.username=" + jdbcUsername + ", jdbc.URL=" + jdbcURL + "]");

        beanManager = BeanManager.getInstance();

        final InitMgmtService initMgmtService = beanManager.getReference(InitMgmtService.class);
        initMgmtService.initSym();

        // Register event listeners
        final EventManager eventManager = beanManager.getReference(EventManager.class);

        final ArticleAddNotifier articleAddNotifier = beanManager.getReference(ArticleAddNotifier.class);
        eventManager.registerListener(articleAddNotifier);

        final ArticleUpdateNotifier articleUpdateNotifier = beanManager.getReference(ArticleUpdateNotifier.class);
        eventManager.registerListener(articleUpdateNotifier);

        final ArticleBaiduSender articleBaiduSender = beanManager.getReference(ArticleBaiduSender.class);
        eventManager.registerListener(articleBaiduSender);

        final CommentNotifier commentNotifier = beanManager.getReference(CommentNotifier.class);
        eventManager.registerListener(commentNotifier);

        final CommentUpdateNotifier commentUpdateNotifier = beanManager.getReference(CommentUpdateNotifier.class);
        eventManager.registerListener(commentUpdateNotifier);

        final ArticleSearchAdder articleSearchAdder = beanManager.getReference(ArticleSearchAdder.class);
        eventManager.registerListener(articleSearchAdder);

        final ArticleSearchUpdater articleSearchUpdater = beanManager.getReference(ArticleSearchUpdater.class);
        eventManager.registerListener(articleSearchUpdater);

        final ArticleAddAudioHandler articleAddAudioHandler = beanManager.getReference(ArticleAddAudioHandler.class);
        eventManager.registerListener(articleAddAudioHandler);

        final ArticleUpdateAudioHandler articleUpdateAudioHandler = beanManager.getReference(ArticleUpdateAudioHandler.class);
        eventManager.registerListener(articleUpdateAudioHandler);

        final TagCache tagCache = beanManager.getReference(TagCache.class);
        tagCache.loadTags();

        final DomainCache domainCache = beanManager.getReference(DomainCache.class);
        domainCache.loadDomains();

        final CronMgmtService cronMgmtService = beanManager.getReference(CronMgmtService.class);
        cronMgmtService.start();

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.DEBUG, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
        Stopwatchs.release();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        Symphonys.SCHEDULED_EXECUTOR_SERVICE.shutdown();
        Symphonys.EXECUTOR_SERVICE.shutdown();

        LOGGER.info("Destroyed the context");
    }

    @Override
    public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
    }

    @Override
    public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
        super.sessionDestroyed(httpSessionEvent);
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(Latkes.getLocale());

        Sessions.setTemplateDir(Symphonys.SKIN_DIR_NAME);
        Sessions.setMobile(false);
        Sessions.setAvatarViewMode(UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL);

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();
        final String userAgentStr = httpServletRequest.getHeader(Common.USER_AGENT);

        final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
        BrowserType browserType = userAgent.getBrowser().getBrowserType();

        if (StringUtils.containsIgnoreCase(userAgentStr, "mobile")
                || StringUtils.containsIgnoreCase(userAgentStr, "MQQBrowser")
                || StringUtils.containsIgnoreCase(userAgentStr, "iphone")
                || StringUtils.containsIgnoreCase(userAgentStr, "MicroMessenger")
                || StringUtils.containsIgnoreCase(userAgentStr, "CFNetwork")
                || StringUtils.containsIgnoreCase(userAgentStr, "Android")) {
            browserType = BrowserType.MOBILE_BROWSER;
        } else if (StringUtils.containsIgnoreCase(userAgentStr, "Iframely")
                || StringUtils.containsIgnoreCase(userAgentStr, "Google")
                || StringUtils.containsIgnoreCase(userAgentStr, "BUbiNG")
                || StringUtils.containsIgnoreCase(userAgentStr, "ltx71")) {
            browserType = BrowserType.ROBOT;
        } else if (BrowserType.UNKNOWN == browserType) {
            if (!StringUtils.containsIgnoreCase(userAgentStr, "Java")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "MetaURI")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "Feed")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "okhttp")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "Sym")) {
                LOGGER.log(Level.WARN, "Unknown client [UA=" + userAgentStr + ", remoteAddr="
                        + Requests.getRemoteAddr(httpServletRequest) + ", URI=" + httpServletRequest.getRequestURI() + "]");
            }
        }

        if (BrowserType.ROBOT == browserType) {
            LOGGER.log(Level.DEBUG, "Request made from a search engine [User-Agent={0}]",
                    httpServletRequest.getHeader(Common.USER_AGENT));
            Sessions.setBot(true);

            return;
        }

        Sessions.setBot(false);

        if (StaticResources.isStatic(httpServletRequest)) {
            return;
        }

        Stopwatchs.start("Request initialized [" + httpServletRequest.getRequestURI() + "]");

        Sessions.setMobile(BrowserType.MOBILE_BROWSER == browserType);

        resolveSkinDir(httpServletRequest);
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(null);
        Sessions.clearThreadLocalData();

        try {
            super.requestDestroyed(servletRequestEvent);

            final HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
            final Object isStaticObj = request.getAttribute(Keys.HttpRequest.IS_REQUEST_STATIC_RESOURCE);
            if (null != isStaticObj && !(Boolean) isStaticObj) {
                Stopwatchs.end();

                final int threshold = Symphonys.PERFORMANCE_THRESHOLD;
                if (0 < threshold) {
                    final long elapsed = Stopwatchs.getElapsed("Request initialized [" + request.getRequestURI() + "]");
                    if (elapsed >= threshold) {
                        LOGGER.log(Level.INFO, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
                    }
                }
            }
        } finally {
            Stopwatchs.release();
        }
    }


    /**
     * Resolve skin (template) for the specified HTTP servlet request.
     *
     * @param request the specified HTTP servlet request
     */
    private void resolveSkinDir(final HttpServletRequest request) {
        Stopwatchs.start("Resolve skin");

        final String templateDirName = Sessions.isMobile() ? "mobile" : "classic";
        Sessions.setTemplateDir(templateDirName);

        final HttpSession httpSession = request.getSession();
        httpSession.setAttribute(Keys.TEMAPLTE_DIR_NAME, templateDirName);

        try {
            final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
            final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);

            final JSONObject optionLang = optionRepository.get(Option.ID_C_MISC_LANGUAGE);
            final String optionLangValue = optionLang.optString(Option.OPTION_VALUE);
            if ("0".equals(optionLangValue)) {
                Locales.setLocale(request.getLocale());
            } else {
                Locales.setLocale(Locales.getLocale(optionLangValue));
            }

            JSONObject user = userQueryService.getCurrentUser(request);
            if (null == user) {
                return;
            }

            final String skin = Sessions.isMobile() ? user.optString(UserExt.USER_MOBILE_SKIN) : user.optString(UserExt.USER_SKIN);
            httpSession.setAttribute(Keys.TEMAPLTE_DIR_NAME, skin);
            Sessions.setTemplateDir(skin);
            Sessions.setAvatarViewMode(user.optInt(UserExt.USER_AVATAR_VIEW_MODE));
            Sessions.setUser(user);
            Sessions.setLoggedIn(true);

            final Locale locale = Locales.getLocale(user.optString(UserExt.USER_LANGUAGE));
            Locales.setLocale(locale);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Resolves skin failed", e);
        } finally {
            Stopwatchs.end();
        }
    }
}
