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

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * Network utilities.
 *
 * @author <a href="https://github.com/voidfyoo">Voidfyoo</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jan 23, 2019
 * @since 1.3.0
 */
public final class Networks {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Networks.class);

    /**
     * Checks the specified hostname is an inner address.
     *
     * @param host the specified hostname
     * @return {@code true} if it is, returns {@code false} otherwise
     */
    public static boolean isInnerAddress(final String host) {
        try {
            final int intAddress = ipToLong(host);
            return ipToLong("0.0.0.0") >> 24 == intAddress >> 24 ||
                    ipToLong("127.0.0.1") >> 24 == intAddress >> 24 ||
                    ipToLong("10.0.0.0") >> 24 == intAddress >> 24 ||
                    ipToLong("172.16.0.0") >> 20 == intAddress >> 20 ||
                    ipToLong("192.168.0.0") >> 16 == intAddress >> 16;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Checks inner address failed: " + e.getMessage());

            return true;
        }
    }

    private static int ipToLong(final String ip) throws Exception {
        return ByteBuffer.wrap(InetAddress.getByName(ip).getAddress()).getInt();
    }

    /**
     * Private constructor.
     */
    private Networks() {
    }
}