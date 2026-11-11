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

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Network utilities test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 11, 2026
 * @since 3.6.4
 */
public class NetworksTestCase {

    @Test
    public void detectsInnerAddresses() {
        assertTrue(Networks.isInnerAddress("127.0.0.1"));
        assertTrue(Networks.isInnerAddress("localhost"));
        assertTrue(Networks.isInnerAddress("10.0.0.1"));
        assertTrue(Networks.isInnerAddress("172.16.0.1"));
        assertTrue(Networks.isInnerAddress("192.168.1.1"));
        assertTrue(Networks.isInnerAddress("169.254.169.254"));
        assertTrue(Networks.isInnerAddress("100.64.0.1"));
        assertTrue(Networks.isInnerAddress("::1"));
        assertTrue(Networks.isInnerAddress("fc00::1"));
        assertTrue(Networks.isInnerAddress(""));
        assertFalse(Networks.isInnerAddress("1.1.1.1"));
        assertFalse(Networks.isInnerAddress("8.8.8.8"));
    }

    @Test
    public void checksSafeHttpUrls() {
        assertFalse(Networks.isSafeHttpUrl(""));
        assertFalse(Networks.isSafeHttpUrl("file:///etc/passwd"));
        assertFalse(Networks.isSafeHttpUrl("javascript:alert(1)"));
        assertFalse(Networks.isSafeHttpUrl("http://127.0.0.1/"));
        assertFalse(Networks.isSafeHttpUrl("http://localhost/"));
        assertFalse(Networks.isSafeHttpUrl("http://169.254.169.254/latest/meta-data/"));
        assertFalse(Networks.isSafeHttpUrl("http://[::1]/"));
        assertTrue(Networks.isSafeHttpUrl("https://1.1.1.1/"));
    }
}
