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
package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * User-Book-Article repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jan 16, 2017
 * @since 1.9.0
 */
@Repository
public class UserBookArticleRepository extends AbstractRepository {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(UserBookArticleRepository.class);

    /**
     * Public constructor.
     */
    public UserBookArticleRepository() {
        super(User.USER + "_" + Book.BOOK + "_" + Article.ARTICLE);
    }

    /**
     * Get a user-book-article relations by the specified article id.
     *
     * @param articleId the specified article id
     * @return for example      <pre>
     * {
     *         "oId": "",
     *         "userId": "",
     *         "bookId": "",
     *         "articleId": article id
     * }, returns {@code null} if not found
     * </pre>
     * @throws RepositoryException repository exception
     */
    public JSONObject getByArticleId(final String articleId) {
        final Query query = new Query().setFilter(
                new PropertyFilter(Article.ARTICLE_T_ID, FilterOperator.EQUAL, articleId)).
                setPageCount(1);

        JSONObject result = null;
        try {
            result = get(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get by article id failed", e);

            return null;
        }

        final JSONArray array = result.optJSONArray(Keys.RESULTS);
        if (0 == array.length()) {
            return null;
        }

        return array.optJSONObject(0);
    }
}
