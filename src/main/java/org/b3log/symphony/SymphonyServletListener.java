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
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.AbstractServletListener;
import org.b3log.latke.util.*;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.event.*;
import org.b3log.symphony.event.solo.ArticleSender;
import org.b3log.symphony.event.solo.ArticleUpdater;
import org.b3log.symphony.event.solo.CommentSender;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.TagMgmtService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Crypts;
import org.b3log.symphony.util.Languages;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Symphony servlet listener.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @version 3.17.8.21, Dec 10, 2016
 * @since 0.2.0
 */
public final class SymphonyServletListener extends AbstractServletListener {

    /**
     * Symphony version.
     */
    public static final String VERSION = "1.7.0";
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

        // Init database if need
        initDB();

        // Register event listeners
        final EventManager eventManager = beanManager.getReference(EventManager.class);

        eventManager.registerListener(new ArticleSender()); // Not a bean
        eventManager.registerListener(new ArticleUpdater()); // Not a bean
        eventManager.registerListener(new CommentSender()); // Not a bean
        eventManager.registerListener(new org.b3log.symphony.event.other.CommentSender()); // Not a bean

        final ArticleNotifier articleNotifier = beanManager.getReference(ArticleNotifier.class);
        eventManager.registerListener(articleNotifier);

        final ArticleBaiduSender articleBaiduSender = beanManager.getReference(ArticleBaiduSender.class);
        eventManager.registerListener(articleBaiduSender);

        final ArticleQQSender articleQQSender = beanManager.getReference(ArticleQQSender.class);
        eventManager.registerListener(articleQQSender);

        final CommentNotifier commentNotifier = beanManager.getReference(CommentNotifier.class);
        eventManager.registerListener(commentNotifier);

        final ArticleSearchAdder articleSearchAdder = beanManager.getReference(ArticleSearchAdder.class);
        eventManager.registerListener(articleSearchAdder);

        final ArticleSearchUpdater articleSearchUpdater = beanManager.getReference(ArticleSearchUpdater.class);
        eventManager.registerListener(articleSearchUpdater);

        final TagCache tagCache = beanManager.getReference(TagCache.class);
        tagCache.loadTags();

        final DomainCache domainCache = beanManager.getReference(DomainCache.class);
        domainCache.loadDomains();

        JdbcRepository.dispose();

        LOGGER.info("Initialized the context");

        Stopwatchs.end();
        LOGGER.log(Level.DEBUG, "Stopwatch: {0}{1}", new Object[]{Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat()});
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

        final String userAgentStr = httpServletRequest.getHeader("User-Agent");

        final UserAgent userAgent = UserAgent.parseUserAgentString(userAgentStr);
        BrowserType browserType = userAgent.getBrowser().getBrowserType();

        if (StringUtils.containsIgnoreCase(userAgentStr, "mobile")
                || StringUtils.containsIgnoreCase(userAgentStr, "MQQBrowser")
                || StringUtils.containsIgnoreCase(userAgentStr, "iphone")) {
            browserType = BrowserType.MOBILE_BROWSER;
        } else if (StringUtils.containsIgnoreCase(userAgentStr, "Iframely")
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
            LOGGER.log(Level.DEBUG, "Request made from a search engine[User-Agent={0}]", httpServletRequest.getHeader("User-Agent"));
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
                new Object[]{session.getId(), httpServletRequest.getRemoteAddr(), httpServletRequest.getHeader("User-Agent"),
                        session.isNew()});

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
     * Initializes database if need.
     */
    private void initDB() {
        final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);

        try {
            final List<JSONObject> admins = userQueryService.getAdmins();
            JdbcRepository.dispose();

            if (null != admins && !admins.isEmpty()) { // Initialized already
                return;
            }
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Check init error", e);

            System.exit(0);
        }

        LOGGER.info("It's your first time setup Sym, initializes DB....");

        final PermissionRepository permissionRepository = beanManager.getReference(PermissionRepository.class);
        final RoleRepository roleRepository = beanManager.getReference(RoleRepository.class);
        final RolePermissionRepository rolePermissionRepository = beanManager.getReference(RolePermissionRepository.class);
        final OptionRepository optionRepository = beanManager.getReference(OptionRepository.class);
        final TagRepository tagRepository = beanManager.getReference(TagRepository.class);
        final TagMgmtService tagMgmtService = beanManager.getReference(TagMgmtService.class);
        final ArticleMgmtService articleMgmtService = beanManager.getReference(ArticleMgmtService.class);
        final UserMgmtService userMgmtService = beanManager.getReference(UserMgmtService.class);

        try {
            LOGGER.log(Level.INFO, "Database [{0}], creates all tables", Latkes.getRuntimeDatabase());

            final List<JdbcRepositories.CreateTableResult> createTableResults = JdbcRepositories.initAllTables();
            for (final JdbcRepositories.CreateTableResult createTableResult : createTableResults) {
                LOGGER.log(Level.INFO, "Creates table result[tableName={0}, isSuccess={1}]",
                        new Object[]{createTableResult.getName(), createTableResult.isSuccess()});
            }

            Transaction transaction = optionRepository.beginTransaction();

            // Init statistic
            JSONObject option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_MEMBER_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_CMT_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_ARTICLE_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_DOMAIN_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_TAG_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_LINK_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_STATISTIC);
            optionRepository.add(option);

            // Init misc
            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_MISC_ALLOW_REGISTER);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_MISC_ALLOW_ADD_ARTICLE);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_MISC_ALLOW_ADD_COMMENT);
            option.put(Option.OPTION_VALUE, "0");
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);
            optionRepository.add(option);

            option = new JSONObject();
            option.put(Keys.OBJECT_ID, Option.ID_C_MISC_LANGUAGE);
            option.put(Option.OPTION_VALUE, "0"); // user browser
            option.put(Option.OPTION_CATEGORY, Option.CATEGORY_C_MISC);
            optionRepository.add(option);

            transaction.commit();

            transaction = permissionRepository.beginTransaction();

            // Init permissions
            final JSONObject permission = new JSONObject();

            // ad management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_AD);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_AD_UPDATE_SIDE);
            permissionRepository.add(permission);

            // article management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_ARTICLE);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_ARTICLE_CANCEL_STICK_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLE_INDEX);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_ARTICLE_REMOVE_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_ARTICLE_STICK_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_ARTICLE_UPDATE_ARTICLE_BASIC);
            permissionRepository.add(permission);

            // comment management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_COMMENT);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMENT_REMOVE_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMENT_UPDATE_COMMENT_BASIC);
            permissionRepository.add(permission);

            // common permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_COMMON);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_ADD_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_UPDATE_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_ADD_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_AT_USER);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_BAD_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_BAD_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_EXCHANGE_INVITATION_CODE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_FOLLOW_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_STICK_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_THANK_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_THANK_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_COMMON_VIEW_ARTICLE_HISTORY);
            permissionRepository.add(permission);

            // domain management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_DOMAIN);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN_TAG);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN_TAG);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_DOMAIN_UPDATE_DOMAIN_BASIC);
            permissionRepository.add(permission);

            // invitecode management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_IC);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_IC_GEN_IC);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_IC_UPDATE_IC_BASIC);
            permissionRepository.add(permission);

            // misc management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_MISC);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ADD_ARTICLE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ADD_COMMENT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_MISC_LANGUAGE);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_MISC_REGISTER_METHOD);
            permissionRepository.add(permission);

            // reserved word management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_RESERVED_WORD);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_RW_ADD_RW);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_RW_REMOVE_RW);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_RW_UPDATE_RW_BASIC);
            permissionRepository.add(permission);

            // tag management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_TAG);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_TAG_UPDATE_TAG_BASIC);
            permissionRepository.add(permission);

            // user management permissions
            permission.put(Permission.PERMISSION_CATEGORY, Permission.PERMISSION_CATEGORY_C_USER);

            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_ADD_POINT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_ADD_USER);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_DEDUCT_POINT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_EXCHANGE_POINT);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_UPDATE_USER_ADVANCED);
            permissionRepository.add(permission);
            permission.put(Keys.OBJECT_ID, Permission.PERMISSION_ID_C_USER_UPDATE_USER_BASIC);
            permissionRepository.add(permission);

            transaction.commit();

            transaction = roleRepository.beginTransaction();

            // Init roles
            final JSONObject role = new JSONObject();

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_ADMIN);
            role.put(Role.ROLE_NAME, "Admin");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_DEFAULT);
            role.put(Role.ROLE_NAME, "Default");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_LEADER);
            role.put(Role.ROLE_NAME, "Leader");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_MEMBER);
            role.put(Role.ROLE_NAME, "Member");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_REGULAR);
            role.put(Role.ROLE_NAME, "Regular");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            role.put(Keys.OBJECT_ID, Role.ROLE_ID_C_VISITOR);
            role.put(Role.ROLE_NAME, "Visitor");
            role.put(Role.ROLE_DESCRIPTION, "");
            roleRepository.add(role);

            transaction.commit();

            transaction = rolePermissionRepository.beginTransaction();

            // Init Role-Permission
            final JSONObject rolePermission = new JSONObject();

            // default role's permissions
            rolePermission.put(Role.ROLE_ID, Role.ROLE_ID_C_DEFAULT);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_ADD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_UPDATE_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_ADD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_AT_USER);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_BAD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_BAD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_EXCHANGE_INVITATION_CODE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_FOLLOW_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_STICK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_THANK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_THANK_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_VIEW_ARTICLE_HISTORY);
            rolePermissionRepository.add(rolePermission);

            // admin role's permissions
            rolePermission.put(Role.ROLE_ID, Role.ROLE_ID_C_ADMIN);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_AD_UPDATE_SIDE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_ARTICLE_CANCEL_STICK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLE_INDEX);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_ARTICLE_REMOVE_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_ARTICLE_STICK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_ARTICLE_UPDATE_ARTICLE_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMENT_REMOVE_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMENT_UPDATE_COMMENT_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_ADD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_UPDATE_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_ADD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_AT_USER);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_BAD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_BAD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_EXCHANGE_INVITATION_CODE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_FOLLOW_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_GOOD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_STICK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_THANK_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_THANK_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_COMMON_VIEW_ARTICLE_HISTORY);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN_TAG);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN_TAG);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_DOMAIN_UPDATE_DOMAIN_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_IC_GEN_IC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_IC_UPDATE_IC_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ADD_ARTICLE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ADD_COMMENT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_MISC_LANGUAGE);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_MISC_REGISTER_METHOD);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_RW_ADD_RW);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_RW_REMOVE_RW);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_RW_UPDATE_RW_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_TAG_UPDATE_TAG_BASIC);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_ADD_POINT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_ADD_USER);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_DEDUCT_POINT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_EXCHANGE_POINT);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_UPDATE_USER_ADVANCED);
            rolePermissionRepository.add(rolePermission);

            rolePermission.put(Keys.OBJECT_ID, Ids.genTimeMillisId());
            rolePermission.put(Permission.PERMISSION_ID, Permission.PERMISSION_ID_C_USER_UPDATE_USER_BASIC);
            rolePermissionRepository.add(rolePermission);

            transaction.commit();

            // Init admin
            final ResourceBundle init = ResourceBundle.getBundle("init");
            final JSONObject admin = new JSONObject();
            admin.put(User.USER_EMAIL, init.getString("admin.email"));
            admin.put(User.USER_NAME, init.getString("admin.name"));
            admin.put(User.USER_PASSWORD, MD5.hash(init.getString("admin.password")));

            final Locale defaultLocale = Locale.getDefault();
            final String lang = Locales.getLanguage(defaultLocale.toString());
            final String country = Locales.getCountry(defaultLocale.toString());
            String language = lang + "_" + country;
            if (!Languages.getAvailableLanguages().contains(language)) {
                language = "en_US";
            }
            admin.put(UserExt.USER_LANGUAGE, language);
            admin.put(User.USER_ROLE, Role.ROLE_ID_C_ADMIN);
            admin.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);
            final String adminId = userMgmtService.addUser(admin);
            admin.put(Keys.OBJECT_ID, adminId);

            // Init default commenter (for sync comment from client)
            final JSONObject defaultCommenter = new JSONObject();
            defaultCommenter.put(User.USER_EMAIL, UserExt.DEFAULT_CMTER_EMAIL);
            defaultCommenter.put(User.USER_NAME, UserExt.DEFAULT_CMTER_NAME);
            defaultCommenter.put(User.USER_PASSWORD, MD5.hash(String.valueOf(new Random().nextInt())));
            defaultCommenter.put(UserExt.USER_LANGUAGE, "en_US");
            defaultCommenter.put(User.USER_ROLE, UserExt.DEFAULT_CMTER_ROLE);
            defaultCommenter.put(UserExt.USER_STATUS, UserExt.USER_STATUS_C_VALID);
            userMgmtService.addUser(defaultCommenter);

            // Add the first tag
            final String tagTitle = Symphonys.get("systemAnnounce");
            final String tagId = tagMgmtService.addTag(adminId, tagTitle);
            final JSONObject tag = tagRepository.get(tagId);
            tag.put(Tag.TAG_URI, "announcement");
            tagMgmtService.updateTag(tagId, tag);

            // Hello World!
            final JSONObject article = new JSONObject();
            article.put(Article.ARTICLE_TITLE, init.getString("helloWorld.title"));
            article.put(Article.ARTICLE_TAGS, init.getString("helloWorld.tags"));
            article.put(Article.ARTICLE_CONTENT, init.getString("helloWorld.content"));
            article.put(Article.ARTICLE_EDITOR_TYPE, 0);
            article.put(Article.ARTICLE_AUTHOR_EMAIL, admin.optString(User.USER_EMAIL));
            article.put(Article.ARTICLE_AUTHOR_ID, admin.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);
            articleMgmtService.addArticle(article);

            LOGGER.info("Initialized DB");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Creates database tables failed", e);

            System.exit(0);
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
