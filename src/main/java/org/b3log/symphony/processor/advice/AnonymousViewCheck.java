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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.ioc.inject.Named;
import org.b3log.latke.ioc.inject.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.AntPathMatcher;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Anonymous view check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.1.3, May 9, 2017
 * @since 1.6.0
 */
@Named
@Singleton
public class AnonymousViewCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AnonymousViewCheck.class);

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

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

    /**
     * Option query service.
     */
    @Inject
    private OptionQueryService optionQueryService;

    private static Cookie getCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();
        if (null == cookies || 0 == cookies.length) {
            return null;
        }

        for (int i = 0; i < cookies.length; i++) {
            final Cookie cookie = cookies[i];
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }

        return null;
    }

    private static void addCookie(final HttpServletResponse response, final String name, final String value) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 24 hours
        cookie.setHttpOnly(true); // HTTP Only
        cookie.setSecure(StringUtils.equalsIgnoreCase(Latkes.getServerScheme(), "https"));

        response.addCookie(cookie);
    }

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        if ((Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            return;
        }

        if ((Boolean) request.getAttribute(Common.IS_MOBILE)) { // Allow anonymous view for mobile users
            return;
        }

        final String requestURI = request.getRequestURI();

        final String[] skips = Symphonys.get("anonymousViewSkips").split(",");
        for (final String skip : skips) {
            if (AntPathMatcher.match(Latkes.getContextPath() + skip, requestURI)) {
                return;
            }
        }

        final JSONObject exception404 = new JSONObject();
        exception404.put(Keys.MSG, HttpServletResponse.SC_NOT_FOUND + ", " + request.getRequestURI());
        exception404.put(Keys.STATUS_CODE, HttpServletResponse.SC_NOT_FOUND);

        final JSONObject exception403 = new JSONObject();
        exception403.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception403.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        if (requestURI.startsWith(Latkes.getContextPath() + "/article/")) {
            final String articleId = StringUtils.substringAfter(requestURI, Latkes.getContextPath() + "/article/");

            try {
                final JSONObject article = articleRepository.get(articleId);
                if (null == article) {
                    throw new RequestProcessAdviceException(exception404);
                }

                if (Article.ARTICLE_ANONYMOUS_VIEW_C_NOT_ALLOW == article.optInt(Article.ARTICLE_ANONYMOUS_VIEW)
                        && null == userQueryService.getCurrentUser(request)
                        && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                    throw new RequestProcessAdviceException(exception403);
                } else if (Article.ARTICLE_ANONYMOUS_VIEW_C_ALLOW == article.optInt(Article.ARTICLE_ANONYMOUS_VIEW)) {
                    return;
                }
            } catch (final RepositoryException | ServiceException e) {
                LOGGER.log(Level.ERROR, "Get article [id=" + articleId + "] failed", e);

                throw new RequestProcessAdviceException(exception404);
            }
        }

        try {
            // Check if admin allow to anonymous view
            final JSONObject option = optionQueryService.getOption(Option.ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                final JSONObject currentUser = userQueryService.getCurrentUser(request);

                // https://github.com/b3log/symphony/issues/373
                final String cookieNameVisits = "anonymous-visits";
                final Cookie visitsCookie = getCookie(request, cookieNameVisits);

                if (null == currentUser && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                    if (null != visitsCookie) {
                        final JSONArray uris = new JSONArray(visitsCookie.getValue());
                        for (int i = 0; i < uris.length(); i++) {
                            final String uri = uris.getString(i);
                            if (uri.equals(requestURI)) {
                                return;
                            }
                        }

                        uris.put(requestURI);
                        if (uris.length() > 7) {
                            throw new RequestProcessAdviceException(exception403);
                        }

                        addCookie(context.getResponse(), cookieNameVisits, uris.toString());

                        return;
                    } else {
                        final JSONArray uris = new JSONArray();
                        uris.put(requestURI);

                        addCookie(context.getResponse(), cookieNameVisits, uris.toString());

                        return;
                    }
                } else { // logged in
                    if (null != visitsCookie) {
                        final Cookie cookie = new Cookie(cookieNameVisits, null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");

                        context.getResponse().addCookie(cookie);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Anonymous view check failed");

            throw new RequestProcessAdviceException(exception403);
        }
    }
}
