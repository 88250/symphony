package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
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

/**
 * Sitemap query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 24, 2016
 * @since 1.6.0
 */
@Service
public class SitemapQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SitemapQueryService.class.getName());

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
     * Generates domains for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genDomains(final Sitemap sitemap) {
        final List<JSONObject> domains = domainCache.getDomains(Integer.MAX_VALUE);

        for (final JSONObject domain : domains) {
            final String permalink = domain.optString(Latkes.getServePath() + "/" + Domain.DOMAIN_URI);

            final Sitemap.URL url = new Sitemap.URL();
            url.setLoc(permalink);

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
                setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)).
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
