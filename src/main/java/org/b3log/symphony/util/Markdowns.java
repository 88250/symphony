/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.LatkeBeanManagerImpl;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.b3log.latke.util.Callstacks;
import org.b3log.latke.util.MD5;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.service.UserQueryService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import org.pegdown.*;
import org.pegdown.ast.*;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * <a href="http://en.wikipedia.org/wiki/Markdown">Markdown</a> utilities.
 * <p>
 * Uses the <a href="https://github.com/chjj/marked">marked</a> as the processor, if not found this command, try
 * built-in <a href="https://github.com/sirthias/pegdown">pegdown</a> instead.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Vanessa</a>
 * @version 1.10.17.22, May 17, 2017
 * @since 0.2.0
 */
public final class Markdowns {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Markdowns.class);

    /**
     * Language service.
     */
    private static final LangPropsService LANG_PROPS_SERVICE
            = LatkeBeanManagerImpl.getInstance().getReference(LangPropsServiceImpl.class);

    /**
     * Bean manager.
     */
    private static final LatkeBeanManager beanManager = Lifecycle.getBeanManager();

    /**
     * User query service.
     */
    private static final UserQueryService userQueryService;

    /**
     * Markdown cache.
     */
    private static final Cache MD_CACHE = CacheFactory.getCache("markdown");

    /**
     * Markdown to HTML timeout.
     */
    private static final int MD_TIMEOUT = 2000;

    /**
     * Marked engine serve path.
     */
    private static final String MARKED_ENGINE_URL = "http://localhost:8250";

    /**
     * Whether marked is available.
     */
    public static boolean MARKED_AVAILABLE;

    static {
        MD_CACHE.setMaxCount(1024 * 10 * 4);

        if (null != beanManager) {
            userQueryService = beanManager.getReference(UserQueryService.class);
        } else {
            userQueryService = null;
        }
    }

    static {
        try {
            final URL url = new URL(MARKED_ENGINE_URL);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);

            final OutputStream outputStream = conn.getOutputStream();
            IOUtils.write("Symphony 大法好", outputStream, "UTF-8");
            IOUtils.closeQuietly(outputStream);

            final InputStream inputStream = conn.getInputStream();
            final String html = IOUtils.toString(inputStream, "UTF-8");
            IOUtils.closeQuietly(inputStream);

            conn.disconnect();

            MARKED_AVAILABLE = StringUtils.contains(html, "<p>Symphony 大法好</p>");

            if (MARKED_AVAILABLE) {
                LOGGER.log(Level.INFO, "[marked] is available, uses it for markdown processing");
            } else {
                LOGGER.log(Level.INFO, "[marked] is not available, uses built-in [pegdown] for markdown processing");
            }
        } catch (final Exception e) {
            LOGGER.log(Level.INFO, "[marked] is not available, uses built-in [pegdown] for markdown processing: "
                    + e.getMessage());
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

        final String tmp = Jsoup.clean(content, baseURI, Whitelist.relaxed().
                        addAttributes(":all", "id", "target", "class").
                        addTags("span", "hr", "kbd", "samp", "tt", "del", "s", "strike", "u").
                        addAttributes("iframe", "src", "width", "height", "border", "marginwidth", "marginheight").
                        addAttributes("audio", "controls", "src").
                        addAttributes("video", "controls", "src", "width", "height").
                        addAttributes("source", "src", "media", "type").
                        addAttributes("object", "width", "height", "data", "type").
                        addAttributes("param", "name", "value").
                        addAttributes("embed", "src", "type", "width", "height", "wmode", "allowNetworking"),
                outputSettings);
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

                continue;
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
        if (Strings.isEmptyOrNull(markdownText)) {
            return "";
        }

        final String cachedHTML = getHTML(markdownText);
        if (null != cachedHTML) {
            return cachedHTML;
        }

        final ExecutorService pool = Executors.newSingleThreadExecutor();
        final long[] threadId = new long[1];

        final Callable<String> call = () -> {
            threadId[0] = Thread.currentThread().getId();

            String html = LANG_PROPS_SERVICE.get("contentRenderFailedLabel");

            if (MARKED_AVAILABLE) {
                html = toHtmlByMarked(markdownText);

                if (!StringUtils.startsWith(html, "<p>")) {
                    html = "<p>" + html + "</p>";
                }
            } else {
                final PegDownProcessor pegDownProcessor
                        = new PegDownProcessor(Extensions.ALL_OPTIONALS | Extensions.ALL_WITH_OPTIONALS);

                final RootNode node = pegDownProcessor.parseMarkdown(markdownText.toCharArray());
                html = new ToHtmlSerializer(new LinkRenderer(), Collections.<String, VerbatimSerializer>emptyMap(),
                        Arrays.asList(new ToHtmlSerializerPlugin[0])).toHtml(node);

                if (!StringUtils.startsWith(html, "<p>")) {
                    html = "<p>" + html + "</p>";
                }

                html = formatMarkdown(html);
            }

            final Document doc = Jsoup.parse(html);
            final List<org.jsoup.nodes.Node> toRemove = new ArrayList<>();
            doc.traverse(new NodeVisitor() {
                @Override
                public void head(final org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.TextNode) {
                        final org.jsoup.nodes.TextNode textNode = (org.jsoup.nodes.TextNode) node;
                        final org.jsoup.nodes.Node parent = textNode.parent();

                        if (parent instanceof org.jsoup.nodes.Element) {
                            final Element parentElem = (Element) parent;

                            if (!parentElem.tagName().equals("code")) {
                                String text = textNode.getWholeText();

                                if (null != userQueryService) {
                                    try {
                                        final Set<String> userNames = userQueryService.getUserNames(text);
                                        for (final String userName : userNames) {
                                            text = text.replace('@' + userName + " ", "@<a href='" + Latkes.getServePath()
                                                    + "/member/" + userName + "'>" + userName + "</a> ");
                                        }
                                        text = text.replace("@participants ",
                                                "@<a href='https://hacpai.com/article/1458053458339' class='ft-red'>participants</a> ");
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
            LOGGER.log(Level.ERROR, "Markdown timeout [md=" + markdownText + "]");
            Callstacks.printCallstack(Level.ERROR, new String[]{"org.b3log"}, null);

            final Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (final Thread thread : threads) {
                if (thread.getId() == threadId[0]) {
                    thread.stop();

                    break;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Markdown failed [md=" + markdownText + "]", e);
        } finally {
            pool.shutdownNow();

            Stopwatchs.end();
        }

        return LANG_PROPS_SERVICE.get("contentRenderFailedLabel");
    }

    private static String toHtmlByMarked(final String markdownText) throws Exception {
        final URL url = new URL(MARKED_ENGINE_URL);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);

        final OutputStream outputStream = conn.getOutputStream();
        IOUtils.write(markdownText, outputStream, "UTF-8");
        IOUtils.closeQuietly(outputStream);

        final InputStream inputStream = conn.getInputStream();
        final String html = IOUtils.toString(inputStream, "UTF-8");
        IOUtils.closeQuietly(inputStream);

        //conn.disconnect();

        return html;
    }

    /**
     * See https://github.com/b3log/symphony/issues/306.
     *
     * @param markdownText
     * @return
     */
    private static String formatMarkdown(final String markdownText) {
        String ret = markdownText;

        final Document doc = Jsoup.parse(markdownText, "", Parser.htmlParser());
        final Elements tagA = doc.select("a");

        for (final Element aTagA : tagA) {
            final String search = aTagA.attr("href");
            final String replace = StringUtils.replace(search, "_", "[downline]");

            ret = StringUtils.replace(ret, search, replace);
        }

        final Elements tagImg = doc.select("img");
        for (final Element aTagImg : tagImg) {
            final String search = aTagImg.attr("src");
            final String replace = StringUtils.replace(search, "_", "[downline]");

            ret = StringUtils.replace(ret, search, replace);
        }

        final Elements tagCode = doc.select("code");
        for (final Element aTagCode : tagCode) {
            final String search = aTagCode.html();
            final String replace = StringUtils.replace(search, "_", "[downline]");

            ret = StringUtils.replace(ret, search, replace);
        }

        String[] rets = ret.split("\n");
        for (final String temp : rets) {
            final String[] toStrong = StringUtils.substringsBetween(temp, "**", "**");
            final String[] toEm = StringUtils.substringsBetween(temp, "_", "_");

            if (toStrong != null && toStrong.length > 0) {
                for (final String strong : toStrong) {
                    final String search = "**" + strong + "**";
                    final String replace = "<strong>" + strong + "</strong>";
                    ret = StringUtils.replace(ret, search, replace);
                }
            }

            if (toEm != null && toEm.length > 0) {
                for (final String em : toEm) {
                    final String search = "_" + em + "_";
                    final String replace = "<em>" + em + "<em>";
                    ret = StringUtils.replace(ret, search, replace);
                }
            }
        }

        ret = StringUtils.replace(ret, "[downline]", "_");

        return ret;
    }

    /**
     * Gets HTML for the specified markdown text.
     *
     * @param markdownText the specified markdown text
     * @return HTML
     */
    public static String getHTML(final String markdownText) {
        final String hash = MD5.hash(markdownText);

        return (String) MD_CACHE.get(hash);
    }

    /**
     * Puts the specified HTML into cache.
     *
     * @param markdownText the specified markdown text
     * @param html         the specified HTML
     */
    private static void putHTML(final String markdownText, final String html) {
        final String hash = MD5.hash(markdownText);

        MD_CACHE.put(hash, html);
    }

    /**
     * Enhanced with {@link Pangu} for text node.
     */
    private static class ToHtmlSerializer implements Visitor {

        protected final Map<String, ReferenceNode> references = new HashMap<String, ReferenceNode>();
        protected final Map<String, String> abbreviations = new HashMap<String, String>();
        protected final LinkRenderer linkRenderer;
        protected final List<ToHtmlSerializerPlugin> plugins;
        protected Printer printer = new Printer();
        protected TableNode currentTableNode;

        protected int currentTableColumn;

        protected boolean inTableHeader;

        protected Map<String, VerbatimSerializer> verbatimSerializers;

        public ToHtmlSerializer(LinkRenderer linkRenderer) {
            this(linkRenderer, Collections.<ToHtmlSerializerPlugin>emptyList());
        }

        public ToHtmlSerializer(LinkRenderer linkRenderer, List<ToHtmlSerializerPlugin> plugins) {
            this(linkRenderer, Collections.<String, VerbatimSerializer>emptyMap(), plugins);
        }

        public ToHtmlSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers) {
            this(linkRenderer, verbatimSerializers, Collections.<ToHtmlSerializerPlugin>emptyList());
        }

        public ToHtmlSerializer(final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers, final List<ToHtmlSerializerPlugin> plugins) {
            this.linkRenderer = linkRenderer;
            this.verbatimSerializers = new HashMap<>(verbatimSerializers);
            if (!this.verbatimSerializers.containsKey(VerbatimSerializer.DEFAULT)) {
                this.verbatimSerializers.put(VerbatimSerializer.DEFAULT, DefaultVerbatimSerializer.INSTANCE);
            }
            this.plugins = plugins;
        }

        public String toHtml(RootNode astRoot) {
            checkArgNotNull(astRoot, "astRoot");
            astRoot.accept(this);
            return printer.getString();
        }

        public void visit(RootNode node) {
            for (ReferenceNode refNode : node.getReferences()) {
                visitChildren(refNode);
                references.put(normalize(printer.getString()), refNode);
                printer.clear();
            }
            for (AbbreviationNode abbrNode : node.getAbbreviations()) {
                visitChildren(abbrNode);
                String abbr = printer.getString();
                printer.clear();
                abbrNode.getExpansion().accept(this);
                String expansion = printer.getString();
                abbreviations.put(abbr, expansion);
                printer.clear();
            }
            visitChildren(node);
        }

        public void visit(AbbreviationNode node) {
        }

        public void visit(AnchorLinkNode node) {
            printLink(linkRenderer.render(node));
        }

        public void visit(AutoLinkNode node) {
            printLink(linkRenderer.render(node));
        }

        public void visit(BlockQuoteNode node) {
            printIndentedTag(node, "blockquote");
        }

        public void visit(BulletListNode node) {
            printIndentedTag(node, "ul");
        }

        public void visit(CodeNode node) {
            printTag(node, "code");
        }

        public void visit(DefinitionListNode node) {
            printIndentedTag(node, "dl");
        }

        public void visit(DefinitionNode node) {
            printConditionallyIndentedTag(node, "dd");
        }

        public void visit(DefinitionTermNode node) {
            printConditionallyIndentedTag(node, "dt");
        }

        public void visit(ExpImageNode node) {
            String text = printChildrenToString(node);
            printImageTag(linkRenderer.render(node, text));
        }

        public void visit(ExpLinkNode node) {
            String text = printChildrenToString(node);
            printLink(linkRenderer.render(node, text));
        }

        public void visit(HeaderNode node) {
            printBreakBeforeTag(node, "h" + node.getLevel());
        }

        public void visit(HtmlBlockNode node) {
            String text = node.getText();
            if (text.length() > 0) {
                printer.println();
            }
            printer.print(text);
        }

        public void visit(InlineHtmlNode node) {
            printer.print(node.getText());
        }

        public void visit(ListItemNode node) {
            if (node instanceof TaskListNode) {
                // vsch: #185 handle GitHub style task list items, these are a bit messy because the <input> checkbox needs to be
                // included inside the optional <p></p> first grand-child of the list item, first child is always RootNode
                // because the list item text is recursively parsed.
                Node firstChild = node.getChildren().get(0).getChildren().get(0);
                boolean firstIsPara = firstChild instanceof ParaNode;
                int indent = node.getChildren().size() > 1 ? 2 : 0;
                boolean startWasNewLine = printer.endsWithNewLine();

                printer.println().print("<li class=\"task-list-item\">").indent(indent);
                if (firstIsPara) {
                    printer.println().print("<p>");
                    printer.print("<input type=\"checkbox\" class=\"task-list-item-checkbox\"" + (((TaskListNode) node).isDone() ? " checked=\"checked\"" : "") + " disabled=\"disabled\"></input>");
                    visitChildren((SuperNode) firstChild);

                    // render the other children, the p tag is taken care of here
                    visitChildrenSkipFirst(node);
                    printer.print("</p>");
                } else {
                    printer.print("<input type=\"checkbox\" class=\"task-list-item-checkbox\"" + (((TaskListNode) node).isDone() ? " checked=\"checked\"" : "") + " disabled=\"disabled\"></input>");
                    visitChildren(node);
                }
                printer.indent(-indent).printchkln(indent != 0).print("</li>")
                        .printchkln(startWasNewLine);
            } else {
                printConditionallyIndentedTag(node, "li");
            }
        }

        public void visit(MailLinkNode node) {
            printLink(linkRenderer.render(node));
        }

        public void visit(OrderedListNode node) {
            printIndentedTag(node, "ol");
        }

        public void visit(ParaNode node) {
            printBreakBeforeTag(node, "p");
        }

        public void visit(QuotedNode node) {
            switch (node.getType()) {
                case DoubleAngle:
                    printer.print("&laquo;");
                    visitChildren(node);
                    printer.print("&raquo;");
                    break;
                case Double:
                    printer.print("&ldquo;");
                    visitChildren(node);
                    printer.print("&rdquo;");
                    break;
                case Single:
                    printer.print("&lsquo;");
                    visitChildren(node);
                    printer.print("&rsquo;");
                    break;
            }
        }

        public void visit(ReferenceNode node) {
            // reference nodes are not printed
        }

        public void visit(RefImageNode node) {
            String text = printChildrenToString(node);
            String key = node.referenceKey != null ? printChildrenToString(node.referenceKey) : text;
            ReferenceNode refNode = references.get(normalize(key));
            if (refNode == null) { // "fake" reference image link
                printer.print("![").print(text).print(']');
                if (node.separatorSpace != null) {
                    printer.print(node.separatorSpace).print('[');
                    if (node.referenceKey != null) {
                        printer.print(key);
                    }
                    printer.print(']');
                }
            } else {
                printImageTag(linkRenderer.render(node, refNode.getUrl(), refNode.getTitle(), text));
            }
        }

        public void visit(RefLinkNode node) {
            String text = printChildrenToString(node);
            String key = node.referenceKey != null ? printChildrenToString(node.referenceKey) : text;
            ReferenceNode refNode = references.get(normalize(key));
            if (refNode == null) { // "fake" reference link
                printer.print('[').print(text).print(']');
                if (node.separatorSpace != null) {
                    printer.print(node.separatorSpace).print('[');
                    if (node.referenceKey != null) {
                        printer.print(key);
                    }
                    printer.print(']');
                }
            } else {
                printLink(linkRenderer.render(node, refNode.getUrl(), refNode.getTitle(), text));
            }
        }

        public void visit(SimpleNode node) {
            switch (node.getType()) {
                case Apostrophe:
                    printer.print("&rsquo;");
                    break;
                case Ellipsis:
                    printer.print("&hellip;");
                    break;
                case Emdash:
                    printer.print("&mdash;");
                    break;
                case Endash:
                    printer.print("&ndash;");
                    break;
                case HRule:
                    printer.println().print("<hr/>");
                    break;
                case Linebreak:
                    printer.print("<br/>");
                    break;
                case Nbsp:
                    printer.print("&nbsp;");
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        public void visit(StrongEmphSuperNode node) {
            if (node.isClosed()) {
                if (node.isStrong()) {
                    printTag(node, "strong");
                } else {
                    printTag(node, "em");
                }
            } else {
                //sequence was not closed, treat open chars as ordinary chars
                printer.print(node.getChars());
                visitChildren(node);
            }
        }

        public void visit(StrikeNode node) {
            printTag(node, "del");
        }

        public void visit(TableBodyNode node) {
            printIndentedTag(node, "tbody");
        }

        @Override
        public void visit(TableCaptionNode node) {
            printer.println().print("<caption>");
            visitChildren(node);
            printer.print("</caption>");
        }

        public void visit(TableCellNode node) {
            String tag = inTableHeader ? "th" : "td";
            List<TableColumnNode> columns = currentTableNode.getColumns();
            TableColumnNode column = columns.get(Math.min(currentTableColumn, columns.size() - 1));

            printer.println().print('<').print(tag);
            column.accept(this);
            if (node.getColSpan() > 1) {
                printer.print(" colspan=\"").print(Integer.toString(node.getColSpan())).print('"');
            }
            printer.print('>');
            visitChildren(node);
            printer.print('<').print('/').print(tag).print('>');

            currentTableColumn += node.getColSpan();
        }

        public void visit(TableColumnNode node) {
            switch (node.getAlignment()) {
                case None:
                    break;
                case Left:
                    printer.print(" align=\"left\"");
                    break;
                case Right:
                    printer.print(" align=\"right\"");
                    break;
                case Center:
                    printer.print(" align=\"center\"");
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        public void visit(TableHeaderNode node) {
            inTableHeader = true;
            printIndentedTag(node, "thead");
            inTableHeader = false;
        }

        public void visit(TableNode node) {
            currentTableNode = node;
            printIndentedTag(node, "table");
            currentTableNode = null;
        }

        public void visit(TableRowNode node) {
            currentTableColumn = 0;
            printIndentedTag(node, "tr");
        }

        public void visit(VerbatimNode node) {
            VerbatimSerializer serializer = lookupSerializer(node.getType());
            serializer.serialize(node, printer);
        }

        protected VerbatimSerializer lookupSerializer(final String type) {
            if (type != null && verbatimSerializers.containsKey(type)) {
                return verbatimSerializers.get(type);
            } else {
                return verbatimSerializers.get(VerbatimSerializer.DEFAULT);
            }
        }

        public void visit(WikiLinkNode node) {
            printLink(linkRenderer.render(node));
        }

        public void visit(TextNode node) {
            if (abbreviations.isEmpty()) {
                printer.print(Pangu.spacingText(node.getText()));
            } else {
                printWithAbbreviations(node.getText());
            }
        }

        public void visit(SpecialTextNode node) {
            printer.printEncoded(node.getText());
        }

        public void visit(SuperNode node) {
            visitChildren(node);
        }

        public void visit(Node node) {
            for (ToHtmlSerializerPlugin plugin : plugins) {
                if (plugin.visit(node, this, printer)) {
                    return;
                }
            }
            // override this method for processing custom Node implementations
            throw new RuntimeException("Don't know how to handle node " + node);
        }

        // helpers
        protected void visitChildren(SuperNode node) {
            for (Node child : node.getChildren()) {
                child.accept(this);
            }
        }

        // helpers
        protected void visitChildrenSkipFirst(SuperNode node) {
            boolean first = true;
            for (Node child : node.getChildren()) {
                if (!first) {
                    child.accept(this);
                }
                first = false;
            }
        }

        protected void printTag(TextNode node, String tag) {
            printer.print('<').print(tag).print('>');
            printer.printEncoded(node.getText());
            printer.print('<').print('/').print(tag).print('>');
        }

        protected void printTag(SuperNode node, String tag) {
            printer.print('<').print(tag).print('>');
            visitChildren(node);
            printer.print('<').print('/').print(tag).print('>');
        }

        protected void printBreakBeforeTag(SuperNode node, String tag) {
            boolean startWasNewLine = printer.endsWithNewLine();
            printer.println();
            printTag(node, tag);
            if (startWasNewLine) {
                printer.println();
            }
        }

        protected void printIndentedTag(SuperNode node, String tag) {
            printer.println().print('<').print(tag).print('>').indent(+2);
            visitChildren(node);
            printer.indent(-2).println().print('<').print('/').print(tag).print('>');
        }

        protected void printConditionallyIndentedTag(SuperNode node, String tag) {
            if (node.getChildren().size() > 1) {
                printer.println().print('<').print(tag).print('>').indent(+2);
                visitChildren(node);
                printer.indent(-2).println().print('<').print('/').print(tag).print('>');
            } else {
                boolean startWasNewLine = printer.endsWithNewLine();

                printer.println().print('<').print(tag).print('>');
                visitChildren(node);
                printer.print('<').print('/').print(tag).print('>').printchkln(startWasNewLine);
            }
        }

        protected void printImageTag(LinkRenderer.Rendering rendering) {
            printer.print("<img");
            printAttribute("src", rendering.href);
            // shouldn't include the alt attribute if its empty
            if (!rendering.text.equals("")) {
                printAttribute("alt", rendering.text);
            }
            for (LinkRenderer.Attribute attr : rendering.attributes) {
                printAttribute(attr.name, attr.value);
            }
            printer.print(" />");
        }

        protected void printLink(LinkRenderer.Rendering rendering) {
            printer.print('<').print('a');
            printAttribute("href", rendering.href);
            for (LinkRenderer.Attribute attr : rendering.attributes) {
                printAttribute(attr.name, attr.value);
            }
            printer.print('>').print(rendering.text).print("</a>");
        }

        protected void printAttribute(String name, String value) {
            printer.print(' ').print(name).print('=').print('"').print(value).print('"');
        }

        protected String printChildrenToString(SuperNode node) {
            Printer priorPrinter = printer;
            printer = new Printer();
            visitChildren(node);
            String result = printer.getString();
            printer = priorPrinter;
            return result;
        }

        protected String normalize(String string) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                switch (c) {
                    case ' ':
                    case '\n':
                    case '\t':
                        continue;
                }
                sb.append(Character.toLowerCase(c));
            }
            return sb.toString();
        }

        protected void printWithAbbreviations(String string) {
            Map<Integer, Map.Entry<String, String>> expansions = null;

            for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
                // first check, whether we have a legal match
                String abbr = entry.getKey();

                int ix = 0;
                while (true) {
                    int sx = string.indexOf(abbr, ix);
                    if (sx == -1) {
                        break;
                    }

                    // only allow whole word matches
                    ix = sx + abbr.length();

                    if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) {
                        continue;
                    }
                    if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
                        continue;
                    }

                    // ok, legal match so save an expansions "task" for all matches
                    if (expansions == null) {
                        expansions = new TreeMap<>();
                    }
                    expansions.put(sx, entry);
                }
            }

            if (expansions != null) {
                int ix = 0;
                for (Map.Entry<Integer, Map.Entry<String, String>> entry : expansions.entrySet()) {
                    int sx = entry.getKey();
                    String abbr = entry.getValue().getKey();
                    String expansion = entry.getValue().getValue();

                    printer.printEncoded(string.substring(ix, sx));
                    printer.print("<abbr");
                    if (org.parboiled.common.StringUtils.isNotEmpty(expansion)) {
                        printer.print(" title=\"");
                        printer.printEncoded(expansion);
                        printer.print('"');
                    }
                    printer.print('>');
                    printer.printEncoded(abbr);
                    printer.print("</abbr>");
                    ix = sx + abbr.length();
                }
                printer.print(string.substring(ix));
            } else {
                printer.print(string);
            }
        }
    }
}
