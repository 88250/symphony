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

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.json.JSONObject;

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
     * Gets a book's information with the specified ISBN.
     *
     * @param isbn the specified ISBN
     * @return book info, returns {@code null} if not found
     */
    public JSONObject getBookByISBN(final String isbn) {
        final String url = "https://api.douban.com/v2/book/isbn/" + isbn ;

        final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        final HTTPRequest request = new HTTPRequest();

        try {
            request.setURL(new URL(url));

            final HTTPResponse response = urlFetchService.fetch(request);
            final String content = new String(response.getContent(), "UTF-8");
            final JSONObject result = new JSONObject(content);

            return result;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Query book by ISBN failed", e);

            return null;
        }
    }
}
