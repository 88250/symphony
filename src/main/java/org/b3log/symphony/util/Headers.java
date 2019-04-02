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

import jodd.io.upload.FileUpload;
import jodd.net.MimeTypes;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP header utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Feb 10, 2018
 * @since 2.8.0
 */
public final class Headers {

    /**
     * Gets suffix (for example jpg) of the specified file.
     *
     * @param file the specified file
     * @return suffix
     */
    public static String getSuffix(final FileUpload file) {
        final String fileName = file.getHeader().getFileName();
        String ret = StringUtils.substringAfterLast(fileName, ".");
        if (StringUtils.isNotBlank(ret)) {
            return ret;
        }

        final String contentType = file.getHeader().getContentType();
        return getSuffix(contentType);
    }

    /**
     * Gets suffix (for example jpg) with the specified content type.
     *
     * @param contentType the specified content type
     * @return suffix
     */
    public static String getSuffix(final String contentType) {
        String ret;
        final String[] exts = MimeTypes.findExtensionsByMimeTypes(contentType, false);
        if (null != exts && 0 < exts.length) {
            ret = exts[0];
        } else {
            ret = StringUtils.substringAfter(contentType, "/");
            ret = StringUtils.substringBefore(ret, ";");
        }

        return ret;
    }

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
