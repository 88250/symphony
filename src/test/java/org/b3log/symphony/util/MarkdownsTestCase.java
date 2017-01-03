/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
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
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 2.3.1.5, Nov 27, 2016
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
        if (!Markdowns.MARKED_AVAILABLE) {
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
        if (!Markdowns.MARKED_AVAILABLE) {
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
        if (!Markdowns.MARKED_AVAILABLE) {
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
        if (!Markdowns.MARKED_AVAILABLE) {
            String md = "test@test.com";
            String html = Markdowns.linkToHtml(md);

            Assert.assertEquals(html, "<p><a href=\"mailto:&#116;e&#115;&#x74;&#x40;&#x74;&#x65;&#115;&#x74;&#x2e;c&#111;&#x6d;\">&#116;e&#115;&#x74;&#x40;&#x74;&#x65;&#115;&#x74;&#x2e;c&#111;&#x6d;</a></p>");
        }
    }

    /**
     * Workaround for https://github.com/sirthias/pegdown/issues/229.
     */
    @Test
    public void toHTML1() {
        if (!Markdowns.MARKED_AVAILABLE) {
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

            md = "然后新建一个study[downline]1文件夹，在文件夹下面新建一个index.html文件,*注意最后一个js代码的type*\n"
                    + "github地址：https://github.com/Userwu/study[downline]react";
            html = Markdowns.toHTML(md);
            Assert.assertEquals(html, "<p>然后新建一个 study_1 文件夹，在文件夹下面新建一个 index.html 文件,*注意最后一个 js 代码的 type*<br/>github 地址：https://github.com/Userwu/study_react</p>");
        }
    }

    /**
     * Link test.
     */
    @Test
    public void toHTML() {
        if (!Markdowns.MARKED_AVAILABLE) {
            String md = "[b3log](http://b3log.org)";
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
        if (!Markdowns.MARKED_AVAILABLE) {
            final URL mdResource = MarkdownsTestCase.class.getResource("/markdown_syntax.text");
            final String md = IOUtils.toString(new FileReader(mdResource.getPath()));
            final String html = Markdowns.toHTML(md);

            //System.out.println(html);
        }
    }

    @Test
    public void timeout() throws Exception {
        if (!Markdowns.MARKED_AVAILABLE) {
            final String md = "win8和win8.1以及win10自带虚拟机，无需再装第三方虚拟机软件。\n"
                    + "首先需要在“启用或关闭windows功能”中，启用Hyper-V\n"
                    + "\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402ua2qsv4foss.png\" >\n"
                    + "\n"
                    + "\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402leky0gnpyza.png\" >\n"
                    + "\n"
                    + "\n"
                    + "启用之后，需要重启计算机才可以继续进行下面的步骤。\n"
                    + "\n"
                    + "<h2>方法/步骤</h2>\n"
                    + "<ol>\n"
                    + "<li>首先打开虚拟主机软件如图，然后新建一个虚拟主机；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402jwahqmr1rg1.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>根据步骤输入一个名称，方便日后管理区分；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402jmajjtwyvoi.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>选择第一代技术，不知道为什么安装linux系统选择第二个总是有问题；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402rkbfp51zxcj.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>控制内存大小不要超过自己的本机大小，根据装的系统来判断，本机16G内存，这里分配3G给Linux；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402t0mx3km3bv4.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>网络可以先不连接，之后再配置；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402u0cakutnz0v.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>创建一个虚拟硬盘，这里要注意尽量不要选择C盘，因为分区装系统后是要占用空间的；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理20160704021wpoivvwli3.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>在虚拟光驱这里需要选择加载光驱操作，如图。也可以选择“以后安装操作系统”，稍后会加以说明如何在空的虚拟机中安装系统；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402foh1yrmfxua.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>如果已经下载好了镜像，可以选择要安装的镜像，一般来说都是iso结尾的文件；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理20160704025fxnln0ad25.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>这样我们就建好了一个虚拟机需要完成保存操作。这里是以后要安装的系统的截图；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402mca11kqggac.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>从操作系统那一项可以判断是以后安装还是已经选择了系统镜像；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402ofdupqzsj0o.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>当我们回到列表就可以看到我们之前建设过的虚拟主机了，刚才建设的也在里面；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402yhhpdytkdpe.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>右键连接并开启主机后我们就能进入系统的安装界面了。后面的系统安装跟实体机无多大区别，这里只说一下之前选择“以后安装系统”的现在该如何安装系统。点击“媒体”-“DVD驱动器”-“插入磁盘”-选择要安装系统镜像（*.iso）- 点击虚拟机的黑色界面-回车。之后就可以进入系统安装界面了；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402fqdwoyuzo1o.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>系统安装完成之后就可以配置网络连接了。在Hyper-V管理器界面选择“虚拟交换机管理器”；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402hqmanm2xa41.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>选择创建外部虚拟机；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402zydduridgf4.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>给网络起个名称，其他选择默认即可；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理20160704025gxsrnlpf4k.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>出现弹出提示，选择“是”；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402nzfe4lzxhpy.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>回到Hyper-V管理器界面，对需要联网的虚拟机系统进行设置；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402zphsuihbr3q.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>选择创建好的虚拟网络交换机；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402fqj54nbeful.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "<li>打开安装好系统的虚拟机，开启网络连接即可；\n"
                    + "\n"
                    + "<img src=\"https://img.imspm.com/imspm.com超级产品经理2016070402kjxqg0tdfrh.png\" >\n"
                    + "\n"
                    + "</li>\n"
                    + "</ol>\n"
                    + "<h2>可以参考的资料</h2>\n"
                    + "<ul>\n"
                    + "<li><a href=\"http://jingyan.baidu.com/article/9c69d48f46ae6e13c9024eba.html\">Hyper-V如何安装</a>\n"
                    + "</li>\n"
                    + "<li><a href=\"http://baike.baidu.com%20/link?url=NYJX2s7VqK2H5H5U2vX6LmXw11ab1-2I8hG7Kk5TrfScGD-zSQnOPebhzmqc_FtX65DQDeP7nJppuzOuwJGbMa\">更多关于 hyper-v 百度百科</a>\n"
                    + "</li>\n"
                    + "</ul>";

            final String html = Markdowns.toHTML(md);

            Assert.assertEquals(html, "Content render failed, please <a href=\"https://hacpai.com/article/1438049659432\">report</a> this problem to help us enhance it, thank you &hearts;");
        }
    }
}
