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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.URI;

/**
 * Network utilities.
 *
 * @author <a href="https://github.com/voidfyoo">Voidfyoo</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.0.0.0, Nov 11, 2026
 * @since 1.3.0
 */
public final class Networks {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Networks.class);

    /**
     * Checks the specified hostname is an inner address.
     *
     * @param host the specified hostname
     * @return {@code true} if it is, returns {@code false} otherwise
     */
    public static boolean isInnerAddress(final String host) {
        if (StringUtils.isBlank(host)) {
            return true;
        }

        try {
            for (final InetAddress address : InetAddress.getAllByName(host)) {
                if (isInnerAddress(address)) {
                    return true;
                }
            }

            return false;
        } catch (final Exception e) {
            LOGGER.log(Level.WARN, "Checks inner address failed: " + e.getMessage());

            return true;
        }
    }

    /**
     * Checks whether the specified URL is a safe HTTP/HTTPS URL for server-side fetching.
     *
     * @param url the specified URL
     * @return {@code true} if it is safe, returns {@code false} otherwise
     */
    public static boolean isSafeHttpUrl(final String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }

        try {
            final URI uri = new URI(url);
            final String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }

            String host = uri.getHost();
            if (StringUtils.isBlank(host)) {
                return false;
            }
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }

            return !isInnerAddress(host);
        } catch (final Exception e) {
            return false;
        }
    }

    private static boolean isInnerAddress(final InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        final byte[] bytes = address.getAddress();
        if (4 == bytes.length) {
            final int b0 = bytes[0] & 0xff;
            final int b1 = bytes[1] & 0xff;
            if (0 == b0) {
                return true;
            }
            if (100 == b0 && 64 <= b1 && 127 >= b1) {
                return true; // 100.64.0.0/10
            }
            if (192 == b0 && 0 == b1 && 0 == (bytes[2] & 0xff)) {
                return true; // 192.0.0.0/24
            }
            if (198 == b0 && (18 == b1 || 19 == b1)) {
                return true; // 198.18.0.0/15
            }
        } else if (16 == bytes.length) {
            if ((bytes[0] & 0xfe) == 0xfc) {
                return true; // fc00::/7
            }

            final boolean ipv4Mapped = 0 == bytes[0] && 0 == bytes[1] && 0 == bytes[2] && 0 == bytes[3]
                    && 0 == bytes[4] && 0 == bytes[5] && 0 == bytes[6] && 0 == bytes[7]
                    && 0 == bytes[8] && 0 == bytes[9] && (bytes[10] & 0xff) == 0xff && (bytes[11] & 0xff) == 0xff;
            if (ipv4Mapped) {
                final int b0 = bytes[12] & 0xff;
                final int b1 = bytes[13] & 0xff;
                return 0 == b0 || 127 == b0 || 10 == b0
                        || (172 == b0 && 16 <= b1 && 31 >= b1)
                        || (192 == b0 && 168 == b1)
                        || (100 == b0 && 64 <= b1 && 127 >= b1)
                        || (169 == b0 && 254 == b1);
            }
        }

        return false;
    }

    /**
     * Private constructor.
     */
    private Networks() {
    }
}