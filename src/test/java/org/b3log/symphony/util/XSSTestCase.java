/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version 1.0.0.0, May 14, 2015
 * @since 0.3.0
 */
public class XSSTestCase {

    @Test
    public void xss() {
        String src = "http://error\"  onerror=\"this.src='http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg';this.removeAttribute('onerror');if(!window.a){console.log('Where am I ?');window.a=1}";
        assertFalse(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));

        src = "http://7u2fje.com1.z0.glb.clouddn.com/girl.jpg";
        assertTrue(Jsoup.isValid("<img src=\"" + src + "\"/>", Whitelist.basicWithImages()));
    }
}
