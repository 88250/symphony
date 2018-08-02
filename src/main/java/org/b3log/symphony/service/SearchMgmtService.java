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

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.net.MimeTypes;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;
import org.jsoup.Jsoup;

/**
 * Search management service.
 * <p>
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> or
 * <a href="https://www.algolia.com">Algolia</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.2.7, May 22, 2018
 * @since 1.4.0
 */
@Service
public class SearchMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SearchMgmtService.class);

    /**
     * Elasticsearch index name.
     */
    public static final String ES_INDEX_NAME = "symphony";

    /**
     * Elasticsearch serve address.
     */
    public static final String ES_SERVER = Symphonys.get("es.server");

    /**
     * Rebuilds ES index.
     */
    public void rebuildESIndex() {
        try {
            HttpRequest.delete(ES_SERVER + "/" + ES_INDEX_NAME).timeout(3000).send();
            HttpRequest.put(ES_SERVER + "/" + ES_INDEX_NAME).timeout(3000).send();

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

            HttpRequest.post(ES_SERVER + "/" + ES_INDEX_NAME + "/" + Article.ARTICLE + "/_mapping").bodyText(mapping.toString()).timeout(3000).contentTypeJson();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Removes index failed", e);
        }
    }

    /**
     * Rebuilds Algolia index.
     */
    public void rebuildAlgoliaIndex() {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            try {
                final HttpResponse response = HttpRequest.post("https://" + host + "/1/indexes/" + index + "/clear").
                        header("X-Algolia-API-Key", key).
                        header("X-Algolia-Application-Id", appId).timeout(5000).send();
                if (200 != response.statusCode()) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Clear index failed", e);

                retries++;
                if (retries > maxRetries) {
                    LOGGER.log(Level.ERROR, "Clear index failed", e);
                }
            }
        }
    }

    /**
     * Updates/Adds indexing the specified document in ES.
     *
     * @param doc  the specified document
     * @param type the specified document type
     */
    public void updateESDocument(final JSONObject doc, final String type) {
        try {
            final JSONObject payload = new JSONObject();
            payload.put("doc", doc);
            payload.put("upsert", doc);

            HttpRequest.post(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update").
                    bodyText(payload.toString()).contentTypeJson().timeout(5000).sendAsync();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates doc failed", e);
        }
    }

    /**
     * Removes the specified document in ES.
     *
     * @param doc  the specified document
     * @param type the specified document type
     */
    public void removeESDocument(final JSONObject doc, final String type) {
        try {
            HttpRequest.delete(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID)).timeout(5000).sendAsync();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Updates doc failed", e);
        }
    }

    /**
     * Updates/Adds indexing the specified document in Algolia.
     *
     * @param doc       the specified document
     * @param skipCheck the specified skip check flag
     */
    public void updateAlgoliaDocument(final JSONObject doc, final boolean skipCheck) {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            try {
                final String id = doc.optString(Keys.OBJECT_ID);

                String content = doc.optString(Article.ARTICLE_CONTENT);
                content = Markdowns.toHTML(content);
                content = Jsoup.parse(content).text();

                doc.put(Article.ARTICLE_CONTENT, content);
                final byte[] data = doc.toString().getBytes("UTF-8");

                if (!skipCheck && content.length() < 32) {
                    LOGGER.log(Level.INFO, "This article is too small [length=" + content.length() + "], so skip it [title="
                            + doc.optString(Article.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

                if (!skipCheck && data.length > 102400) {
                    LOGGER.log(Level.INFO, "This article is too big [length=" + data.length + "], so skip it [title="
                            + doc.optString(Article.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

                final HttpResponse response = HttpRequest.put("https://" + host + "/1/indexes/" + index + "/" + id).
                        header("X-Algolia-API-Key", key).
                        header("X-Algolia-Application-Id", appId).body(data, MimeTypes.MIME_APPLICATION_JSON).timeout(5000).send();
                response.charset("UTF-8");
                if (200 != response.statusCode()) {
                    LOGGER.warn(response.bodyText());
                }

                break;
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Index failed", e);

                retries++;
                if (retries > maxRetries) {
                    LOGGER.log(Level.ERROR, "Index failed [doc=" + doc + "]", e);
                }
            }

            try {
                Thread.sleep(100);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Sleep error", e);
            }
        }
    }

    /**
     * Removes the specified document in Algolia.
     *
     * @param doc the specified document
     */
    public void removeAlgoliaDocument(final JSONObject doc) {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            try {
                final String id = doc.optString(Keys.OBJECT_ID);
                final HttpResponse response = HttpRequest.delete("https://" + host + "/1/indexes/" + index + "/" + id).
                        header("X-Algolia-API-Key", key).
                        header("X-Algolia-Application-Id", appId).body(doc.toString().getBytes("UTF-8"), MimeTypes.MIME_APPLICATION_JSON).timeout(5000).send();
                if (200 != response.statusCode()) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Remove object failed", e);

                retries++;
                if (retries > maxRetries) {
                    LOGGER.log(Level.ERROR, "Remove object failed", e);
                }
            }
        }
    }
}
