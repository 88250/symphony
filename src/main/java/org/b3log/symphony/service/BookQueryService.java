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
package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.urlfetch.*;
import org.b3log.symphony.model.Book;
import org.b3log.symphony.repository.BookRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URL;

/**
 * Book query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jan 3, 2017
 * @since 1.9.0
 */
@Service
public class BookQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AvatarQueryService.class);

    /**
     * Book repository.
     */
    @Inject
    private BookRepository bookRepository;

    /**
     * Gets a book's information with the specified ISBN.
     *
     * @param isbn the specified ISBN
     * @return book info, returns {@code null} if not found
     */
    @Transactional
    public JSONObject getBookByISBN(final String isbn) {
        final String url = "https://api.douban.com/v2/book/isbn/" + isbn;

        final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        final HTTPRequest request = new HTTPRequest();
        request.addHeader(new HTTPHeader("User-Agent", Symphonys.USER_AGENT_BOT));

        try {
            request.setURL(new URL(url));

            final HTTPResponse response = urlFetchService.fetch(request);
            final String content = new String(response.getContent(), "UTF-8");
            final JSONObject result = new JSONObject(content);

            JSONObject ret = bookRepository.getByISBN(isbn);
            boolean add = false;
            if (null == ret) {
                ret = new JSONObject();
                add = true;
            }

            ret.put(Book.BOOK_ALT_TITLE, result.optString("alt_title"));
            ret.put(Book.BOOK_AUTHOR, result.optJSONArray("author").toString());
            ret.put(Book.BOOK_AUTHOR_INTRO, result.optString("author_intro").replace("\n", "\n\n"));
            ret.put(Book.BOOK_BINDING, result.optString("binding"));
            ret.put(Book.BOOK_CATALOG, result.optString("catalog"));
            ret.put(Book.BOOK_CATEGORY, result.optJSONObject("series").optString("title"));
            ret.put(Book.BOOK_DOUBAN_URL, result.optString("alt"));
            ret.put(Book.BOOK_IMG_URL, result.optString("image"));
            ret.put(Book.BOOK_ISBN10, result.optString("isbn10"));
            ret.put(Book.BOOK_ISBN13, result.optString("isbn13"));
            ret.put(Book.BOOK_ORIGINAL_TITLE, result.optString("origin_title"));
            ret.put(Book.BOOK_PAGES, result.optString("pages"));
            ret.put(Book.BOOK_PRICE, result.optString("price"));
            ret.put(Book.BOOK_PUBLISH_DATE, result.optString("pubdate"));
            ret.put(Book.BOOK_PUBLISHER, result.optString("publisher"));
            ret.put(Book.BOOK_SUB_TITLE, result.optString("subtitle"));
            ret.put(Book.BOOK_SUMMARY, result.optString("summary").replace("\n", "\n\n"));

            final StringBuilder tagBuilder = new StringBuilder();
            final JSONArray tags = result.optJSONArray("tags");
            for (int i = 0; i < tags.length(); i++) {
                final JSONObject tag = tags.optJSONObject(i);

                tagBuilder.append(tag.optString("name")).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }
            ret.put(Book.BOOK_TAGS, tagBuilder.toString());

            ret.put(Book.BOOK_TITLE, result.optString("title"));
            ret.put(Book.BOOK_TRANSLATOR, result.optJSONArray("translator").toString());

            if (add) {
                bookRepository.add(ret);
            } else {
                bookRepository.update(ret.optString(Keys.OBJECT_ID), ret);
            }

            ret.put(Book.BOOK_TRANSLATOR, result.optJSONArray("translator"));
            ret.put(Book.BOOK_AUTHOR, result.optJSONArray("author"));

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Query book by ISBN failed", e);

            return null;
        }
    }
}
