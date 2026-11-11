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
 * Escape utilities test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 11, 2026
 * @since 3.6.4
 */
public class EscapesTestCase {

    @Test
    public void sanitizesTagCSS() {
        String ret = Escapes.sanitizeTagCSS("<style>a > b {color:red}</style><script>alert(1)</script>");
        assertTrue(ret.contains("<style>"));
        assertTrue(ret.contains("a > b"));
        assertFalse(ret.toLowerCase().contains("<script"));

        ret = Escapes.sanitizeTagCSS("<div onmouseover=\"alert(1)\">x</div>");
        assertFalse(ret.contains("onmouseover"));

        ret = Escapes.sanitizeTagCSS("a > b {background:url(javascript:alert(1))}");
        assertFalse(ret.toLowerCase().contains("javascript:"));
        assertFalse(ret.contains("<"));

        ret = Escapes.sanitizeTagCSS("<style>@import 'http://example.com/x.css';</style>");
        assertFalse(ret.toLowerCase().contains("@import"));
    }
}
