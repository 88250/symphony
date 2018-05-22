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

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

import java.net.URLEncoder;

/**
 * URL utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jan 1, 2018
 * @since 2.4.0
 */
public final class URLs {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(URLs.class);

    /**
     * Encodes the specified URL.
     *
     * @param url the specified URL
     * @return encoded URL
     */
    public static String encode(final String url) {
        String ret = url;
        try {
            ret = URLEncoder.encode(url, "UTF-8");
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Encodes URL [" + url + "] failed", e);
        }

        return ret;
    }

    /**
     * Private constructor.
     */
    private URLs() {
    }
}
