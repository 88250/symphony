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
package org.b3log.symphony.cache;

import javax.inject.Named;
import javax.inject.Singleton;
import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Article cache.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.2, Nov 10, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class ArticleCache {

    /**
     * Article cache.
     */
    private static final Cache ARTICLE_CACHE = CacheFactory.getCache(Article.ARTICLES);

    /**
     * Article abstract cache.
     */
    private static final Cache ARTICLE_ABSTRACT_CACHE = CacheFactory.getCache(Article.ARTICLES + "_"
            + Article.ARTICLE_T_PREVIEW_CONTENT);

    static {
        ARTICLE_CACHE.setMaxCount(Symphonys.getInt("cache.articleCnt"));
        ARTICLE_ABSTRACT_CACHE.setMaxCount(Symphonys.getInt("cache.articleCnt"));
    }

    /**
     * Gets an article abstract by the specified article id.
     *
     * @param articleId the specified article id
     * @return article abstract, return {@code null} if not found
     */
    public String getArticleAbstract(final String articleId) {
        return (String) ARTICLE_ABSTRACT_CACHE.get(articleId);
    }

    /**
     * Puts an article abstract by the specified article id and article abstract.
     *
     * @param articleId the specified article id
     * @param articleAbstract the specified article abstract
     */
    public void putArticleAbstract(final String articleId, final String articleAbstract) {
        ARTICLE_ABSTRACT_CACHE.put(articleId, articleAbstract);
    }

    /**
     * Gets an article by the specified article id.
     *
     * @param id the specified article id
     * @return article, returns {@code null} if not found
     */
    public JSONObject getArticle(final String id) {
        final JSONObject article = (JSONObject) ARTICLE_CACHE.get(id);
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
