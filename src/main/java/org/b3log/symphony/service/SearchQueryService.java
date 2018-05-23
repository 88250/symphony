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

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.*;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.URLs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.UnknownHostException;

/**
 * Search query service.
 * <p>
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> or <a href="https://www.algolia.com">Algolia</a> as the
 * underlying engine.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.2.1.2, Aug 23, 2016
 * @since 1.4.0
 */
@Service
public class SearchQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchQueryService.class);

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Searches by Elasticsearch.
     *
     * @param type        the specified document type
     * @param keyword     the specified keyword
     * @param currentPage the specified current page number
     * @param pageSize    the specified page size
     * @return search result, returns {@code null} if not found
     */
    public JSONObject searchElasticsearch(final String type, final String keyword, final int currentPage, final int pageSize) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);
        try {
            request.setURL(new URL(SearchMgmtService.ES_SERVER + "/" + SearchMgmtService.ES_INDEX_NAME + "/" + type
                    + "/_search"));

            final JSONObject reqData = new JSONObject();
            final JSONObject query = new JSONObject();
            final JSONObject bool = new JSONObject();
            query.put("bool", bool);
            final JSONObject must = new JSONObject();
            bool.put("must", must);
            final JSONObject queryString = new JSONObject();
            must.put("query_string", queryString);
            queryString.put("query", "*" + keyword + "*");
            queryString.put("fields", new String[]{Article.ARTICLE_TITLE, Article.ARTICLE_CONTENT});
            queryString.put("default_operator", "and");
            reqData.put("query", query);
            reqData.put("from", (currentPage - 1) * pageSize);
            reqData.put("size", pageSize);
            final JSONArray sort = new JSONArray();
            final JSONObject sortField = new JSONObject();
            sort.put(sortField);
            sortField.put(Article.ARTICLE_CREATE_TIME, "desc");
            sort.put("_score");
            reqData.put("sort", sort);

            final JSONObject highlight = new JSONObject();
            reqData.put("highlight", highlight);
            highlight.put("number_of_fragments", 3);
            highlight.put("fragment_size", 150);
            final JSONObject fields = new JSONObject();
            highlight.put("fields", fields);
            final JSONObject contentField = new JSONObject();
            fields.put(Article.ARTICLE_CONTENT, contentField);
            LOGGER.debug(reqData.toString(4));
            request.setPayload(reqData.toString().getBytes("UTF-8"));
            final HTTPResponse response = URL_FETCH_SVC.fetch(request);

            return new JSONObject(new String(response.getContent(), "UTF-8"));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Queries failed", e);

            return null;
        }
    }

    /**
     * Searches by Algolia.
     *
     * @param keyword     the specified keyword
     * @param currentPage the specified current page number
     * @param pageSize    the specified page size
     * @return search result, returns {@code null} if not found
     */
    public JSONObject searchAlgolia(final String keyword, final int currentPage, final int pageSize) {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            try {
                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));

                request.setRequestMethod(HTTPRequestMethod.POST);
                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/query"));

                final JSONObject params = new JSONObject();
                params.put("params", "query=" + URLs.encode(keyword)
                        + "&getRankingInfo=1&facets=*&attributesToRetrieve=*&highlightPreTag=%3Cem%3E"
                        + "&highlightPostTag=%3C%2Fem%3E"
                        + "&facetFilters=%5B%5D&maxValuesPerFacet=100"
                        + "&hitsPerPage=" + pageSize + "&page=" + (currentPage - 1));

                request.setPayload(params.toString().getBytes("UTF-8"));

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);

                final JSONObject ret = new JSONObject(new String(response.getContent(), "UTF-8"));
                if (200 != response.getResponseCode()) {
                    LOGGER.warn(ret.toString(4));

                    return null;
                }

                return ret;
            } catch (final UnknownHostException e) {
                LOGGER.log(Level.ERROR, "Queries failed [UnknownHostException=" + host + "]");

                retries++;

                if (retries > maxRetries) {
                    LOGGER.log(Level.ERROR, "Queries failed [UnknownHostException]");
                }
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Queries failed", e);

                break;
            }
        }

        return null;
    }
}
