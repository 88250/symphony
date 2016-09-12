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
package org.b3log.symphony.util;

import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Link;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Link utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 7, 2016
 * @since 1.6.0
 */
public final class Links {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Links.class.getName());

    /**
     * Thread pool.
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(50);

    /**
     * Gets links from the specified HTML.
     *
     * @param html the specified HTML
     * @return a list of links, each of them like this:      <pre>
     * {
     *     "url": "https://hacpai.com/article/1440573175609",
     *     "title": "黑客派简介",
     *     "html": "page HTML",
     *     "text": "page text",
     *     "baiduRefCnt": int
     * }
     * </pre>
     */
    public static List<JSONObject> getLinks(final String html) {
        final Document doc = Jsoup.parse(html);
        final Elements urlElements = doc.select("a");

        final List<Spider> spiders = new ArrayList<>();
        for (final Element urlEle : urlElements) {
            try {
                final String url = urlEle.attr("href");
                if ("/".equals(url)) {
                    continue;
                }

                spiders.add(new Spider(url));
            } catch (final Exception e) {
                LOGGER.warn("Can't parse [" + urlEle.attr("href") + "]");
            }
        }

        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<Future<JSONObject>> results = EXECUTOR_SERVICE.invokeAll(spiders);
            for (final Future<JSONObject> result : results) {
                final JSONObject link = result.get();
                if (null == link) {
                    continue;
                }

                ret.add(link);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses URLs failed", e);
        }

        return ret;
    }

    static class Spider implements Callable<JSONObject> {

        private final String url;

        Spider(final String url) {
            this.url = url;
        }

        @Override
        public JSONObject call() throws Exception {
            try {
                final Connection.Response res = Jsoup.connect(url).timeout(2000).execute();
                final String html = new String(res.bodyAsBytes(), res.charset());

                String title = StringUtils.substringBetween(html, "<title>", "</title>");
                title = StringUtils.trim(title);

                final String keywords = StringUtils.substringBetween(html, "eywords\" content=\"", "\"");

                final JSONObject ret = new JSONObject();
                ret.put(Link.LINK_ADDR, url);
                ret.put(Link.LINK_TITLE, title);
                ret.put(Link.LINK_T_KEYWORDS, keywords);
                ret.put(Link.LINK_T_HTML, html);
                final Document doc = Jsoup.parse(html);
                doc.select("pre").remove();
                ret.put(Link.LINK_T_TEXT, doc.text());

                final URL baiduURL = new URL("http://www.baidu.com/s?wd="
                        + URLEncoder.encode(url, "UTF-8"));
                final HttpURLConnection conn = (HttpURLConnection) baiduURL.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                final InputStream inputStream = conn.getInputStream();
                final String baiduRes = IOUtils.toString(inputStream, "UTF-8");
                IOUtils.closeQuietly(inputStream);
                conn.disconnect();

                String baiduRefCnt = StringUtils.substringBetween(baiduRes, "百度为您找到相关结果约", "个");
                baiduRefCnt = StringUtils.replace(baiduRefCnt, ",", "");
                if (StringUtils.isBlank(baiduRefCnt)) {
                    baiduRefCnt = "0";
                }
                ret.put(Link.LINK_BAIDU_REF_CNT, Integer.valueOf(baiduRefCnt));

                return ret;
            } catch (final Exception e) {
                return null;
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final String html = IOUtils.toString(new FileReader("D:\\1.html"));
        final List<JSONObject> links = getLinks(html);
        for (final JSONObject link : links) {
            LOGGER.info(link.optInt(Link.LINK_BAIDU_REF_CNT) + "  "
                    + link.optString(Link.LINK_ADDR) + "  "
                    + link.optString(Link.LINK_TITLE) + "  "
                    + link.optString(Link.LINK_T_KEYWORDS));
        }

        EXECUTOR_SERVICE.shutdown();
    }
}
