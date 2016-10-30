/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.util;

import java.io.FileReader;
import java.net.URL;
import org.testng.Assert;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.Latkes;
import org.testng.annotations.Test;

/**
 * Markdown utilities test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyrjung.github.io">Zephyr</a>
 * @version 2.1.0.2, Oct 30, 2016
 * @since 0.1.6
 */
public class MarkdownsTestCase {

    static {
        Latkes.initRuntimeEnv();
    }

    /**
     * Tag test.
     */
    @Test
    public void tag() {
        final String kbd = "<kbd>DV</kbd>";

        String html = Markdowns.toHTML(kbd);
        html = Markdowns.clean(html, "");

        Assert.assertTrue(html.contains("<kbd>") && html.contains("</kbd>"));
    }

    /**
     * XSS test.
     */
    @Test
    public void clean() {
        final String md = "<a href='data:text/html;base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K'>a link</a>";

        final String html = Markdowns.toHTML(md);
        final String securedHTML = Markdowns.clean(html, "");

        Assert.assertFalse(securedHTML.contains("href"));
    }

    /**
     * Auto space test.
     */
    @Test
    public void space() {
        final String md = "Sym是一个用Java写的实时论坛，欢迎来[体验Sym](https://hacpai.com)！";
        final String html = Markdowns.toHTML(md);

        Assert.assertEquals(html, "<p>Sym 是一个用 Java 写的实时论坛，欢迎来<a href=\"https://hacpai.com\">体验 Sym</a>！</p>");
    }

    /**
     * FIXME: https://github.com/sirthias/pegdown/issues/229
     */
    @Test
    public void toHTML1() {
        final String md = "Sym**是一个用_Java_写的实时论坛**";
        final String html = Markdowns.toHTML(md);

        Assert.assertEquals(html, "<p>Sym <strong>是一个用 <em>Java</em> 写的实时论坛</strong> </p>");
    }

    /**
     * Link test.
     */
    @Test
    public void toHTML() {
        String md = "[b3log](http://b3log.org)";
        String html = Markdowns.toHTML(md);
        Assert.assertTrue(html.contains("href"));

        md = "[b3log](b3log.org)";
        html = Markdowns.toHTML(md);
        Assert.assertTrue(html.contains("href"));
    }

    /**
     * Standard syntax test.
     *
     * @throws java.lang.Exception exception
     */
    @Test
    public void toHtml0() throws Exception {
        final URL mdResource = MarkdownsTestCase.class.getResource("/markdown_syntax.text");
        final String md = IOUtils.toString(new FileReader(mdResource.getPath()));
        final String html = Markdowns.toHTML(md);

        //System.out.println(html);
    }
}
