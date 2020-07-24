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
package org.b3log.symphony.cache;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.ioc.Singleton;
import org.b3log.latke.repository.*;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Article cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://qiankunpingtai.cn">qiankunpingtai</a>
 * @version 1.3.1.3, May 20, 2019
 * @since 1.4.0
 */
@Singleton
public class ArticleCache {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ArticleCache.class);

    /**
     * Article cache.
     */
    private static final Cache ARTICLE_CACHE = CacheFactory.getCache(Article.ARTICLES);

    /**
     * Article abstract cache.
     */
    private static final Cache ARTICLE_ABSTRACT_CACHE = CacheFactory.getCache(Article.ARTICLES + "_"
            + Article.ARTICLE_T_PREVIEW_CONTENT);

    /**
     * Side hot articles cache.
     */
    private static final List<JSONObject> SIDE_HOT_ARTICLES = new ArrayList<>();

    /**
     * Side random articles cache.
     */
    private static final List<JSONObject> SIDE_RANDOM_ARTICLES = new ArrayList<>();

    /**
     * Perfect articles cache.
     */
    private static final List<JSONObject> PERFECT_ARTICLES = new ArrayList<>();

    /**
     * Gets side hot articles.
     *
     * @return side hot articles
     */
    public List<JSONObject> getSideHotArticles() {
        if (SIDE_HOT_ARTICLES.isEmpty()) {
            return Collections.emptyList();
        }

        return JSONs.clone(SIDE_HOT_ARTICLES);
    }

    /**
     * Loads side hot articles.
     */
    public void loadSideHotArticles() {
        final BeanManager beanManager = BeanManager.getInstance();
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);
        final ArticleQueryService articleQueryService = beanManager.getReference(ArticleQueryService.class);

        Stopwatchs.start("Load side hot articles");
        try {
            final String id = String.valueOf(DateUtils.addDays(new Date(), -7).getTime());
            final Query query = new Query().addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                    addSort(Keys.OBJECT_ID, SortDirection.ASCENDING).
                    setPage(1, Symphonys.SIDE_HOT_ARTICLES_CNT);
            final List<Filter> filters = new ArrayList<>();
            filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
            filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
            filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_EQUAL, Tag.TAG_TITLE_C_SANDBOX));
            filters.add(new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT));
            query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters)).
                    select(Article.ARTICLE_TITLE, Article.ARTICLE_PERMALINK, Article.ARTICLE_AUTHOR_ID, Article.ARTICLE_ANONYMOUS);
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
            articleQueryService.organizeArticles(articles);

            SIDE_HOT_ARTICLES.clear();
            SIDE_HOT_ARTICLES.addAll(articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Loads side hot articles failed", e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets side random articles.
     *
     * @return side random articles
     */
    public List<JSONObject> getSideRandomArticles() {
        int size = Symphonys.SIDE_RANDOM_ARTICLES_CNT;
        if (1 > size) {
            return Collections.emptyList();
        }

        if (SIDE_RANDOM_ARTICLES.isEmpty()) {
            return Collections.emptyList();
        }

        size = size > SIDE_RANDOM_ARTICLES.size() ? SIDE_RANDOM_ARTICLES.size() : size;
        Collections.shuffle(SIDE_RANDOM_ARTICLES);

        return JSONs.clone(SIDE_RANDOM_ARTICLES.subList(0, size));
    }

    /**
     * Loads side random articles.
     */
    public void loadSideRandomArticles() {
        final int size = Symphonys.SIDE_RANDOM_ARTICLES_CNT;
        if (1 > size) {
            return;
        }

        final BeanManager beanManager = BeanManager.getInstance();
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);
        final ArticleQueryService articleQueryService = beanManager.getReference(ArticleQueryService.class);

        Stopwatchs.start("Load side random articles");
        try {
            final List<JSONObject> articles = articleRepository.getRandomly(size * 5);
            articleQueryService.organizeArticles(articles);

            SIDE_RANDOM_ARTICLES.clear();
            SIDE_RANDOM_ARTICLES.addAll(articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Loads side random articles failed", e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets an article abstract by the specified article id.
     *
     * @param articleId the specified article id
     * @return article abstract, return {@code null} if not found
     */
    public String getArticleAbstract(final String articleId) {
        final JSONObject value = ARTICLE_ABSTRACT_CACHE.get(articleId);
        if (null == value) {
            return null;
        }

        return value.optString(Common.DATA);
    }

    /**
     * Gets perfect articles.
     *
     * @return side random articles
     */
    public List<JSONObject> getPerfectArticles() {
        if (PERFECT_ARTICLES.isEmpty()) {
            return Collections.emptyList();
        }

        return JSONs.clone(PERFECT_ARTICLES);
    }

    /**
     * Loads perfect articles.
     */
    public void loadPerfectArticles() {
        final BeanManager beanManager = BeanManager.getInstance();
        final ArticleRepository articleRepository = beanManager.getReference(ArticleRepository.class);
        final ArticleQueryService articleQueryService = beanManager.getReference(ArticleQueryService.class);

        Stopwatchs.start("Query perfect articles");
        try {
            final Query query = new Query().
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setPageCount(1).setPage(1, 36);
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Article.ARTICLE_PERFECT, FilterOperator.EQUAL, Article.ARTICLE_PERFECT_C_PERFECT),
                    new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT)));
            query.select(Keys.OBJECT_ID,
                    Article.ARTICLE_STICK,
                    Article.ARTICLE_CREATE_TIME,
                    Article.ARTICLE_UPDATE_TIME,
                    Article.ARTICLE_LATEST_CMT_TIME,
                    Article.ARTICLE_AUTHOR_ID,
                    Article.ARTICLE_TITLE,
                    Article.ARTICLE_STATUS,
                    Article.ARTICLE_VIEW_CNT,
                    Article.ARTICLE_TYPE,
                    Article.ARTICLE_PERMALINK,
                    Article.ARTICLE_TAGS,
                    Article.ARTICLE_LATEST_CMTER_NAME,
                    Article.ARTICLE_COMMENT_CNT,
                    Article.ARTICLE_ANONYMOUS,
                    Article.ARTICLE_PERFECT,
                    Article.ARTICLE_QNA_OFFER_POINT,
                    Article.ARTICLE_SHOW_IN_LIST);
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
            articleQueryService.organizeArticles(articles);
            PERFECT_ARTICLES.clear();
            PERFECT_ARTICLES.addAll(articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Loads perfect articles failed", e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Puts an article abstract by the specified article id and article abstract.
     *
     * @param articleId       the specified article id
     * @param articleAbstract the specified article abstract
     */
    public void putArticleAbstract(final String articleId, final String articleAbstract) {
        final JSONObject value = new JSONObject();
        value.put(Common.DATA, articleAbstract);
        ARTICLE_ABSTRACT_CACHE.put(articleId, value);
    }

    /**
     * Gets an article by the specified article id.
     *
     * @param id the specified article id
     * @return article, returns {@code null} if not found
     */
    public JSONObject getArticle(final String id) {
        final JSONObject article = ARTICLE_CACHE.get(id);
        if (null == article) {
            return null;
        }

        return JSONs.clone(article);
    }

    /**
     * Adds or updates the specified article.
     *
     * @param article the specified article
     */
    public void putArticle(final JSONObject article) {
        final String articleId = article.optString(Keys.OBJECT_ID);

        ARTICLE_CACHE.put(articleId, JSONs.clone(article));
        ARTICLE_ABSTRACT_CACHE.remove(articleId);
    }

    /**
     * Removes an article by the specified article id.
     *
     * @param id the specified article id
     */
    public void removeArticle(final String id) {
        ARTICLE_CACHE.remove(id);
        ARTICLE_ABSTRACT_CACHE.remove(id);
    }
}
