/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.processor;

import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.cache.ArticleCache;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.util.Symphonys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cache processor.
 * <ul>
 * <li>Refreshes cache (/cron/refresh-cache), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Apr 4, 2018
 * @since 2.6.0
 */
@RequestProcessor
public class CacheProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CacheProcessor.class);

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Article cache.
     */
    @Inject
    private ArticleCache articleCache;

    /**
     * Refreshes cache.
     * <ul>
     * <li>Tags</li>
     * <li>Domains</li>
     * </ul>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/refresh-cache", method = HTTPRequestMethod.GET)
    public void refreshCache(final HTTPRequestContext context,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        domainCache.loadDomains();
        articleCache.loadPerfectArticles();
        articleCache.loadSideHotArticles();
        articleCache.loadSideRandomArticles();
        tagCache.loadTags();

        context.renderJSON().renderTrueResult();
    }
}
