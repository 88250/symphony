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

import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.Iterator;

/**
 * Escape utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jan 31, 2019
 * @since 2.3.0
 */
public final class Escapes {
    /**
     * Sanitizes the specified file name.
     *
     * @param unsanitized the specified file name
     * @return sanitized file name
     */
    public static String sanitizeFilename(final String unsanitized) {
        return unsanitized.
                replaceAll("[\\?\\\\/:|<>\\*\\[\\]]", "-"). // ? \ / : | < > * [ ] to -
                replaceAll("\\s+", "-");              // white space to -
    }

    /**
     * Escapes the specified string.
     *
     * @param str the specified string
     */
    public static String escapeHTML(final String str) {
        return Encode.forHtml(str);
    }

    /**
     * Escapes string property in the specified JSON object.
     *
     * @param jsonObject the specified JSON object
     */
    public static void escapeHTML(final JSONObject jsonObject) {
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (jsonObject.opt(key) instanceof String) {
                jsonObject.put(key, Encode.forHtml(jsonObject.optString(key)));
            }
        }
    }

    /**
     * Private constructor.
     */
    private Escapes() {
    }

}
