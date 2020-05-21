/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
 * Audio player utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.0.2.1, May 22, 2020
 * @since 2.1.0
 */
public final class AudioPlayers {

    /**
     * Audio (.mp3, .flac) URL regex.
     */
    private static final String MP3_URL_REGEX = "<p>( )*<a href.*\\.(mp3|flac).*</a>( )*</p>";

    /**
     * Audio URL regex pattern.
     */
    private static final Pattern PATTERN = Pattern.compile(MP3_URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Renders the specified content with audio player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static String render(final String content) {
        final StringBuffer contentBuilder = new StringBuffer();

        final Matcher m = PATTERN.matcher(content);
        while (m.find()) {
            final String g = m.group();
            String audioName = StringUtils.substringBetween(g, "\">", "</a>");
            audioName = StringUtils.substringBeforeLast(audioName, ".");
            String audioURL = StringUtils.substringBetween(g, "href=\"", "\" rel=");
            if (StringUtils.isBlank(audioURL)) {
                audioURL = StringUtils.substringBetween(g, "href=\"", "\"");
            }

            m.appendReplacement(contentBuilder, "<div class=\"aplayer content-audio\" data-title=\""
                    + audioName + "\" data-url=\"" + audioURL + "\" ></div>\n");
        }
        m.appendTail(contentBuilder);
        return contentBuilder.toString();
    }

    private AudioPlayers() {
    }
}
