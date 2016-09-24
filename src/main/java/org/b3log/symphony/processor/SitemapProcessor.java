/*
 * Copyright (c) 2010-2016, b3log.org & hacpai.com
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
package org.b3log.symphony.processor;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.TextXMLRenderer;
import org.b3log.symphony.model.sitemap.Sitemap;
import org.b3log.symphony.service.SitemapQueryService;

/**
 * Sitemap processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 24, 2016
 * @since 1.6.0
 */
@RequestProcessor
public class SitemapProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SitemapProcessor.class.getName());

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
    @RequestProcessing(value = "/sitemap.xml", method = HTTPRequestMethod.GET)
    public void sitemap(final HTTPRequestContext context) {
        final TextXMLRenderer renderer = new TextXMLRenderer();

        context.setRenderer(renderer);

        final Sitemap sitemap = new Sitemap();

        try {
            LOGGER.log(Level.INFO, "Generating sitemap....");

            sitemapQueryService.genDomains(sitemap);
            sitemapQueryService.genArticles(sitemap);
            
            final String content = sitemap.toString();

            LOGGER.log(Level.INFO, "Generated sitemap");

            renderer.setContent(content);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get blog article feed error", e);

            try {
                context.getResponse().sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
