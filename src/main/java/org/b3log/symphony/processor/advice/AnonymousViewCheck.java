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
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.logging.Level;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.advice.BeforeRequestProcessAdvice;
import org.b3log.latke.servlet.advice.RequestProcessAdviceException;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.service.OptionQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Anonymous view check.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.1, Aug 29, 2016
 * @since 1.6.0
 */
@Named
@Singleton
public class AnonymousViewCheck extends BeforeRequestProcessAdvice {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AnonymousViewCheck.class.getName());

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

    @Override
    public void doAdvice(final HTTPRequestContext context, final Map<String, Object> args) throws RequestProcessAdviceException {
        final HttpServletRequest request = context.getRequest();

        if ((Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            return;
        }

        if ((Boolean) request.getAttribute(Common.IS_MOBILE)) { // XXX: allow anonymous view for mobie users
            return;
        }

        if (Strings.contains(request.getRequestURI(), Symphonys.get("anonymousViewSkips").split(","))) {
            return;
        }

        final JSONObject exception404 = new JSONObject();
        exception404.put(Keys.MSG, HttpServletResponse.SC_NOT_FOUND + ", " + request.getRequestURI());
        exception404.put(Keys.STATUS_CODE, HttpServletResponse.SC_NOT_FOUND);

        final JSONObject exception403 = new JSONObject();
        exception403.put(Keys.MSG, HttpServletResponse.SC_FORBIDDEN + ", " + request.getRequestURI());
        exception403.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

        final String requestURI = request.getRequestURI();
        if (requestURI.startsWith(Latkes.getContextPath() + "/article")) {
            final String articleId = StringUtils.substringAfter(requestURI, Latkes.getContextPath() + "/article");

            try {
                final JSONObject article = articleRepository.get(articleId);
                if (null == article) {
                    throw new RequestProcessAdviceException(exception404);
                }

                if (Article.ARTICLE_ANONYMOUS_VIEW_C_NOT_ALLOW == article.optInt(Article.ARTICLE_ANONYMOUS_VIEW)
                        && null == userQueryService.getCurrentUser(request)
                        && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                    throw new RequestProcessAdviceException(exception403);
                }
            } catch (final RepositoryException | ServiceException e) {
                LOGGER.log(Level.ERROR, "Get article [id=" + articleId + "] failed", e);

                throw new RequestProcessAdviceException(exception404);
            }
        }

        try {
            // check if admin allow to anonymous view
            final JSONObject option = optionQueryService.getOption(Option.ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                JSONObject currentUser = userQueryService.getCurrentUser(request);
                if (null == currentUser && !userMgmtService.tryLogInWithCookie(request, context.getResponse())) {
                    throw new RequestProcessAdviceException(exception403);
                }
            }
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Anonymous view check failed");

            throw new RequestProcessAdviceException(exception403);
        }
    }
}
