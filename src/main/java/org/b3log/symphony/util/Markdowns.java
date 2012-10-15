/*
 * Copyright (c) 2012, B3log Team
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
import org.b3log.latke.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.tautua.markdownpapers.Markdown;

/**
 * <a href="http://en.wikipedia.org/wiki/Markdown">Markdown</a> utilities.
 * 
 * <p>Uses the <a href="http://markdown.tautua.org/">MarkdownPapers</a> as the converter.</p>
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.1, Oct 15, 2012
 * @since 0.2.0
 */
public final class Markdowns {

    /**
     * Article content cleaner whitelist.
     */
    public static final Whitelist ARTICLE_CONTENT_WHITELIST = Whitelist.relaxed().addAttributes(":all", "id");

    /**
     * Gets the safe HTML content of the specified content.
     * 
     * @param content the specified content
     * @param baseURL the specified base URL, the relative path value of href will starts with this URL
     * @return safe HTML content
     */
    public static String clean(final String content, final String baseURL) {
        return Jsoup.clean(content, baseURL, ARTICLE_CONTENT_WHITELIST);
    }

    /**
     * Converts the specified markdown text to HTML.
     * 
     * @param markdownText the specified markdown text
     * @return converted HTML, returns {@code null} if the specified markdown text is "" or {@code null}
     * @throws Exception exception 
     */
    public static String toHTML(final String markdownText) throws Exception {
        if (Strings.isEmptyOrNull(markdownText)) {
            return null;
        }

        final StringWriter writer = new StringWriter();
        final Markdown markdown = new Markdown();

        markdown.transform(new StringReader(markdownText), writer);

        return writer.toString();
    }

    /**
     * Private constructor.
     */
    private Markdowns() {
    }
}
