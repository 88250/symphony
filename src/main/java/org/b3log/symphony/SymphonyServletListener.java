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
package org.b3log.symphony;

import eu.bitwalker.useragentutils.BrowserType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.*;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.event.*;
import org.b3log.symphony.event.solo.ArticleSender;
import org.b3log.symphony.event.solo.ArticleUpdater;
import org.b3log.symphony.event.solo.CommentSender;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.InitMgmtService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Crypts;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.Locale;

/**
 * Symphony servlet listener.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @version 3.19.9.33, May 18, 2017
 * @since 0.2.0
 */
public final class SymphonyServletListener extends AbstractServletListener {

    /**
     * Symphony version.
     */
    public static final String VERSION = "2.1.0";
    /**
     * JSONO print indent factor.
     */
    public static final int JSON_PRINT_INDENT_FACTOR = 4;
    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SymphonyServletListener.class);
    /**
     * Bean manager.
     */
    private LatkeBeanManager beanManager;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        Stopwatchs.start("Context Initialized");
        Latkes.setScanPath("org.b3log.symphony");
        super.contextInitialized(servletContextEvent);

        // del this after done TODO: https://github.com/b3log/symphony/issues/98
        final String skinDirName = Symphonys.get("skinDirName");
        Latkes.loadSkin(skinDirName);

        beanManager = Lifecycle.getBeanManager();

        final InitMgmtService initMgmtService = beanManager.getReference(InitMgmtService.class);
        initMgmtService.initSym();

        // Register event listeners
        final EventManager eventManager = beanManager.getReference(EventManager.class);

        eventManager.registerListener(new ArticleSender()); // Not a bean
        eventManager.registerListener(new ArticleUpdater()); // Not a bean
        eventManager.registerListener(new CommentSender()); // Not a bean
        eventManager.registerListener(new org.b3log.symphony.event.other.CommentSender()); // Not a bean

        final ArticleAddNotifier articleAddNotifier = beanManager.getReference(ArticleAddNotifier.class);
        eventManager.registerListener(articleAddNotifier);

        final ArticleUpdateNotifier articleUpdateNotifier = beanManager.getReference(ArticleUpdateNotifier.class);
        eventManager.registerListener(articleUpdateNotifier);

        final ArticleBaiduSender articleBaiduSender = beanManager.getReference(ArticleBaiduSender.class);
        eventManager.registerListener(articleBaiduSender);

        final ArticleQQSender articleQQSender = beanManager.getReference(ArticleQQSender.class);
        eventManager.registerListener(articleQQSender);

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

        JdbcRepository.dispose();

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.DEBUG, "Stopwatch: {0}{1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
        Stopwatchs.release();
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        Symphonys.EXECUTOR_SERVICE.shutdown();

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

            final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);

            try {
                userMgmtService.updateOnlineStatus(user.optString(Keys.OBJECT_ID), "", false);
            } catch (final ServiceException e) {
                LOGGER.log(Level.ERROR, "Changes user online from [true] to [false] failed", e);
            }
        }

        super.sessionDestroyed(httpSessionEvent);
    }

    @Override
    public void requestInitialized(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(Latkes.getLocale());

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequestEvent.getServletRequest();

        httpServletRequest.setAttribute(Keys.TEMAPLTE_DIR_NAME, Symphonys.get("skinDirName"));
        httpServletRequest.setAttribute(Common.IS_MOBILE, false);

        httpServletRequest.setAttribute(UserExt.USER_AVATAR_VIEW_MODE, UserExt.USER_AVATAR_VIEW_MODE_C_ORIGINAL);

        final String userAgentStr = httpServletRequest.getHeader(Common.USER_AGENT);

        final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
        BrowserType browserType = userAgent.getBrowser().getBrowserType();

        if (StringUtils.containsIgnoreCase(userAgentStr, "mobile")
                || StringUtils.containsIgnoreCase(userAgentStr, "MQQBrowser")
                || StringUtils.containsIgnoreCase(userAgentStr, "iphone")
                || StringUtils.containsIgnoreCase(userAgentStr, "MicroMessenger")
                || StringUtils.containsIgnoreCase(userAgentStr, "CFNetwork")) {
            browserType = BrowserType.MOBILE_BROWSER;
        } else if (StringUtils.containsIgnoreCase(userAgentStr, "Iframely")
                || StringUtils.containsIgnoreCase(userAgentStr, "Google")
                || StringUtils.containsIgnoreCase(userAgentStr, "B3log")) {
            browserType = BrowserType.ROBOT;
        } else if (BrowserType.UNKNOWN == browserType) {
            if (!StringUtils.containsIgnoreCase(userAgentStr, "Java")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "MetaURI")
                    && !StringUtils.containsIgnoreCase(userAgentStr, "Feed")) {
                LOGGER.log(Level.WARN, "Unknown client [UA=" + userAgentStr + ", remoteAddr="
                        + Requests.getRemoteAddr(httpServletRequest) + ", URI="
                        + httpServletRequest.getRequestURI() + "]");
            }
        }

        if (BrowserType.ROBOT == browserType) {
            LOGGER.log(Level.DEBUG, "Request made from a search engine[User-Agent={0}]",
                    httpServletRequest.getHeader(Common.USER_AGENT));
            httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, true);

            return;
        }

        httpServletRequest.setAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT, false);

        if (StaticResources.isStatic(httpServletRequest)) {
            return;
        }

        Stopwatchs.start("Request initialized [" + httpServletRequest.getRequestURI() + "]");

        httpServletRequest.setAttribute(Common.IS_MOBILE, BrowserType.MOBILE_BROWSER == browserType);

        // Gets the session of this request
        final HttpSession session = httpServletRequest.getSession();
        LOGGER.log(Level.TRACE, "Gets a session[id={0}, remoteAddr={1}, User-Agent={2}, isNew={3}]",
                new Object[]{session.getId(), httpServletRequest.getRemoteAddr(),
                        httpServletRequest.getHeader(Common.USER_AGENT), session.isNew()});

        resolveSkinDir(httpServletRequest);
    }

    @Override
    public void requestDestroyed(final ServletRequestEvent servletRequestEvent) {
        Locales.setLocale(null);

        try {
            super.requestDestroyed(servletRequestEvent);

            final HttpServletRequest request = (HttpServletRequest) servletRequestEvent.getServletRequest();
            final boolean isStatic = (Boolean) request.getAttribute(Keys.HttpRequest.IS_REQUEST_STATIC_RESOURCE);
            if (!isStatic) {
                Stopwatchs.end();

                final long elapsed = Stopwatchs.getElapsed("Request initialized [" + request.getRequestURI() + "]");
                if (elapsed > Symphonys.getInt("perfromance.threshold")) {
                    LOGGER.log(Level.INFO, "Stopwatch: {0}{1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
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

        request.setAttribute(Keys.TEMAPLTE_DIR_NAME, (Boolean) request.getAttribute(Common.IS_MOBILE)
                ? "mobile" : "classic");

        try {
            final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
            final UserRepository userRepository = beanManager.getReference(UserRepository.class);
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
                final Cookie[] cookies = request.getCookies();
                if (null == cookies || 0 == cookies.length) {
                    return;
                }

                try {
                    for (final Cookie cookie : cookies) {
                        if (!"b3log-latke".equals(cookie.getName())) {
                            continue;
                        }

                        final String value = Crypts.decryptByAES(cookie.getValue(), Symphonys.get("cookie.secret"));
                        if (StringUtils.isBlank(value)) {
                            break;
                        }

                        final JSONObject cookieJSONObject = new JSONObject(value);

                        final String userId = cookieJSONObject.optString(Keys.OBJECT_ID);
                        if (Strings.isEmptyOrNull(userId)) {
                            break;
                        }

                        user = userRepository.get(userId);
                        if (null == user) {
                            return;
                        } else {
                            break;
                        }
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Read cookie failed", e);
                }

                if (null == user) {
                    return;
                }
            }

            request.setAttribute(Keys.TEMAPLTE_DIR_NAME, (Boolean) request.getAttribute(Common.IS_MOBILE)
                    ? user.optString(UserExt.USER_MOBILE_SKIN) : user.optString(UserExt.USER_SKIN));
            request.setAttribute(UserExt.USER_AVATAR_VIEW_MODE, user.optInt(UserExt.USER_AVATAR_VIEW_MODE));

            request.setAttribute(User.USER, user);

            final Locale locale = Locales.getLocale(user.optString(UserExt.USER_LANGUAGE));
            Locales.setLocale(locale);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Resolves skin failed", e);
        } finally {
            Stopwatchs.end();
        }
    }
}
