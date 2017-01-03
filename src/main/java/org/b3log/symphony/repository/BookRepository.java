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
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.model.Book;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Book repository.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jan 3, 2017
 * @since 1.9.0
 */
@Repository
public class BookRepository extends AbstractRepository {

    /**
     * Public constructor.
     */
    public BookRepository() {
        super(Book.BOOK);
    }

    /**
     * Gets a book with the specified ISBN.
     *
     * @param isbn the specified ISBN
     * @return a book, returns {@code null} if not found
     * @throws RepositoryException reposiory exception
     */
    public JSONObject getByISBN(final String isbn) throws RepositoryException {
        final Query query = new Query().setFilter(CompositeFilterOperator.or(
                new PropertyFilter(Book.BOOK_ISBN10, FilterOperator.EQUAL, isbn),
                new PropertyFilter(Book.BOOK_ISBN13, FilterOperator.EQUAL, isbn)
        ));

        final JSONArray books = get(query).optJSONArray(Keys.RESULTS);
        if (books.length() < 1) {
            return null;
        }
        return books.optJSONObject(0);
    }
}
