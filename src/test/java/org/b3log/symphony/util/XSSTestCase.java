/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2019, b3log.org & hacpai.com
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

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * XSS test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Sep 14, 2017
 * @since 0.3.0
 */
public class XSSTestCase {

    @Test
    public void xss() {
        String src = "http://error\"  onerror=\"this.src='http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg';this.removeAttribute('onerror');if(!window.a){console.log('Where am I ?');window.a=1}";
        assertFalse(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));

        src = "http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg";
        assertTrue(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));

        src = "1\" onmouseover=alert(111);\"&p=10";
        assertTrue(Jsoup.isValid(src, Whitelist.none()));

        src = src.replace("\"", "");
        src = "<div onmouseover=alert(111);></div>";
        assertFalse(Jsoup.isValid(src, Whitelist.none()));
    }
}
