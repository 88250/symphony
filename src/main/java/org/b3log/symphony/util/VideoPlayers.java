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
import org.b3log.latke.ioc.BeanManager;
import org.b3log.latke.service.LangPropsService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Video player utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.1, Oct 25, 2018
 * @since 2.2.0
 */
public final class VideoPlayers {

    /**
     * Video URL regix.
     */
    private static final String VIDEO_URL_REGEX =
            "<p>( )*<a href.*\\.(rm|rmvb|3gp|avi|mpeg|mp4|wmv|mkv|dat|asf|flv|mov).*</a>( )*</p>";

    /**
     * Video URL regex pattern.
     */
    private static final Pattern PATTERN = Pattern.compile(VIDEO_URL_REGEX, Pattern.CASE_INSENSITIVE);

    /**
     * Renders the specified content with video player if need.
     *
     * @param content the specified content
     * @return rendered content
     */
    public static final String render(final String content) {
        final BeanManager beanManager = BeanManager.getInstance();
        final LangPropsService langPropsService = beanManager.getReference(LangPropsService.class);

        final StringBuffer contentBuilder = new StringBuffer();
        final Matcher m = PATTERN.matcher(content);

        while (m.find()) {
            final String g = m.group();
            String videoURL = StringUtils.substringBetween(g, "href=\"", "\" rel=");
            if (StringUtils.isBlank(videoURL)) {
                videoURL = StringUtils.substringBetween(g, "href=\"", "\"");
            }

            m.appendReplacement(contentBuilder, "<video width=\"100%\" src=\""
                    + videoURL + "\" controls=\"controls\">" + langPropsService.get("notSupportPlayLabel") + "</video>\n");
        }
        m.appendTail(contentBuilder);

        return contentBuilder.toString();
    }

    private VideoPlayers() {
    }
}
