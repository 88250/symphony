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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
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
 * @version 1.0.0.1, Sep 9, 2016
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
     * @param baseURL the specified base URL
     * @param html the specified HTML
     * @return a list of links, each of them like this:      <pre>
     * {
     *     "linkAddr": "https://hacpai.com/article/1440573175609",
     *     "linkTitle": "黑客派简介",
     *     "linkKeywords": "",
     *     "linkHTML": "page HTML",
     *     "linkText": "page text",
     *     "linkBaiduRefCnt": int
     * }
     * </pre>
     */
    public static List<JSONObject> getLinks(final String baseURL, final String html) {
        final Document doc = Jsoup.parse(html, baseURL);
        final Elements urlElements = doc.select("a");

        final Set<String> urls = new HashSet<>();
        final List<Spider> spiders = new ArrayList<>();

        String url = null;
        for (final Element urlEle : urlElements) {
            try {
                url = urlEle.absUrl("href");
                if (StringUtils.isBlank(url) || !StringUtils.contains(url, "://")) {
                    url = StringUtils.substringBeforeLast(baseURL, "/") + url;
                }

                final URL formedURL = new URL(url);
                final String path = formedURL.getPath();
                if ("/".equals(path)) {
                    url = StringUtils.substringBeforeLast(url, "/");
                }

                if (StringUtils.contains(url, "#")) {
                    url = StringUtils.substringBefore(url, "#");
                }

                urls.add(url);
            } catch (final Exception e) {
                LOGGER.warn("Can't parse [" + url + "]");
            }
        }

        final List<JSONObject> ret = new ArrayList<>();

        try {
            for (final String u : urls) {
                spiders.add(new Spider(u));
            }

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

        Collections.sort(ret, new Comparator<JSONObject>() {
            @Override
            public int compare(final JSONObject link1, final JSONObject link2) {
                return link1.optInt(Link.LINK_BAIDU_REF_CNT) - link2.optInt(Link.LINK_BAIDU_REF_CNT);
            }
        });

        return ret;
    }

    static class Spider implements Callable<JSONObject> {

        private final String url;

        Spider(final String url) {
            this.url = url;
        }

        @Override
        public JSONObject call() throws Exception {
            final int TIMEOUT = 2000;

            try {
                final JSONObject ret = new JSONObject();

                // Get meta info of the URL
                final Connection.Response res = Jsoup.connect(url).timeout(TIMEOUT).followRedirects(false).execute();
                if (HttpServletResponse.SC_OK != res.statusCode()) {
                    return null;
                }

                String charset = res.charset();
                if (StringUtils.isBlank(charset)) {
                    charset = "UTF-8";
                }
                final String html = new String(res.bodyAsBytes(), charset);

                String title = StringUtils.substringBetween(html, "<title>", "</title>");
                title = StringUtils.trim(title);

                if (!containsChinese(title)) {
                    return null;
                }

                final String keywords = StringUtils.substringBetween(html, "eywords\" content=\"", "\"");

                ret.put(Link.LINK_ADDR, url);
                ret.put(Link.LINK_TITLE, title);
                ret.put(Link.LINK_T_KEYWORDS, keywords);
                ret.put(Link.LINK_T_HTML, html);
                final Document doc = Jsoup.parse(html);
                doc.select("pre").remove();
                ret.put(Link.LINK_T_TEXT, doc.text());

                // Evaluate the URL
                URL baiduURL = new URL("https://www.baidu.com/s?pn=0&wd=" + URLEncoder.encode(url, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) baiduURL.openConnection();
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.addRequestProperty("User-Agent", Symphonys.USER_AGENT_BOT);

                InputStream inputStream = conn.getInputStream();
                String baiduRes = IOUtils.toString(inputStream, "UTF-8");
                IOUtils.closeQuietly(inputStream);
                conn.disconnect();

                int baiduRefCnt = StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");
                if (1 > baiduRefCnt) {
                    ret.put(Link.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    LOGGER.debug(ret.optString(Link.LINK_ADDR));

                    return ret;
                } else {
                    baiduURL = new URL("https://www.baidu.com/s?pn=10&wd=" + URLEncoder.encode(url, "UTF-8"));
                    conn = (HttpURLConnection) baiduURL.openConnection();
                    conn.setConnectTimeout(TIMEOUT);
                    conn.setReadTimeout(TIMEOUT);
                    conn.addRequestProperty("User-Agent", Symphonys.USER_AGENT_BOT);

                    inputStream = conn.getInputStream();
                    baiduRes = IOUtils.toString(inputStream, "UTF-8");
                    IOUtils.closeQuietly(inputStream);
                    conn.disconnect();

                    baiduRefCnt += StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");

                    ret.put(Link.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    LOGGER.debug(ret.optString(Link.LINK_ADDR));

                    return ret;
                }
            } catch (final SocketTimeoutException e) {
                return null;
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Parses URL [" + url + "] failed", e);
                return null;
            }
        }
    }

    private static boolean containsChinese(final String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }

        final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        final Matcher m = p.matcher(str);

        return m.find();
    }

    public static void main(final String[] args) throws Exception {
        final String url = "https://github.com/helloqingfeng/Awsome-Front-End-learning-resource/tree/master/04-Front-end-tutorial-master";
        final Document doc = Jsoup.parse(new URL(url), 5000);
        final String html = doc.html();

        final List<JSONObject> links = getLinks(url, html);
        for (final JSONObject link : links) {
            LOGGER.info(link.optInt(Link.LINK_BAIDU_REF_CNT) + "  "
                    + link.optString(Link.LINK_ADDR) + "  "
                    + link.optString(Link.LINK_TITLE) + "  "
                    + link.optString(Link.LINK_T_KEYWORDS));
        }

        EXECUTOR_SERVICE.shutdown();
    }
}
