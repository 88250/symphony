/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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

import java.io.FileReader;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.Assert;
import org.apache.commons.io.IOUtils;
import org.b3log.latke.Latkes;
import org.testng.annotations.Test;

/**
 * Markdown utilities test case.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 2.4.1.7, May 2, 2017
 * @since 0.1.6
 */
public class MarkdownsTestCase {

    static {
        Latkes.initRuntimeEnv();
    }

    @Test
    public void jsoupParse() {
        final Document parse = Jsoup.parse("<p><strong><br>大家提提意见</strong></p>\n" +
                "<hr>\n" +
                "<p>一 基本情况<br> -学习和掌握  java （）年<br> -学习和掌握  javascript（）年<br> -在搭好框架的前提下，写一个中等复杂度的查添删改的时间（）小时<br> -是否有能力独立写一个自己的blog网站（），如果能需要的时间（）天<br> -英文水平（  ）<br> -业余时间的安排（           ）</p>\n" +
                "<p>一 选择题（答案统一写在第十题下面）</p>\n" +
                "<p>1有一段java 应用程序，它的主类名是a1，那么保存它的源文件名可以是（）<br>  A ) a1.java            B) a1.class             C) a1                    D) 都对</p>\n" +
                "<p>2  Java中（）<br>  A) 一个子类可以有多个父类，一个父类也可以有多个子类<br>  B) 一个子类可以有多个父类，但一个父类只可以有一个子类<br>  C) 一个子类可以有一个父类，但一个父类可以有多个子类<br>  D)上述说法都不对</p>\n" +
                "<p>3  main方法是Java应用程序执行的入口点，关于main方法的方法头以下哪项是合法的？<br>       A)public  static  void  main（）<br>      B)public  static  void   main（ String[]  args ）<br>   C)public  static int  main（String  [] arg ）<br>   D)public  void  main（String  arg[] ）</p>\n" +
                "<p>4 在Java中，一个类可同时定义许多同名的方法，这些方法的形式参数个数、类型或顺序各不相同，传回的值也可以不相同。这种面向对象程序的特性称为（ ）<br>A)隐藏              B)覆盖               C)重载         D)Java不支持此特性</p>\n" +
                "<p>5  A派生出子类B，B派生出子类C，并且在Java源代码中有如下声明： </p>\n" +
                "<pre><code>1.    A  a0=new  A(); \n" +
                "</code></pre><ol>\n" +
                "<li>A  a1 =new  B(); </li>\n" +
                "<li>A  a2=new  C();<br>问以下哪个说法是正确的？ （      ）<br>A)只有第1行能通过编译<br>B)第1、2行能通过编译，但第3行编译出错<br>C)第1、2、3行能通过编译，但第2、3行运行时出错<br>D)第1行、第2行和第3行的声明都是正确的 </li>\n" +
                "</ol>\n" +
                "<p>6 关于以下程序段，正确的说法是（ ）<br>     1．  String  s1=“a”+“b”;<br>    2．   String  s2=new  String（s1）；<br>3．    if（s1= =s2）<br>4．       System.out.println(“= =  is succeeded”);<br>5．     if (s1.equals(s2))<br>6．        System.out.println(“.equals() is succeeded”);<br>A)行4与行6都将执行<br>B)行4执行，行6不执行<br>C)行6执行，行4不执行<br> D)行4、行6都不执行 </p>\n" +
                "<p>7 设计用户表时，身份证号为固定18位长，对该字段最好采用（）数据类型</p>\n" +
                "<p>A)int B) char C) varchar D)text </p>\n" +
                "<p>8为了加快对某表查询的速度，应对此表建立（）。<br>     A.)约束 B)存储过程 C) 规则 D) 索引</p>\n" +
                "<p>9 在HTML页面上编写Javascript代码时，应编写在（）标签中间。<br>   A)<javascript>和</javascript><br>   B)<script>和</script><br>   C) <head>和</head><br>   D) <body </body></p>\n" +
                "<p>10 以下（）选项不能够正确地得到这个标签：</p>\n" +
                "<p><input id=\"btnGo\" type=\"button\" value=\"单击我\" class=\"btn\"/><br>A)  $(&quot;#btnGo&quot;)<br>B)   $(&quot;.btnGo&quot;)<br>C)  $(&quot;.btn&quot;)<br>    D)  $(&quot;input[type=&#39;button&#39;]&quot;)</p>\n" +
                "<p>统一写出选择题答案：</p>\n" +
                "<p>二 问答题<br>-简述Java中接口和抽象类的区别</p>\n" +
                "<p>-简述java的集合对象list，Map,Set, Queue的特点</p>\n" +
                "<p>-用一句话简述下列框架的作用<br>Spring:<br>springmvc（struts2):<br>Hibernate（mybatis):<br>Jquery:<br>Bootstrap：</p>\n" +
                "<p>-简述Javascript中的对象是什么，怎么创建一个对象的实例（简单代码）</p>\n" +
                "<p>-简述下列sql关键字的作用<br>Select * from Table:<br>Where :<br>Having:<br>Group by:<br>Order by:</p>\n" +
                "<p>三 知识面题<br>写出你学习过或者了解过的相关扩展知识。<br>-学习或者了解过的其他编程语言：</p>\n" +
                "<p>-学习或者了解过java其他框架(除去SSH)：</p>\n" +
                "<p>-学习或者了解过相关数据库：</p>\n" +
                "<p>-游览过或者知道的任何技术网站:</p>\n" +
                "<p>-其他你知道的新奇技术：</p>\n" +
                "<hr>\n");

        final String html = parse.html();
        System.out.println(html);
        Assert.assertTrue(html.contains("<body < body>")); // Jsoup bug
    }

    /**
     * Tag test.
     */
    @Test
    public void tag() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String kbd = "<kbd>DV</kbd>";

            String html = Markdowns.toHTML(kbd);
            html = Markdowns.clean(html, "");

            Assert.assertTrue(html.contains("<kbd>") && html.contains("</kbd>"));
        }
    }

    /**
     * XSS test.
     */
    @Test
    public void clean() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String md = "<a href='data:text/html;base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K'>a link</a>";

            final String html = Markdowns.toHTML(md);
            final String securedHTML = Markdowns.clean(html, "");

            Assert.assertFalse(securedHTML.contains("href"));
        }
    }

    /**
     * Auto space test.
     */
    @Test
    public void space() {
        if (Markdowns.MARKED_AVAILABLE) {
            final String md = "Sym是一个用Java写的实时论坛，欢迎来[体验Sym](https://hacpai.com)！";
            final String html = Markdowns.toHTML(md);

            Assert.assertEquals(html, "<p>Sym 是一个用 Java 写的实时论坛，欢迎来<a href=\"https://hacpai.com\">体验 Sym</a>！</p>");
        }
    }

    /**
     * email test not link to User page if the name is in email address
     */
    @Test
    public void linkToHTML() {
        if (Markdowns.MARKED_AVAILABLE) {
            String md = "test@test.com";
            String html = Markdowns.toHTML(md);

            Assert.assertEquals(html, "<p>test@test.com</p>");
        }
    }

    /**
     * Workaround for https://github.com/sirthias/pegdown/issues/229.
     */
    @Test
    public void toHTML1() {
        if (Markdowns.MARKED_AVAILABLE) {
            String md = "Sym**是一个用 _Java_ 写的实时论坛**";
            String html = Markdowns.toHTML(md);
            Assert.assertEquals(html, "<p>Sym<strong>是一个用 <em>Java</em> 写的实时论坛</strong></p>");

            md = "[link](https://github.com/b3log/symphony/blob/master/README_zh_CN.md)";
            html = Markdowns.toHTML(md);
            Assert.assertEquals(html, "<p><a href=\"https://github.com/b3log/symphony/blob/master/README_zh_CN.md\">link</a></p>");

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
            Assert.assertEquals(html, "<p></p><pre><code>server {\n"
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
                    + "</code></pre><p></p>");

            md = "然后新建一个study_1文件夹，在文件夹下面新建一个index.html文件,*注意最后一个js代码的type*\n"
                    + "github地址：https://github.com/Userwu/study_react";
            html = Markdowns.toHTML(md);
            Assert.assertEquals(html, "<p>然后新建一个 study_1 文件夹，在文件夹下面新建一个 index.html 文件,<em>注意最后一个 js 代码的 type</em><br>github 地址：<a href=\"https://github.com/Userwu/study_react\">https://github.com/Userwu/study_react</a></p>");
        }
    }

    /**
     * Link test.
     */
    @Test
    public void toHTML() {
        if (Markdowns.MARKED_AVAILABLE) {
            String md = "[b3log](https://b3log.org)";
            String html = Markdowns.toHTML(md);
            Assert.assertTrue(html.contains("href"));

            md = "[b3log](b3log.org)";
            html = Markdowns.toHTML(md);
            Assert.assertTrue(html.contains("href"));
        }
    }

    /**
     * Standard syntax test.
     *
     * @throws java.lang.Exception exception
     */
    @Test
    public void toHtml0() throws Exception {
        if (Markdowns.MARKED_AVAILABLE) {
            final URL mdResource = MarkdownsTestCase.class.getResource("/markdown_syntax.text");
            final String md = IOUtils.toString(new FileReader(mdResource.getPath()));
            final String html = Markdowns.toHTML(md);

            //System.out.println(html);
        }
    }
}
