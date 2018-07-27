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
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP header utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jul 27, 2018
 * @since 2.8.0
 */
public final class Headers {

    /**
     * Gets a value of a header specified by the given header name from the specified request.
     *
     * @param request    the specified request
     * @param name       the given header name
     * @param defaultVal the specified default value
     * @return header value, returns {@code defaultVal} if not found this header
     */
    public static String getHeader(final HttpServletRequest request, final String name, final String defaultVal) {
        String value = request.getHeader(name);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        }

        return Jsoup.clean(value, Whitelist.none());
    }

    /**
     * Private constructor.
     */
    private Headers() {
    }
}
