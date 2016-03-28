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
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.URLFetchService;
import org.b3log.latke.urlfetch.URLFetchServiceFactory;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Search management service.
 *
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Mar 28, 2016
 * @since 1.4.0
 */
@Service
public class SearchMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchMgmtService.class.getName());

    /**
     * Index name.
     */
    public static final String INDEX_NAME = "symphony";

    /**
     * Elasticsearch serve address.
     */
    public static final String SERVER = Symphonys.get("es.server");

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Rebuilds index.
     */
    public void rebuildIndex() {
        try {
            final HTTPRequest removeRequest = new HTTPRequest();
            removeRequest.setRequestMethod(HTTPRequestMethod.DELETE);
            removeRequest.setURL(new URL(SERVER + "/" + INDEX_NAME));
            URL_FETCH_SVC.fetch(removeRequest);

            final HTTPRequest createRequest = new HTTPRequest();
            createRequest.setRequestMethod(HTTPRequestMethod.PUT);
            createRequest.setURL(new URL(SERVER + "/" + INDEX_NAME));
            URL_FETCH_SVC.fetch(createRequest);

            final HTTPRequest mappingRequest = new HTTPRequest();
            mappingRequest.setRequestMethod(HTTPRequestMethod.POST);
            mappingRequest.setURL(new URL(SERVER + "/" + INDEX_NAME + "/" + Article.ARTICLE + "/_mapping"));

            final JSONObject mapping = new JSONObject();
            final JSONObject article = new JSONObject();
            mapping.put(Article.ARTICLE, article);
            final JSONObject properties = new JSONObject();
            article.put("properties", properties);
            final JSONObject title = new JSONObject();
            properties.put(Article.ARTICLE_TITLE, title);
            title.put("type", "string");
            title.put("analyzer", "ik_smart");
            title.put("search_analyzer", "ik_smart");
            final JSONObject content = new JSONObject();
            properties.put(Article.ARTICLE_CONTENT, content);
            content.put("type", "string");
            content.put("analyzer", "ik_smart");
            content.put("search_analyzer", "ik_smart");
            
            mappingRequest.setPayload(mapping.toString().getBytes("UTF-8"));

            URL_FETCH_SVC.fetch(mappingRequest);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes index failed", e);
        }
    }

    /**
     * Updates/Adds indexing the specified document.
     *
     * @param doc the specified document
     * @param type the specified document type
     */
    public void updateDocument(final JSONObject doc, final String type) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(SERVER + "/" + INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update"));

            final JSONObject payload = new JSONObject();
            payload.put("doc", doc);
            payload.put("upsert", doc);

            request.setPayload(payload.toString().getBytes("UTF-8"));

            URL_FETCH_SVC.fetchAsync(request);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates doc failed", e);
        }
    }
}
