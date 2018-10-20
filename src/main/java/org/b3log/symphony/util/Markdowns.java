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

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.options.DataHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.util.Callstacks;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.service.UserQueryService;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * <a href="http://en.wikipedia.org/wiki/Markdown">Markdown</a> utilities.
 * <p>
 * Uses the <a href="https://github.com/chjj/marked">marked</a> as the processor, if not found this command, try
 * built-in <a href="https://github.com/vsch/flexmark-java">flexmark</a> instead.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Vanessa</a>
 * @version 1.11.21.7, Oct 20, 2018
 * @since 0.2.0
 */
public final class Markdowns {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Markdowns.class);

    /**
     * Markdown cache.
     */
    private static final Map<String, JSONObject> MD_CACHE = new ConcurrentHashMap<>();

    /**
     * Markdown to HTML timeout.
     */
    private static final int MD_TIMEOUT = Symphonys.getInt("markdown.timeout");

    /**
     * Marked engine serve path.
     */
    private static final String MARKED_ENGINE_URL = "http://localhost:8250";

    /**
     * Built-in MD engine options.
     */
    private static final DataHolder OPTIONS = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL_WITH_OPTIONALS);

    /**
     * Built-in MD engine parser.
     */
    private static final com.vladsch.flexmark.parser.Parser PARSER =
            com.vladsch.flexmark.parser.Parser.builder(OPTIONS).build();

    /**
     * Built-in MD engine HTML renderer.
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    /**
     * Whether marked is available.
     */
    public static boolean MARKED_AVAILABLE;

    static {
        try {
            final URL url = new URL(MARKED_ENGINE_URL);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            try (final OutputStream outputStream = conn.getOutputStream()) {
                IOUtils.write("Symphony 大法好", outputStream, "UTF-8");
            }

            String html;
            try (final InputStream inputStream = conn.getInputStream()) {
                html = IOUtils.toString(inputStream, "UTF-8");
            }

            conn.disconnect();

            MARKED_AVAILABLE = StringUtils.contains(html, "<p>Symphony 大法好</p>");

            if (MARKED_AVAILABLE) {
                LOGGER.log(Level.INFO, "[marked] is available, uses it for markdown processing");
            } else {
                LOGGER.log(Level.INFO, "[marked] is not available, uses built-in [flexmark] for markdown processing");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.INFO, "[marked] is not available caused by [" + e.getMessage() + "], uses built-in [flexmark] for markdown processing");
        }
    }

    /**
     * Private constructor.
     */
    private Markdowns() {
    }

    /**
     * Gets the safe HTML content of the specified content.
     *
     * @param content the specified content
     * @param baseURI the specified base URI, the relative path value of href will starts with this URL
     * @return safe HTML content
     */
    public static String clean(final String content, final String baseURI) {
        final Document.OutputSettings outputSettings = new Document.OutputSettings();
        outputSettings.prettyPrint(false);

        final Whitelist whitelist = Whitelist.relaxed().addAttributes(":all", "id", "target", "class", "data-src", "aria-name", "aria-label");
        inputWhitelist(whitelist);
        final String tmp = Jsoup.clean(content, baseURI, whitelist, outputSettings);
        final Document doc = Jsoup.parse(tmp, baseURI, Parser.htmlParser());

        final Elements ps = doc.getElementsByTag("p");
        for (final Element p : ps) {
            p.removeAttr("style");
        }

        final Elements iframes = doc.getElementsByTag("iframe");
        for (final Element iframe : iframes) {
            final String src = StringUtils.deleteWhitespace(iframe.attr("src"));
            if (StringUtils.startsWithIgnoreCase(src, "javascript")
                    || StringUtils.startsWithIgnoreCase(src, "data:")) {
                iframe.remove();
            }
        }

        final Elements objs = doc.getElementsByTag("object");
        for (final Element obj : objs) {
            final String data = StringUtils.deleteWhitespace(obj.attr("data"));
            if (StringUtils.startsWithIgnoreCase(data, "data:")
                    || StringUtils.startsWithIgnoreCase(data, "javascript")) {
                obj.remove();

                continue;
            }

            final String type = StringUtils.deleteWhitespace(obj.attr("type"));
            if (StringUtils.containsIgnoreCase(type, "script")) {
                obj.remove();
            }
        }

        final Elements embeds = doc.getElementsByTag("embed");
        for (final Element embed : embeds) {
            final String data = StringUtils.deleteWhitespace(embed.attr("src"));
            if (StringUtils.startsWithIgnoreCase(data, "data:")
                    || StringUtils.startsWithIgnoreCase(data, "javascript")) {
                embed.remove();
            }
        }

        final Elements as = doc.getElementsByTag("a");
        for (final Element a : as) {
            a.attr("rel", "nofollow");

            final String href = a.attr("href");
            if (href.startsWith(Latkes.getServePath())) {
                continue;
            }

            a.attr("target", "_blank");
        }

        final Elements audios = doc.getElementsByTag("audio");
        for (final Element audio : audios) {
            audio.attr("preload", "none");
        }

        final Elements videos = doc.getElementsByTag("video");
        for (final Element video : videos) {
            video.attr("preload", "none");
        }

        final Elements forms = doc.getElementsByTag("form");
        for (final Element form : forms) {
            form.remove();
        }

        final Elements inputs = doc.getElementsByTag("input");
        for (final Element input : inputs) {
            if (!"checkbox".equalsIgnoreCase(input.attr("type"))) {
                input.remove();
            }
        }

        String ret = doc.body().html();
        ret = ret.replaceAll("(</?br\\s*/?>\\s*)+", "<br>"); // patch for Jsoup issue

        return ret;
    }

    /**
     * Converts the specified markdown text to HTML.
     *
     * @param markdownText the specified markdown text
     * @return converted HTML, returns an empty string "" if the specified markdown text is "" or {@code null}, returns
     * 'markdownErrorLabel' if exception
     */
    public static String toHTML(final String markdownText) {
        if (StringUtils.isBlank(markdownText)) {
            return "";
        }

        final String cachedHTML = getHTML(markdownText);
        if (null != cachedHTML) {
            return cachedHTML;
        }

        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);
        final UserQueryService userQueryService = beanManager.getReference(UserQueryService.class);
        final ExecutorService pool = Executors.newSingleThreadExecutor();
        final long[] threadId = new long[1];

        final Callable<String> call = () -> {
            threadId[0] = Thread.currentThread().getId();

            String html = langPropsService.get("contentRenderFailedLabel");

            if (MARKED_AVAILABLE) {
                try {
                    html = toHtmlByMarked(markdownText);
                    if (!StringUtils.startsWith(html, "<p>")) {
                        html = "<p>" + html + "</p>";
                    }
                } catch (final Exception e) {
                    LOGGER.log(Level.WARN, "Failed to use [marked] for markdown [md=" + StringUtils.substring(markdownText, 0, 256) + "]: " + e.getMessage());

                    com.vladsch.flexmark.ast.Node document = PARSER.parse(markdownText);
                    html = RENDERER.render(document);
                    if (!StringUtils.startsWith(html, "<p>")) {
                        html = "<p>" + html + "</p>";
                    }
                }
            } else {
                com.vladsch.flexmark.ast.Node document = PARSER.parse(markdownText);
                html = RENDERER.render(document);
                if (!StringUtils.startsWith(html, "<p>")) {
                    html = "<p>" + html + "</p>";
                }
            }

            final Whitelist whitelist = Whitelist.relaxed();
            inputWhitelist(whitelist);
            html = Jsoup.clean(html, whitelist);
            final Document doc = Jsoup.parse(html);
            final List<org.jsoup.nodes.Node> toRemove = new ArrayList<>();
            doc.traverse(new NodeVisitor() {
                @Override
                public void head(final org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.TextNode) {
                        final org.jsoup.nodes.TextNode textNode = (org.jsoup.nodes.TextNode) node;
                        final org.jsoup.nodes.Node parent = textNode.parent();

                        if (parent instanceof Element) {
                            final Element parentElem = (Element) parent;

                            if (!parentElem.tagName().equals("code")) {
                                String text = textNode.getWholeText();
                                boolean nextIsBr = false;
                                final org.jsoup.nodes.Node nextSibling = textNode.nextSibling();
                                if (nextSibling instanceof Element) {
                                    nextIsBr = "br".equalsIgnoreCase(((Element) nextSibling).tagName());
                                }

                                if (null != userQueryService) {
                                    try {
                                        final Set<String> userNames = userQueryService.getUserNames(text);
                                        for (final String userName : userNames) {
                                            text = text.replace('@' + userName + (nextIsBr ? "" : " "), "@" + UserExt.getUserLink(userName));
                                        }
                                        text = text.replace("@participants ",
                                                "@<a href='https://hacpai.com/article/1458053458339' target='_blank' class='ft-red'>participants</a> ");
                                    } finally {
                                        JdbcRepository.dispose();
                                    }
                                }

                                if (text.contains("@<a href=")) {
                                    final List<org.jsoup.nodes.Node> nodes = Parser.parseFragment(text, parentElem, "");
                                    final int index = textNode.siblingIndex();

                                    parentElem.insertChildren(index, nodes);
                                    toRemove.add(node);
                                } else {
                                    textNode.text(Pangu.spacingText(text));
                                }
                            }
                        }
                    }
                }

                @Override
                public void tail(org.jsoup.nodes.Node node, int depth) {
                }
            });

            toRemove.forEach(node -> node.remove());

            doc.select("pre>code").addClass("hljs");
            doc.select("a").forEach(a -> {
                String src = a.attr("href");
                if (StringUtils.containsIgnoreCase(src, "javascript:")) {
                    a.remove();

                    return;
                }

                if (StringUtils.startsWithAny(src, new String[]{Latkes.getServePath(), Symphonys.get("qiniu.domain")})
                        || StringUtils.endsWithIgnoreCase(src, ".mov")) {
                    return;
                }

                src = URLs.encode(src);
                a.attr("href", Latkes.getServePath() + "/forward?goto=" + src);
                a.attr("target", "_blank");
                a.attr("rel", "nofollow");
            });
            doc.outputSettings().prettyPrint(false);

            String ret = doc.select("body").html();
            ret = StringUtils.trim(ret);

            // cache it
            putHTML(markdownText, ret);

            return ret;
        };

        Stopwatchs.start("Md to HTML");
        try {
            final Future<String> future = pool.submit(call);

            return future.get(MD_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            LOGGER.log(Level.ERROR, "Markdown timeout [md=" + StringUtils.substring(markdownText, 0, 256) + "]");
            Callstacks.printCallstack(Level.ERROR, new String[]{"org.b3log"}, null);

            final Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (final Thread thread : threads) {
                if (thread.getId() == threadId[0]) {
                    thread.stop();

                    break;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Markdown failed [md=" + StringUtils.substring(markdownText, 0, 256) + "]", e);
        } finally {
            pool.shutdownNow();

            Stopwatchs.end();
        }

        return langPropsService.get("contentRenderFailedLabel");
    }

    private static String toHtmlByMarked(final String markdownText) throws Exception {
        final URL url = new URL(MARKED_ENGINE_URL);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(100);
        conn.setReadTimeout(1000);
        conn.setDoOutput(true);

        try (final OutputStream outputStream = conn.getOutputStream()) {
            IOUtils.write(markdownText, outputStream, "UTF-8");
        }

        String ret;
        try (final InputStream inputStream = conn.getInputStream()) {
            ret = IOUtils.toString(inputStream, "UTF-8");
        }
        //conn.disconnect();

        return ret;
    }

    /**
     * Gets HTML for the specified markdown text.
     *
     * @param markdownText the specified markdown text
     * @return HTML
     */
    private static String getHTML(final String markdownText) {
        final String hash = DigestUtils.md5Hex(markdownText);
        final JSONObject value = MD_CACHE.get(hash);
        if (null == value) {
            return null;
        }

        return value.optString(Common.DATA);
    }

    /**
     * Puts the specified HTML into cache.
     *
     * @param markdownText the specified markdown text
     * @param html         the specified HTML
     */
    private static void putHTML(final String markdownText, final String html) {
        final String hash = DigestUtils.md5Hex(markdownText);
        final JSONObject value = new JSONObject();
        value.put(Common.DATA, html);
        MD_CACHE.put(hash, value);
    }

    private static void inputWhitelist(final Whitelist whitelist) {
        whitelist.addTags("span", "hr", "kbd", "samp", "tt", "del", "s", "strike", "u").
                addAttributes("iframe", "src", "width", "height", "border", "marginwidth", "marginheight").
                addAttributes("audio", "controls", "src").
                addAttributes("video", "controls", "src", "width", "height").
                addAttributes("source", "src", "media", "type").
                addAttributes("object", "width", "height", "data", "type").
                addAttributes("param", "name", "value").
                addAttributes("input", "type", "disabled", "checked").
                addAttributes("embed", "src", "type", "width", "height", "wmode", "allowNetworking").
                addAttributes("code", "class").
                addAttributes("span", "class");
    }
}
