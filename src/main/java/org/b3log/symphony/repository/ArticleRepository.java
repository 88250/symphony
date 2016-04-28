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
package org.b3log.symphony.repository;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.cache.ArticleCache;
import org.b3log.symphony.model.Article;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Article repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.1, Apr 20, 2016
 * @since 0.2.0
 */
@Repository
public class ArticleRepository extends AbstractRepository {

    /**
     * Article cache.
     */
    @Inject
    private ArticleCache articleCache;

    /**
     * Public constructor.
     */
    public ArticleRepository() {
        super(Article.ARTICLE);
    }

    @Override
    public void remove(final String id) throws RepositoryException {
        super.remove(id);

        articleCache.removeArticle(id);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {
        JSONObject ret = articleCache.getArticle(id);
        if (null != ret) {
            return ret;
        }

        ret = super.get(id);

        if (null == ret) {
            return null;
        }

        articleCache.putArticle(ret);

        return ret;
    }

    @Override
    public void update(final String id, final JSONObject article) throws RepositoryException {
        super.update(id, article);

        article.put(Keys.OBJECT_ID, id);
        articleCache.putArticle(article);
    }

    @Override
    public List<JSONObject> getRandomly(final int fetchSize) throws RepositoryException {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final double mid = Math.random();

        Query query = new Query().setFilter(
                CompositeFilterOperator.and(new PropertyFilter(Article.ARTICLE_RANDOM_DOUBLE, FilterOperator.GREATER_THAN_OR_EQUAL, mid),
                        new PropertyFilter(Article.ARTICLE_RANDOM_DOUBLE, FilterOperator.LESS_THAN_OR_EQUAL, mid),
                        new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)))
                .setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);

        final JSONObject result1 = get(query);
        final JSONArray array1 = result1.optJSONArray(Keys.RESULTS);

        final List<JSONObject> list1 = CollectionUtils.<JSONObject>jsonArrayToList(array1);

        ret.addAll(list1);

        final int reminingSize = fetchSize - array1.length();

        if (0 != reminingSize) { // Query for remains
            query = new Query();
            query.setFilter(
                    CompositeFilterOperator.and(new PropertyFilter(Article.ARTICLE_RANDOM_DOUBLE, FilterOperator.GREATER_THAN_OR_EQUAL, 0D),
                            new PropertyFilter(Article.ARTICLE_RANDOM_DOUBLE, FilterOperator.LESS_THAN_OR_EQUAL, mid),
                            new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)));
            query.setCurrentPageNum(1);
            query.setPageSize(reminingSize);
            query.setPageCount(1);

            final JSONObject result2 = get(query);
            final JSONArray array2 = result2.optJSONArray(Keys.RESULTS);

            final List<JSONObject> list2 = CollectionUtils.<JSONObject>jsonArrayToList(array2);

            ret.addAll(list2);
        }

        return ret;
    }

    /**
     * Gets an article by the specified article title.
     *
     * @param articleTitle the specified article title
     * @return an article, {@code null} if not found
     * @throws RepositoryException repository exception
     */
    public JSONObject getByTitle(final String articleTitle) throws RepositoryException {
        final Query query = new Query().setFilter(new PropertyFilter(Article.ARTICLE_TITLE,
                FilterOperator.EQUAL, articleTitle)).setPageCount(1);

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
}
