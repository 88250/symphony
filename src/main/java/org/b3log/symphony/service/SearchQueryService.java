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
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Search query service.
 *
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jan 22, 2016
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
     * Searches.
     *
     * @param type the specified document type
     * @param keyword the specified keyword
     * @param currentPage the specified current page number
     * @param pageSize the specified page size
     * @return search result
     */
    public JSONObject search(final String type, final String keyword, final int currentPage, final int pageSize) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(SearchMgmtService.SERVER + "/" + SearchMgmtService.INDEX_NAME + "/" + type
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

            return new JSONObject();
        }
    }
}
