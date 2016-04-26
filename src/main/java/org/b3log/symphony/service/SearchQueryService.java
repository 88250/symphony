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
package org.b3log.symphony.service;

import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPHeader;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Search query service.
 *
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> as the underlying engine. Uses
 * <a href="https://www.algolia.com">Algolia</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.1.1, Apr 1, 2016
 * @since 1.4.0
 */
@Service
public class SearchQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchQueryService.class.getName());

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Searches by Elasticsearch.
     *
     * @param type the specified document type
     * @param keyword the specified keyword
     * @param currentPage the specified current page number
     * @param pageSize the specified page size
     * @return search result, returns {@code null} if not found
     */
    public JSONObject searchElasticsearch(final String type, final String keyword, final int currentPage, final int pageSize) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(SearchMgmtService.ES_SERVER + "/" + SearchMgmtService.ES_INDEX_NAME + "/" + type
                    + "/_search"));

            final JSONObject reqData = new JSONObject();
            final JSONObject q = new JSONObject();
            final JSONObject and = new JSONObject();
            q.put("and", and);
            final JSONArray query = new JSONArray();
            and.put("query", query);
            final JSONObject or = new JSONObject();
            query.put(or);
            final JSONArray orClause = new JSONArray();
            or.put("or", orClause);

            final JSONObject content = new JSONObject();
            content.put(Article.ARTICLE_CONTENT, keyword);
            final JSONObject matchContent = new JSONObject();
            matchContent.put("match", content);
            orClause.put(matchContent);

            final JSONObject title = new JSONObject();
            title.put(Article.ARTICLE_TITLE, keyword);
            final JSONObject matchTitle = new JSONObject();
            matchTitle.put("match", title);
            orClause.put(matchTitle);

            reqData.put("query", q);
            reqData.put("from", currentPage);
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

            final JSONArray filter = new JSONArray();
            and.put("filter", filter);
            final JSONObject term = new JSONObject();
            filter.put(term);
            final JSONObject field = new JSONObject();
            term.put("term", field);
            field.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);

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
     * @param keyword the specified keyword
     * @param currentPage the specified current page number
     * @param pageSize the specified page size
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
                params.put("params", "query=" + URLEncoder.encode(keyword, "UTF-8")
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
