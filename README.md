# [Symphony](https://github.com/b3log/symphony) [![Build Status](https://img.shields.io/travis/b3log/symphony.svg?style=flat)](https://travis-ci.org/b3log/symphony)

## 简介

[Sym](https://github.com/b3log/symphony) 是一个用 Java 写的现代化的社区论坛，欢迎来**[体验](https://hacpai.com/register)**！（如果你需要搭建一个企业内网论坛，请使用 [SymX](https://github.com/FangStarNet/symphonyx)）

* 非常详细的 [Sym 功能点脑图](http://naotu.baidu.com/file/cd31354ac9abc047569c73c560a5a913?token=b9750ae13f39ef9a)
* 如果你在搭建或者二次开发时碰到问题，欢迎加 Q 群 17370164 进行讨论

Sym 的诞生是有如下几点原因：

（正版）

* 好用的 Java 开源论坛系统难以寻找
* 很多系统界面上仍然保持着老式风格，远远没有跟上前端发展的脚步
* 很多系统没有创新、好玩的特性，缺少现代化的用户体验
* 我们正在探索新的论坛模式，实现独奏（[Solo](https://github.com/b3log/solo)）与协奏（[Symphony](https://github.com/b3log/symphony)）相结合的[社区新体验](https://hacpai.com/b3log)

（野版）

* 做最 NB 的开源论坛系统
* 作者技痒，炫技之作

## 基本理念

### 实时交互

在浏览帖子时，传统论坛都是需要刷新页面来查看回帖的，而 Sym 则是基于 WebSocket 技术进行回帖推送，看帖时不需要刷新页面也可以看到其他人回帖。

### 互联

Sym 提供了 [API](https://hacpai.com/article/1457158841475) 进行帖子、回帖的同步（[B3log 构思](https://hacpai.com/b3log)），目前 [Solo](https://github.com/b3log/solo)、[Typecho](https://github.com/DT27/B3logForHacPai)、[Z-BlogPHP](https://github.com/zblogapp/hacpai)、[WordPress](https://github.com/zh-h/hacpai-sync-wordpress) 均已经提供插件来进行内容同步，欢迎大家进行接入！

### HTML5

Sym 使用了很多 HTML5 提供的技术特性，比如

* 通过使用本地存储防止编辑帖子/回帖时内容丢失
* 使用了音频特性来进行帖子/回帖音频录制、播放
* 复制/粘贴上传图片
* CSS3 动画

通过使用这些技术，Sym 可以让用户在分享、交流时更加便捷、舒服 

## 安装

需求：Maven3+、MySQL5.5+、Jetty9+/Tomcat9+

1. [下载](https://github.com/b3log/symphony/archive/master.zip)源码
2. 解压后修改 `src/main/resources/local.properties` 中的数据库配置，并创建数据库
3. _可能需要_修改 `latke.properties` 中的端口为容器端口
4. _可能需要_修改 `init.properties` 中的管理员账号
3. 使用 `mvn install` 进行构建
4. 将构建好的 war 包部署到容器中，数据库表会在第一次启动时自动建立

注意：

* 没有数据库建表 SQL 脚本，手动建库后，表会在第一次启动时自动生成
* 生产环境建议使用反向代理，并需要配置好 WebSocket 代理
* Tomcat 用 9 以上版本，最好是使用最新版本
* 参考 [Latke 配置剖析](https://hacpai.com/article/1474087427032)

## 配置

* 图片上传默认是上传服务器本地，要使用[七牛](https://portal.qiniu.com/signup?code=3lewbghpvrqky)可配置 `symphony.properties` 中的 `qiniu.*` 属性
* 将 WEB-INF/cron.xml 中注释掉的部分打开
* 邮件发送使用的是 [SendCloud](http://sendcloud.sohu.com)，需要配置 `symphony.properties` 中的 `sendcloud.*` 属性
* 用户注册时需要验证邮箱的，所以必须先配置好 SendCloud

如果遇到问题，可以参考一下这篇[帖子](https://hacpai.com/article/1468824093225)。

## 案例

* [黑客派](https://hacpai.com)
* [宽客网](http://www.cnq.net)
* [贵州IT](http://www.gzit.info)
* [超级产品经理](https://imspm.com)
* [Titandb 学习主站](https://titandb.cn)

如果你也搭建好了，欢迎通过 Pull Request 将你的站点加到这个列表中 :-p

## 商用授权

如果需要将 Sym 用于商用（比如公司搭建对外社区），则必须付费，报价 ￥4000，请联系我（Q845765）进行细节咨询。

## 开源授权

请仔细查看并遵循[使用条款](https://github.com/b3log/symphony#terms)，尊重我们的劳动成果。

商用授权和开源授权在功能上没有任何区别，但商用授权后可以去除页脚版权部分。如果在未获得商用授权前私自去除版权部分，必将追究法律责任。

## 感悟

在实现 [B3log 构思](https://hacpai.com/b3log)的这几年：

* 我们见证了 xAE（GAE/BAE/SAE/etc）的兴起与没落。2009 年选择了 GAE 作为服务器，并开始实现 [Latke](https://github.com/b3log/latke) 框架来解决跨云平台，直到[告别 GAE](https://hacpai.com/article/1443685401909)，不得不感叹技术更迭之快
* 感受到了自造轮子的优缺点，并且可以肯定一点：对于一个想要长久的产品来说，自制技术框架优势远大于劣势
* 一个好玩的产品或说是细节特性然并卵，需要做的是一个能够持续提供用户价值的产品/特性
* 虽然直到目前 B3log 系产品用户不多，但我们已经初步证明了：Java 用来实现博客、论坛没有什么不好的
* 使用开源软件，了解开源思想，融入开源
* [如果你想做个程序员相关的论坛，请三思](https://hacpai.com/article/1471007706462)
* [你怎么看待社群、社区这两个词？](https://hacpai.com/article/1465652829809)
* [UGC 社区价值生态](https://hacpai.com/article/1462028669762)

## 贡献

Sym 的主要作者是 [Daniel](https://github.com/88250) 与 [Vanessa](https://github.com/Vanessa219)，所有贡献者可以在[这里](https://github.com/b3log/symphony/graphs/contributors)看到。

我们非常期待你加入到这个项目中，无论是使用反馈还是代码补丁，都是对 Sym 一份满满的爱 :heart:

## Terms

* This software is open sourced under the Apache License 2.0 
* You can not get rid of the "Powered by [B3log 开源](http://b3log.org) • [Sym](https://github.com/b3log/symphony)" from any page, even which you made
* If you want to use this software for commercial purpose, please mail to support@liuyun.io for a commercial license request
* Copyright &copy; b3log.org, all rights reserved

## 鸣谢

Sym 的诞生离不开以下开源项目：

* [jQuery](https://github.com/jquery/jquery)：前端 JavaScript 工具库
* [CodeMirror](https://github.com/codemirror/CodeMirror)：前端 Markdown 编辑器内核
* [Highlight.js](https://github.com/isagalaev/highlight.js)：前端代码高亮库
* [emojify.js](https://github.com/Ranks/emojify.js)：前端 Emoji 处理库
* [APlayer](https://github.com/DIYgod/APlayer)：前端 HTML5 音乐播放器
* [ECharts](https://github.com/ecomfe/echarts)：前端 JavaScript 交互式图表库
* [MathJax](https://github.com/mathjax/MathJax)：前端数学公式渲染引擎
* [SoundRecorder](https://github.com/rderveloy/JavaScript-Sound-Recorder)：前端 HTML5 录音库
* [ZeroClipboard](https://github.com/zeroclipboard/zeroclipboard)：前端剪贴板支持
* [JavaScript MD5](http://pajhome.org.uk/crypt/md5/index.html)：前端 JavaScript MD5 库
* [ReconnectingWebSocket](https://github.com/joewalnes/reconnecting-websocket)：前端 WebSocket 重连库
* [to-markdown](https://github.com/domchristie/to-markdown)：前端 HTML 转换 Markdown
* [UAParser.js](https://github.com/faisalman/ua-parser-js)：前端 User-Agent 解析库
* [Sass](http://sass-lang.com)：前端 CSS 处理工具
* [jsoup](https://github.com/jhy/jsoup)：Java HTML 解析器
* [pegdown](https://github.com/sirthias/pegdown)：Java Markdown 处理库
* [Apache Commons](http://commons.apache.org)：Java 工具库集
* [Jodd](https://github.com/oblac/jodd)：Java 工具库集
* [emoji-java](https://github.com/vdurmont/emoji-java)：Java Emoji 处理库
* [User-Agent-Utils](https://github.com/HaraldWalker/user-agent-utils)：Java User-Agent 解析库
* [Druid](https://github.com/alibaba/druid)：Java 数据库连接池
* [FreeMarker](http://freemarker.org)：好用的 Java 模版引擎
* [Latke](https://github.com/b3log/latke)：Java Web 框架 
* [NetBeans](https://netbeans.org)：全宇宙暂时排名第三的 IDE

----

## 功能图解

**首页**

![index](https://cloud.githubusercontent.com/assets/873584/19502589/a71b28ca-95e0-11e6-82da-2e2585f912d4.png)

**帖子**

![article](https://cloud.githubusercontent.com/assets/873584/19502600/b9a92172-95e0-11e6-9a39-402c08b87e6f.png)

（右边红色回帖按钮的位置应该在右下角，截图软件不给力..）

**个人设置**

![settings](https://cloud.githubusercontent.com/assets/873584/19502605/c04f1e46-95e0-11e6-980f-a927bd774dc3.png)

**发布编辑**

* Markdown 编辑器，支持 GFM 语法
* LaTeX 数学公式
* 复制粘贴时自动转换为 Markdown 
* Chrome 下可以直接粘贴图片，其他浏览器支持拖拽
* 除了使用文字，也可以在帖子内进行录音
* 支持 Emoji
* 使用本地存储保障数据在未提交时不丢

![post](https://cloud.githubusercontent.com/assets/873584/19502607/c290209c-95e0-11e6-8fca-484cdef94bd6.png)

### 移动端

移动端使用单独的模版进行渲染，解决通过一套模版自适应不能达成的效果和体验。

![mobile](https://cloud.githubusercontent.com/assets/873584/19502609/c4b25502-95e0-11e6-9d6a-1ea18bfebf6f.png)
