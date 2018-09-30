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
package org.b3log.symphony.service;

import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.model.sitemap.Sitemap;
import org.b3log.symphony.repository.ArticleRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Sitemap query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Mar 31, 2018
 * @since 1.6.0
 */
@Service
public class SitemapQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SitemapQueryService.class);

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Generates index for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genIndex(final Sitemap sitemap) {
        final Sitemap.URL url = new Sitemap.URL();
        url.setLoc(Latkes.getServePath());
        url.setChangeFreq("always");
        url.setPriority("1.0");

        sitemap.addURL(url);
    }

    /**
     * Generates domains for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genDomains(final Sitemap sitemap) {
        final List<JSONObject> domains = domainCache.getDomains(Integer.MAX_VALUE);

        for (final JSONObject domain : domains) {
            final String permalink = Latkes.getServePath() + "/domain/" + domain.optString(Domain.DOMAIN_URI);

            final Sitemap.URL url = new Sitemap.URL();
            url.setLoc(permalink);
            url.setChangeFreq("always");
            url.setPriority("0.9");

            sitemap.addURL(url);
        }
    }

    /**
     * Generates articles for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genArticles(final Sitemap sitemap) {
        final Query query = new Query().setCurrentPageNum(1).setPageCount(Integer.MAX_VALUE).
                addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONArray articles = articleRepository.get(query).getJSONArray(Keys.RESULTS);

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.getJSONObject(i);
                final long id = article.getLong(Keys.OBJECT_ID);
                final String permalink = Latkes.getServePath() + "/article/" + id;

                final Sitemap.URL url = new Sitemap.URL();
                url.setLoc(permalink);
                final Date updateDate = new Date(id);
                final String lastMod = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(updateDate);
                url.setLastMod(lastMod);

                sitemap.addURL(url);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets sitemap articles failed", e);
        }
    }
}
