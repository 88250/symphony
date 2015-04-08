/*
 * Copyright (c) 2012-2015, b3log.org
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

import java.io.StringReader;
import java.io.StringWriter;
import org.b3log.latke.ioc.LatkeBeanManager;
import org.b3log.latke.ioc.Lifecycle;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.LangPropsServiceImpl;
import org.b3log.latke.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.tautua.markdownpapers.Markdown;
import org.tautua.markdownpapers.parser.ParseException;

/**
 * <a href="http://en.wikipedia.org/wiki/Markdown">Markdown</a> utilities.
 *
 * <p>
 * Uses the <a href="http://markdown.tautua.org/">MarkdownPapers</a> as the converter.
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.7, Apr 8, 2015
 * @since 0.2.0
 */
public final class Markdowns {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Markdowns.class.getName());

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

        final String tmp = Jsoup.clean(content,
                                       baseURI, Whitelist.relaxed().addAttributes(":all", "id", "target", "class", "style").addTags("span").
                                       addTags("hr").addTags("iframe").addAttributes("iframe", "src", "width", "height"),
                                       outputSettings);
        final Document doc = Jsoup.parse(tmp, baseURI, Parser.xmlParser());
        final Elements iframes = doc.getElementsByTag("iframe");

        for (final Element iframe : iframes) {
            final String src = iframe.attr("src");
            if (!src.startsWith("https://wide.b3log.org")) {
                iframe.remove();
            }
        }

        return doc.html();
    }

    /**
     * Converts the specified markdown text to HTML.
     *
     * @param markdownText the specified markdown text
     * @return converted HTML, returns {@code null} if the specified markdown text is "" or {@code null}, returns 'markdownErrorLabel' if
     * exception
     */
    public static String toHTML(final String markdownText) {
        if (Strings.isEmptyOrNull(markdownText)) {
            return null;
        }

        final StringWriter writer = new StringWriter();
        final Markdown markdown = new Markdown();

        try {
            markdown.transform(new StringReader(markdownText), writer);
        } catch (final ParseException e) {
            LOGGER.log(Level.ERROR, "Markdown error[text={0}]", markdownText);

            final LatkeBeanManager beanManager = Lifecycle.getBeanManager();
            final LangPropsService langPropsService = beanManager.getReference(LangPropsServiceImpl.class);

            return langPropsService.get("markdownErrorLabel");
        }

        return writer.toString();
    }

    /**
     * Private constructor.
     */
    private Markdowns() {
    }
}
