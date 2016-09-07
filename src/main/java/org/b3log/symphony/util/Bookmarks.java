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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Netscape Bookmark file parsing utilities.
 *
 * https://github.com/KM-Bookmarks/bookm-parser.
 *
 * @author unascribed
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 6, 2016
 * @since 1.6.0
 */
public final class Bookmarks {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Bookmarks.class.getName());

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(50);

    public static void main(final String[] args) throws Exception {
        final Parser parser = new Parser(new File("D:\\1.html"));
        parser.parse();

        final List<Spider> spiders = new ArrayList<>();
        for (final Bookmark bookmark : parser.bookmarks) {
            final URL url = bookmark.url;
            if ("/".equals(url.getPath())) {
                continue;
            }

            spiders.add(new Spider(url));
        }

        final List<Future<JSONObject>> results = EXECUTOR_SERVICE.invokeAll(spiders);
        for (final Future<JSONObject> result : results) {
            final JSONObject link = result.get();
            if (null == link) {
                continue;
            }

            LOGGER.info(link.optString("url") + "  " + link.optString("title") + "   " + link.optString("text"));
        }
    }

    static class Spider implements Callable<JSONObject> {

        private URL url;

        Spider(final URL url) {
            this.url = url;
        }

        @Override
        public JSONObject call() throws Exception {
            try {
                final Connection.Response res = Jsoup.connect(url.toString()).timeout(2000).execute();
                final String body = new String(res.bodyAsBytes(), res.charset());
                
                String title = StringUtils.substringBetween(body, "<title>", "</title>");
                title = StringUtils.trim(title);

                final JSONObject ret = new JSONObject();
                ret.put("url", url);
                ret.put("title", title);
                ret.put("body", body);
                
                final Document doc = Jsoup.parse(body);
                doc.select("pre").remove();
                        
                ret.put("text", doc.text());

                return ret;
            } catch (final Exception e) {
                return null;
            }
        }
    }

    static class Parser {

        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        int indent = 0;

        Bookmark bookmark = new Bookmark();

        List<Bookmark> bookmarks = new ArrayList<>();

        private final Element first;

        private final Deque<String> currentTag = new ArrayDeque<>(20);

        public Parser(File theDoc) throws IOException {
            Document doc = Jsoup.parse(theDoc, "UTF-8");
            first = doc.select("DL").first();
        }

        public void parse() {
            dlElement(first.children());
        }

        private void dlElement(Elements elems) {
            indent++;
            for (Element elem : elems) {
                switch (elem.nodeName()) {
                    case "dt":
                        if (bookmark.url != null) {
                            // System.out.println(bookmark.toString());
                            bookmarks.add(bookmark);

                            bookmark = new Bookmark();
                            bookmark.tag = String.join(":", currentTag);
                        }
                        dtElement(elem.children());
                        break;
                    case "dd":
                        //System.out.println("# Descr: " + elem.html());
                        bookmark.description = elem.html();
                        break;
                    case "dl":
                        dlElement(elem.children());
                        break;
                    case "p":
                        continue;
                    default:
                        //System.err.println("! No comprendo: " + elem.nodeName() + ": " + elem.html());
                        LOGGER.error("! No comprendo: " + elem.nodeName() + ": " + elem.html());
                }
            }
            if (--indent > 0) {
                currentTag.removeLast();
            }
        }

        private void dtElement(Elements elems) {
            for (Element elem : elems) {
                switch (elem.nodeName()) {
                    case "h3":
                        //System.out.println("# Tag: " + elem.ownText());
                        currentTag.addLast(elem.ownText());
                        bookmark.tag = String.join(":", currentTag);
                        break;
                    case "a":
                        //System.out.println("# "+ indent + " Link: " + elem.html());
                        bookmark.name = elem.html();

                        try {
                            bookmark.url = new URL(elem.attr("href"));
                        } catch (final Exception e) {
                        }

                        long dv = Long.parseLong(elem.attr("add_date"));   // http://stackoverflow.com/questions/539900/google-bookmark-export-date-format
                        if (dv > 10000000000l) {
                            dv = dv / 1000l;
                        } else {
                            dv = dv * 1000l;
                        }

                        bookmark.date = format.format(new Date(dv));
                        break;
                    case "dl":
                        dlElement(elem.children());
                        break;
                    case "p":
                        continue;
                    default:
                        //System.err.println("! No comprendo: " + elem.html());
                        LOGGER.error("! No comprendo: " + elem.html());
                }
            }
        }
    }

    static class Bookmark {

        public String name;

        public URL url;

        public String description;

        public String date;

        public String tag;

        @Override
        public String toString() {
            String sep = " ; ";
            return "\"" + name + "\"" + sep
                    + url + sep
                    + "\"" + description + "\"" + sep
                    + "\"" + date + "\"" + sep
                    + "\"" + tag + "\"";
        }
    }
}
