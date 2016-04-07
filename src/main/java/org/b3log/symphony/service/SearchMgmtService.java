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
import java.net.UnknownHostException;
import org.b3log.latke.Keys;
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
import org.json.JSONObject;

/**
 * Search management service.
 *
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.3, Apr 7, 2016
 * @since 1.4.0
 */
@Service
public class SearchMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchMgmtService.class.getName());

    /**
     * Elasticsearch index name.
     */
    public static final String ES_INDEX_NAME = "symphony";

    /**
     * Elasticsearch serve address.
     */
    public static final String ES_SERVER = Symphonys.get("es.server");

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Rebuilds ES index.
     */
    public void rebuildESIndex() {
        try {
            final HTTPRequest removeRequest = new HTTPRequest();
            removeRequest.setRequestMethod(HTTPRequestMethod.DELETE);
            removeRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
            URL_FETCH_SVC.fetch(removeRequest);

            final HTTPRequest createRequest = new HTTPRequest();
            createRequest.setRequestMethod(HTTPRequestMethod.PUT);
            createRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
            URL_FETCH_SVC.fetch(createRequest);

            final HTTPRequest mappingRequest = new HTTPRequest();
            mappingRequest.setRequestMethod(HTTPRequestMethod.POST);
            mappingRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + Article.ARTICLE + "/_mapping"));

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
     * Updates/Adds indexing the specified document in ES.
     *
     * @param doc the specified document
     * @param type the specified document type
     */
    public void updateESDocument(final JSONObject doc, final String type) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update"));

            final JSONObject payload = new JSONObject();
            payload.put("doc", doc);
            payload.put("upsert", doc);

            request.setPayload(payload.toString().getBytes("UTF-8"));

            URL_FETCH_SVC.fetchAsync(request);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates doc failed", e);
        }
    }

    /**
     * Removes the specified document in ES.
     *
     * @param doc the specified document
     * @param type the specified document type
     */
    public void removeESDocument(final JSONObject doc, final String type) {
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.DELETE);

        try {
            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID)));

            URL_FETCH_SVC.fetchAsync(request);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates doc failed", e);
        }
    }

    /**
     * Updates/Adds indexing the specified document in Algolia.
     *
     * @param doc the specified document
     */
    public void updateAlgoliaDocument(final JSONObject doc) {
        int retries = 3;

        while (retries > 0) {
            try {
                final String appId = Symphonys.get("algolia.appId");
                final String index = Symphonys.get("algolia.index");
                final String key = Symphonys.get("algolia.adminKey");

                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
                request.setRequestMethod(HTTPRequestMethod.PUT);

                final String id = doc.optString(Keys.OBJECT_ID);
                request.setURL(new URL("https://" + appId + "-dsn.algolia.net/1/indexes/" + index + "/" + id));

                request.setPayload(doc.toString().getBytes("UTF-8"));

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != response.getResponseCode()) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final UnknownHostException e) {
                LOGGER.log(Level.ERROR, "Index failed [UnknownHostException]");

                retries--;
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Index failed", e);

                break;
            }

            try {
                Thread.sleep(1000);
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Sleep error", e);

                break;
            }
        }
    }

    /**
     * Removes the specified document in Algolia.
     *
     * @param doc the specified document
     */
    public void removeAlgoliaDocument(final JSONObject doc) {
        int retries = 3;

        while (retries > 0) {
            try {
                final String appId = Symphonys.get("algolia.appId");
                final String index = Symphonys.get("algolia.index");
                final String key = Symphonys.get("algolia.adminKey");

                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
                request.setRequestMethod(HTTPRequestMethod.DELETE);

                final String id = doc.optString(Keys.OBJECT_ID);
                request.setURL(new URL("https://" + appId + "-dsn.algolia.net/1/indexes/" + index + "/" + id));

                request.setPayload(doc.toString().getBytes("UTF-8"));

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != response.getResponseCode()) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final UnknownHostException e) {
                LOGGER.log(Level.ERROR, "Remove object failed [UnknownHostException]");

                retries--;
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Remove object failed", e);

                break;
            }

            try {
                Thread.sleep(1000);
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Sleep error", e);

                break;
            }
        }
    }
}
