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

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MP3 player utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.2.0, Jul 15, 2018
 * @since 2.1.0
 */
public final class MP3Players {

    /**
     * MP3 URL regex.
     */
    private static final String MP3_URL_REGEX = "<p>( )*<a href.*\\.mp3.*</a>( )*</p>";

    /**
     * MP3 URL regex pattern.
     */
    private static final Pattern PATTERN = Pattern.compile(MP3_URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Renders the specified content with MP3 player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static final String render(final String content) {
        final StringBuffer contentBuilder = new StringBuffer();

        final Matcher m = PATTERN.matcher(content);
        while (m.find()) {
            final String g = m.group();
            String mp3Name = StringUtils.substringBetween(g, "\">", ".mp3</a>");
            String mp3URL = StringUtils.substringBetween(g, "href=\"", "\" rel=");
            if (StringUtils.isBlank(mp3URL)) {
                mp3URL = StringUtils.substringBetween(g, "href=\"", "\"");
            }

            m.appendReplacement(contentBuilder, "<div class=\"aplayer content-audio\" data-title=\""
                    + mp3Name + "\" data-url=\"" + mp3URL + "\" ></div>\n");
        }
        m.appendTail(contentBuilder);

        return contentBuilder.toString();
    }

    private MP3Players() {
    }
}
