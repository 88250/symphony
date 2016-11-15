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
 * @version 2.2.1.4, Nov 11, 2016
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
     * email test
     * not link to User page if the name is in email address
     */
    @Test
    public void linkToHTML(){
    	  String md = "test@test.com";
          String html = Markdowns.linkToHtml(md);
          System.out.println(html);
          Assert.assertEquals(html, "<p><a href=\"mailto:&#116;e&#115;&#x74;&#x40;&#x74;&#x65;&#115;&#x74;&#x2e;c&#111;&#x6d;\">&#116;e&#115;&#x74;&#x40;&#x74;&#x65;&#115;&#x74;&#x2e;c&#111;&#x6d;</a></p>");
    }
    
    /**
     * Workaround for https://github.com/sirthias/pegdown/issues/229.
     */
    @Test
    public void toHTML1() {
        String md = "Sym**是一个用 _Java_ 写的实时论坛**";
        String html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p>Sym<strong>是一个用 <em>Java</em> 写的实时论坛</strong></p>");

        md = "[link](https://github.com/b3log/symphony/blob/master/README_zh_CN.md)";
        html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p><a href=\"https://github.com/b3log/symphony/blob/master/README_zh_CN.md\">link</a></p>");

        md = "* [插件开发](https://docs.google.com/document/pub?id=15H7Q3EBo-44v61Xp_epiYY7vK_gPJLkQaT7T1gkE64w&pli=1)：插件机制、处理流程";
        html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p><ul>\n  <li><a href=\"https://docs.google.com/document/pub?id=15H7Q3EBo-44v61Xp_epiYY7vK_gPJLkQaT7T1gkE64w&pli=1\">插件开发</a>：插件机制、处理流程</li>\n</ul></p>");

        md = "<p>你好，黑客派</p>\n<ul>\n  <li>你好，**黑客派**</li>\n</ul>";
        html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p>你好，黑客派</p>\n<ul>\n  <li>你好，<strong>黑客派</strong></li>\n</ul>");

        md = "```\n"
                + "server {\n"
                + "    listen       443 ssl;\n"
                + "    server_name  usb.dev;\n"
                + "\n"
                + "    access_log off;\n"
                + "\n"
                + "    ssl on;\n"
                + "    ssl_certificate /etc/nginx/ssl/server.crt;\n"
                + "    ssl_certificate_key /etc/nginx/ssl/server.key;\n"
                + "    ssl_client_certificate /etc/nginx/ssl/ca.crt;\n"
                + "    ssl_verify_client on;\n"
                + "\n"
                + "    location / {\n"
                + "        proxy_pass http://backend$request_uri;\n"
                + "    }\n"
                + "}\n"
                + "```";
        html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p><pre><code>server {\n"
                + "    listen       443 ssl;\n"
                + "    server_name  usb.dev;\n"
                + "\n"
                + "    access_log off;\n"
                + "\n"
                + "    ssl on;\n"
                + "    ssl_certificate /etc/nginx/ssl/server.crt;\n"
                + "    ssl_certificate_key /etc/nginx/ssl/server.key;\n"
                + "    ssl_client_certificate /etc/nginx/ssl/ca.crt;\n"
                + "    ssl_verify_client on;\n"
                + "\n"
                + "    location / {\n"
                + "        proxy_pass http://backend$request_uri;\n"
                + "    }\n"
                + "}\n"
                + "</code></pre></p>");
        
        md = "然后新建一个study[downline]1文件夹，在文件夹下面新建一个index.html文件,*注意最后一个js代码的type*\n" +
             "github地址：https://github.com/Userwu/study[downline]react";
        html = Markdowns.toHTML(md);
        Assert.assertEquals(html, "<p>然后新建一个 study_1 文件夹，在文件夹下面新建一个 index.html 文件,*注意最后一个 js 代码的 type*<br/>github 地址：https://github.com/Userwu/study_react</p>");
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
