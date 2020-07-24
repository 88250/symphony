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
package org.b3log.symphony.processor;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.http.renderer.TextXmlRenderer;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.ioc.Singleton;
import org.b3log.symphony.model.sitemap.Sitemap;
import org.b3log.symphony.service.SitemapQueryService;

/**
 * Sitemap processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.1, Apr 8, 2020
 * @since 1.6.0
 */
@Singleton
public class SitemapProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(SitemapProcessor.class);

    /**
     * Sitemap query service.
     */
    @Inject
    private SitemapQueryService sitemapQueryService;

    /**
     * Returns the sitemap.
     *
     * @param context the specified context
     */
    public void sitemap(final RequestContext context) {
        final TextXmlRenderer renderer = new TextXmlRenderer();
        context.setRenderer(renderer);
        final Sitemap sitemap = new Sitemap();
        LOGGER.log(Level.DEBUG, "Generating sitemap....");
        sitemapQueryService.genIndex(sitemap);
        sitemapQueryService.genDomains(sitemap);
        // sitemapQueryService.genArticles(sitemap);
        final String content = sitemap.toString();
        LOGGER.log(Level.DEBUG, "Generated sitemap");
        renderer.setContent(content);
    }
}
