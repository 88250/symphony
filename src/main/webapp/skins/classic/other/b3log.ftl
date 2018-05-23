<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "../macro-head.ftl">
    <!DOCTYPE html>
    <html>
    <head>
        <@head title="B3log 构思 - ${symphonyLabel}" />
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
        <link rel="canonical" href="${servePath}/b3log">
    </head>
    <body>
    <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head">B3log 构思</h2>
                        <div class="fn-content content-reset">
                            <i class="ft-gray">一个正在逐渐清晰、实践的创意</i>
                            <h3>背景故事</h3>
                            <p>
                                美妙而动听的音乐可能是一个人的独奏，也可以是一种很有感觉的节奏，亦或者是一场宏伟的交响乐。B3log 将以不同的方式为你呈现出同样优美的旋律。
                            </p>
                            <p>
                                悦耳的音乐至少有三部分构成：不同乐器的演奏、统一的节奏配合，最终编曲合成。B3log 系列服务基于此观念，诞生了三个产品：
                            </p>
                            <ul>
                                <li>
                                    Solo 是<a href="http://www.iciba.com/solo">独奏</a>，
                                    <a href="https://github.com/b3log/solo">Solo</a>
                                    也是一个独立的个人博客系统，在音乐中相当于乐手各自所拿的乐器，使用它来演奏不同的声音。
                                </li>
                                <li>
                                    Rhythm 是<a href="http://www.iciba.com/rhythm">节奏</a>，相当于音乐中节拍器控制的节奏。
                                    <a href="https://github.com/b3log/rhythm">Rhythm</a>
                                    用来处理不同博客和社区服务之间的同步，以避免在交响乐中乱了节奏。
                                </li>
                                <li>
                                    Symphony 是<a href="http://www.iciba.com/symphony">交响乐</a>。
                                    <a href="https://github.com/b3log/symphony">Symphony</a>
                                    以论坛的方式交互，汇总个人博客，需要很多人协同参与才能奏出美妙的乐章。
                                </li>
                            </ul>
                            <p>
                                B3log 名字
                                <a href="http://88250.b3log.org/articles/2009/12/09/1260370800000.html">来源于</a>
                                “Bulletin Board Blog”缩写，目标是将个人<i>独立博客</i>与论坛结合，形成一种<i>新的网络社区体验</i>。
                            </p>
                            <h3>独立博客</h3>
                            <p>
                                博客是一个表达情感，分享经验、生活的地方，最重要的就是能够随心记录点点滴滴。
                            </p>
                            <p>
                                关于选择独立博客还是选择个人博客服务的讨论<a
                                    href="https://www.google.com.hk/search?sugexp=chrome,mod=15&amp;sourceid=chrome&amp;ie=UTF-8&amp;q=%E4%B8%BA%E4%BB%80%E4%B9%88+%E7%8B%AC%E7%AB%8B%E5%8D%9A%E5%AE%A2">比比皆是</a>。究其根本，如果有廉价、稳定的空间搭建独立博客，且安装部署简单、后续维护简洁、使用过程方便，那博客服务就完全没有优势了。
                            </p>
                            <p>
                                随着云环境的普及（或者说成是泛滥）、廉价、稳定。易管理的空间已经有很多，各大互联网公司 xAE 的运营也日趋成熟。
                                搭建一个廉价、甚至是免费的独立博客已经不再是只有&ldquo;高手&rdquo;才能玩的了，我们只需按步就班即可。
                            </p>
                            <p>
                                使用博客服务就没有那么简单了，写文章时得注意内容关键字；稍微敏感的文章会被删除；
                                国家法定节假日不让发表文章；说维护就停止服务；说封杀就封杀；想修改下界面、调换下位置，简直就弱爆了....
                                因为博主的理由都不是理由，注册服务的时候已经告知过了。
                            </p>
                            <p>
                                离开博客服务提供商自己玩吧。不过自己玩会有点无趣（独乐乐不如众乐乐），再加上没人理没人踩那就更不乐了。
                                但如果能把多数单飞的人聚在一起，这将会变得非常有趣！
                            </p>
                            <h3>新的社区体验</h3>
                            <img src="${staticServePath}/images/about/about.png" width="458" height="199"/>
                            <p>
                                这是一个非常简单的内容聚合平台。仅文章同步而言，没有什么新鲜的，其新意体现在文章/评论的同时同步上。
                            </p>
                            <p>
                                博客中的文章评论会同步到社区中，社区帖子的回复同样也会同步到博客的文章中。在加强用户之间互动的同时也不失独立博客的自由。
                            </p>
                            <p>
                                社区的内容接口是开放的，这意味着将来不仅是 Solo 可以调用，其他类型的客户端也是可以通过这组开放接口完成用户交互，共享数据。
                            </p>
                            <p>
                                最终，我们想要实现 B3log 永恒的价值观&mdash;&mdash;平等、自由、奔放。
                            </p>
                            <h3>Solo</h3>
                            <p>
                                从功能上看，Solo 是一个开源的 Java 博客系统，是一个轻量、简洁的写作环境。
                            </p>
                            <p>
                                目前支持部署在大多数标准 Servlet 容器内，比如 Tomcat、Jetty。也可以使用独立模式启动，该模式不需要额外安装 MySQL，并且内嵌了容器。
                            </p>
                            <p>
                                从架构上看，Solo 是 B3log 社区的重要客户端，但除了 Solo 外，目前也以插件的方支持了一些主流博客系统，请看<a
                                    href="https://hacpai.com/article/1457158841475">这里</a>。
                            </p>
                            <h3>Rhythm</h3>
                            <p>
                                Rhythm 是社区服务器，主要提供同步文章、保存用户信息、文章索引/分类/过滤等功能。
                                控制着从各客户端同步文章到社区的节奏，是社区的关键前置服务。
                            </p>
                            <p>
                                该服务对于 Solo 客服端用户来说是透明的，Rhythm 会提供一组 HTTP 接口供第三方客户端进行使用。
                            </p>
                            <h3>Symphony</h3>
                            <p>
                                这是 B3log 社区项目的代号，也是提供交互聚合的平台，体验方面的主要特色是实时交互。
                            </p>
                            <h3>计划中的特性</h3>
                            <h4 class="ft-gray">Tag Forge</h4>
                            <ul>
                                <li>每个用户可以创建自己的标签，形成个人标签图</li>
                                <li>用户的个人标签图将自动提交（Push）到社区，根据合并规则（重复关联计数、默认关联等）形成社区标签图</li>
                                <li>用户可以更新（Pull）社区标签图，选择自己需要的标签或关联合并到自己的标签图中</li>
                            </ul>
                            <p>
                                这个特性方便用户进行知识管理，体验社区协作与分享。
                            </p>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
    </html>