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
package org.b3log.symphony.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Link;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Apr 5, 2018
 * @since 1.6.0
 */
public final class Links {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Links.class);

    /**
     * Gets link card from the specified URL.
     *
     * @param url the specified URL
     * @return link card like this: <pre>
     * {
     *     "linkTitle": "黑客派简介 - 黑客派",
     *     "linkAddr": "https://hacpai.com/article/1440573175609",
     *     "linkDescription: "欢迎来到黑客派，这里是 B3log 开源社区的线上论坛，是程序员和设计师的聚集地，目前已经有 [链接]加入。 HacPai 分别取 Hacker / Painter 的头三个字母组成，源自[链 ..",
     *     "linkImage": "https://img.hacpai.com/avatar/1353745196354_1515855515308.png?imageView2/1/w/242/h/242/interlace/0/q/100",
     *     "linkSite": "黑客派",
     *     "linkSiteDomain": "hacpai.com",
     *     "linkSiteAddr": "https://hacpai.com",
     *     "linkSiteIcon": "https://static.hacpai.com/images/favicon.png"
     * }
     * </pre>, returns default result if not found
     */
    public static JSONObject getLinkCard(String url) {
        final JSONObject ret = new JSONObject();
        try {
            if (StringUtils.startsWith(url, "//")) {
                url = "https://" + url;
            }

            ret.put(Link.LINK_TITLE, "");
            ret.put(Link.LINK_ADDR, url);
            ret.put(Link.LINK_T_DESCRIPTION, "");
            ret.put(Link.LINK_T_IMAGE, "");
            ret.put(Link.LINK_T_SITE, "");
            ret.put(Link.LINK_T_SITE_DOMAIN, url);
            ret.put(Link.LINK_T_SITE_ADDR, url);
            ret.put(Link.LINK_T_SITE_ICON, "");
            final URL u = new URL(url);
            ret.put(Link.LINK_T_SITE_DOMAIN, u.getHost());
            ret.put(Link.LINK_T_SITE_ADDR, u.getProtocol() + "://" + u.getHost());
            ret.put(Link.LINK_T_SITE_ICON, "");

            if (!Strings.isURL(url)) {
                return ret;
            }

            String suffix = null;
            final int idx = u.getPath().lastIndexOf(".");
            if (0 < idx) {
                suffix = u.getPath().substring(idx + 1);
            }
            if (StringUtils.isNotBlank(suffix) && !StringUtils.equalsIgnoreCase("html", suffix)) {
                return ret;
            }

            final Connection.Response res = Jsoup.connect(url).timeout(2000).followRedirects(true).userAgent(Symphonys.USER_AGENT_BOT).execute();
            if (HttpServletResponse.SC_OK != res.statusCode()) {
                return ret;
            }

            String charset = res.charset();
            if (StringUtils.isBlank(charset)) {
                charset = "UTF-8";
            }
            final String contentType = res.contentType();
            if (!StringUtils.containsIgnoreCase(contentType, "text/html")) {
                return ret;
            }
            final String html = new String(res.bodyAsBytes(), charset);

            final JSONObject parsed = parseCard(url, html);
            if (null == parsed) {
                return ret;
            }

            return parsed;
        } catch (final SocketTimeoutException e) {
            try {
                final String spiderServer = Symphonys.get("proxy.spiderServer");
                if (StringUtils.isBlank(spiderServer)) {
                    return ret;
                }

                final Connection.Response res = Jsoup.connect(spiderServer + "?url=" + url).timeout(2000).followRedirects(true).userAgent(Symphonys.USER_AGENT_BOT).execute();
                if (HttpServletResponse.SC_OK != res.statusCode()) {
                    return ret;
                }

                String charset = res.charset();
                if (StringUtils.isBlank(charset)) {
                    charset = "UTF-8";
                }
                final String contentType = res.contentType();
                if (!StringUtils.containsIgnoreCase(contentType, "text/html")) {
                    return ret;
                }
                final String html = new String(res.bodyAsBytes(), charset);

                final JSONObject parsed = parseCard(url, html);
                if (null == parsed) {
                    return ret;
                }

                return parsed;
            } catch (final Exception e0) {
                LOGGER.log(Level.ERROR, "Gets link card [" + url + "] via proxy failed: " + e.getMessage());

                return ret;
            }
        } catch (final UnsupportedMimeTypeException e) {
            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets link card for [" + url + "] failed: " + e.getMessage());

            return ret;
        }
    }

    private static JSONObject parseCard(final String url, final String html) {
        try {
            final Document doc = Jsoup.parse(html);
            final Elements titleElem = doc.getElementsByTag("title");
            String title = titleElem.text();
            final Elements ogTitleElem = doc.select("meta[property=\"og:title\"]");
            final String ogTitle = StringUtils.trim(ogTitleElem.attr("content"));
            if (StringUtils.isNotBlank(ogTitle)) {
                title = ogTitle;
            }
            String addr = url;
            final Elements ogURLElem = doc.select("meta[property=\"og:url\"]");
            String ogURL = StringUtils.trim(ogURLElem.attr("content"));
            if (StringUtils.isNotBlank(ogURL)) {
                if (StringUtils.startsWith(ogURL, "//")) {
                    ogURL = "https:" + ogURL;
                }
                addr = ogURL;
            }
            String description = "";
            final Elements descElem = doc.select("meta[name=\"description\"]");
            description = StringUtils.trim(descElem.attr("content"));
            final Elements ogDescElem = doc.select("meta[property=\"og:description\"]");
            final String ogDesc = StringUtils.trim(ogDescElem.attr("content"));
            if (StringUtils.isNotBlank(ogDesc)) {
                description = ogDesc;
            }
            String image = "";
            final Elements ogImgElem = doc.select("meta[property=\"og:image\"]");
            String ogImg = StringUtils.trim(ogImgElem.attr("content"));
            if (StringUtils.isNotBlank(ogImg)) {
                if (StringUtils.startsWith(ogImg, "//")) {
                    ogImg = "https:" + ogImg;
                }
                image = ogImg;
            }
            if (StringUtils.isBlank(image)) {
                final Elements tImgElem = doc.select("meta[name=\"twitter:image\"]");
                String tImg = StringUtils.trim(tImgElem.attr("content"));
                if (StringUtils.isNotBlank(tImg)) {
                    if (StringUtils.startsWith(tImg, "//")) {
                        tImg = "https:" + tImg;
                    }
                    image = tImg;
                }
            }
            String site = "";
            final Elements ogSiteElem = doc.select("meta[property=\"og:site_name\"]");
            final String ogSite = StringUtils.trim(ogSiteElem.attr("content"));
            if (StringUtils.isNotBlank(ogSite)) {
                site = ogSite;
            }

            final URL u = new URL(addr);
            final String siteAddr = u.getProtocol() + "://" + u.getHost();
            final String siteDomain = u.getHost();

            String siteIcon;
            final Elements siteIconElem = doc.select("link[rel=\"icon\"]");
            siteIcon = StringUtils.trim(siteIconElem.attr("href"));
            if (StringUtils.isNotBlank(siteIcon) && !StringUtils.startsWith(siteIcon, "http")) {
                siteIcon = siteAddr + "/" + siteIcon;
            }

            final JSONObject ret = new JSONObject();
            ret.put(Link.LINK_TITLE, title);
            ret.put(Link.LINK_ADDR, addr);
            ret.put(Link.LINK_T_DESCRIPTION, description);
            ret.put(Link.LINK_T_IMAGE, image);
            ret.put(Link.LINK_T_SITE, site);
            ret.put(Link.LINK_T_SITE_DOMAIN, siteDomain);
            ret.put(Link.LINK_T_SITE_ADDR, siteAddr);
            ret.put(Link.LINK_T_SITE_ICON, siteIcon);

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses link card [url=" + url + ", html=" + html + "] failed", e);

            return null;
        }
    }

    /**
     * Gets link from the specified URL.
     *
     * @param url the specified URL
     * @return link like this: <pre>
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
    public static JSONObject getLink(final String url) {
        try {
            return Symphonys.EXECUTOR_SERVICE.submit(new Spider(url)).get();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Parses URL [" + url + "] failed", e);

            return null;
        }
    }

    /**
     * Gets links from the specified HTML.
     *
     * @param baseURL the specified base URL
     * @param html    the specified HTML
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
                final String protocol = formedURL.getProtocol();
                final String host = formedURL.getHost();
                final int port = formedURL.getPort();
                final String path = formedURL.getPath();

                url = protocol + "://" + host;
                if (-1 != port && 80 != port && 443 != port) {
                    url += ":" + port;
                }
                url += path;

                if (StringUtils.endsWith(url, "/")) {
                    url = StringUtils.substringBeforeLast(url, "/");
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

            final List<Future<JSONObject>> results = Symphonys.EXECUTOR_SERVICE.invokeAll(spiders);
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

        Collections.sort(ret, Comparator.comparingInt(link -> link.optInt(Link.LINK_BAIDU_REF_CNT)));

        return ret;
    }

    private static boolean containsChinese(final String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }

        final Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        final Matcher m = p.matcher(str);

        return m.find();
    }

    static class Spider implements Callable<JSONObject> {
        private final String url;

        Spider(final String url) {
            this.url = url;
        }

        @Override
        public JSONObject call() {
            final int TIMEOUT = 5000;

            try {
                final JSONObject ret = new JSONObject();

                // Get meta info of the URL
                final Connection.Response res = Jsoup.connect(url).timeout(TIMEOUT).followRedirects(true).execute();
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
                title = Emotions.toAliases(title);

                final String keywords = StringUtils.substringBetween(html, "eywords\" content=\"", "\"");

                ret.put(Link.LINK_ADDR, url);
                ret.put(Link.LINK_TITLE, title);
                ret.put(Link.LINK_T_KEYWORDS, keywords);
                ret.put(Link.LINK_T_HTML, html);
                final Document doc = Jsoup.parse(html);
                doc.select("pre").remove();
                ret.put(Link.LINK_T_TEXT, doc.text());

                // Evaluate the URL
                URL baiduURL = new URL("https://www.baidu.com/s?pn=0&wd=" + URLs.encode(url));
                HttpURLConnection conn = (HttpURLConnection) baiduURL.openConnection();
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.addRequestProperty(Common.USER_AGENT, Symphonys.USER_AGENT_BOT);

                String baiduRes;
                try (final InputStream inputStream = conn.getInputStream()) {
                    baiduRes = IOUtils.toString(inputStream, "UTF-8");
                }
                conn.disconnect();

                int baiduRefCnt = StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");
                if (1 > baiduRefCnt) {
                    ret.put(Link.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    // LOGGER.debug(ret.optString(Link.LINK_ADDR));

                    return ret;
                } else {
                    baiduURL = new URL("https://www.baidu.com/s?pn=10&wd=" + URLs.encode(url));
                    conn = (HttpURLConnection) baiduURL.openConnection();
                    conn.setConnectTimeout(TIMEOUT);
                    conn.setReadTimeout(TIMEOUT);
                    conn.addRequestProperty(Common.USER_AGENT, Symphonys.USER_AGENT_BOT);

                    try (final InputStream inputStream = conn.getInputStream()) {
                        baiduRes = IOUtils.toString(inputStream, "UTF-8");
                    }
                    conn.disconnect();

                    baiduRefCnt += StringUtils.countMatches(baiduRes, "<em>" + url + "</em>");

                    ret.put(Link.LINK_BAIDU_REF_CNT, baiduRefCnt);
                    // LOGGER.debug(ret.optString(Link.LINK_ADDR));

                    return ret;
                }
            } catch (final SocketTimeoutException e) {
                return null;
            } catch (final Exception e) {
                LOGGER.log(Level.TRACE, "Parses URL [" + url + "] failed", e);

                return null;
            }
        }
    }
}
