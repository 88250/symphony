# [Symphony](https://github.com/b3log/symphony) [![Build Status](https://img.shields.io/travis/b3log/symphony.svg?style=flat)](https://travis-ci.org/b3log/symphony)

## 简介

[Sym](https://github.com/b3log/symphony) 是一个用 Java 写的实时论坛，欢迎来**[体验](https://hacpai.com/register)**！（如果你需要搭建一个企业内网论坛，请使用 [SymX](https://github.com/FangStarNet/symphonyx)）

* [这里](http://naotu.baidu.com/file/cd31354ac9abc047569c73c560a5a913?token=b9750ae13f39ef9a)是 Sym 功能点脑图，看看是否满足你的需求
* 如果你在搭建或者二次开发时碰到问题，欢迎加 Q 群 17370164 进行讨论

### 作者

Sym 的主要作者是 [Daniel](https://github.com/88250) 与 [Vanessa](https://github.com/Vanessa219)，所有贡献者可以在[这里](https://github.com/b3log/symphony/graphs/contributors)看到。

### 初衷

Sym 的诞生是有如下几点原因：

* 已有的用 Java 写的论坛很少也很丑，并且大多数已经不再维护
* 我们想实现一种[新的网络社区体验](https://hacpai.com/b3log)，独立博客+论坛互动

## 基本理念

### 实时交互

在浏览帖子时，传统论坛都是需要刷新页面来查看回帖的，而 Sym 则是基于 WebSocket 技术进行回帖推送，看帖时不需要刷新页面也可以看到其他人回帖。

### 互联

Sym 提供了 [API](https://hacpai.com/article/1457158841475) 进行帖子、评论的同步（[B3log 构思](https://hacpai.com/b3log)），目前 [Solo](https://github.com/b3log/solo)、[Typecho](https://github.com/DT27/B3logForHacPai)、[Z-BlogPHP](https://github.com/zblogapp/hacpai)、[WordPress](https://github.com/zh-h/hacpai-sync-wordpress) 均已经提供插件来进行内容同步，欢迎大家进行接入！

### HTML5

Sym 使用了很多 HTML5 提供的技术特性，比如

* 通过使用本地存储防止编辑帖子/评论时内容丢失
* 使用了音频特性来进行帖子/评论音频录制、播放
* 复制/粘贴上传图片
* CSS3 动画

通过使用这些技术，Sym 可以让用户在分享、交流时更加便捷、舒服 

## 功能图解

**首页**

![首页](https://cloud.githubusercontent.com/assets/873584/17216774/8ad7840e-5514-11e6-9e0a-aa05693bfd49.png)

**帖子**

![帖子](https://cloud.githubusercontent.com/assets/873584/17220386/927f5512-5521-11e6-9045-bd35e627ccd2.png)

**个人设置**

![个人设置](https://cloud.githubusercontent.com/assets/873584/17216836/d845db64-5514-11e6-88fa-19c0b2c7bf6a.png)

**发布编辑**

* Markdown 编辑器，支持 GFM 语法
* LaTeX 数学公式
* 复制粘贴时自动转换为 Markdown 
* Chrome 下可以直接粘贴图片，其他浏览器支持拖拽
* 除了使用文字，也可以在帖子内进行录音
* 支持 Emoji
* 使用本地存储保障数据在未提交时不丢

![发布编辑](https://cloud.githubusercontent.com/assets/873584/17216839/db3a18e4-5514-11e6-9cca-190e04a750fd.png)

### 移动端

移动端使用单独的模版进行渲染，解决通过一套模版自适应不能达成的效果和体验。

## 安装

需求：Maven3+、MySQL5+、Jetty9+/Tomcat9+（理论上只要实现了 [JSR356](https://jcp.org/en/jsr/detail?id=356) 规范的 Servlet 容器都可以）

1. [下载](https://github.com/b3log/symphony/archive/master.zip)源码
2. 解压后修改 `src/main/resources/local.properties` 中的数据库配置，并创建数据库
3. _可能需要_修改 `latke.properties` 中的端口为容器端口
4. _可能需要_修改 `init.properties` 中的管理员账号
3. 使用 `mvn install` 进行构建
4. 部署到 Servlet 容器的 ROOT 下并启动容器，数据库表会在第一次启动时自动建立

注意：

* 没有数据库建表 SQL 脚本，只需要手动建库，表会在第一次启动时自动生成
* 只能部署到 ROOT 中，线上环境建议使用反向代理
* Tomcat 用 9 以上版本，最好是使用最新版

## 配置

* 图片上传默认是上传服务器本地，要使用[七牛](http://www.qiniu.com)可配置 `symphony.properties` 中的 `qiniu.*` 属性
* 邮件发送使用的是 [SendCloud](http://sendcloud.sohu.com)，需要配置 `symphony.properties` 中的 `sendcloud.*` 属性
* 将 WEB-INF/cron.xml 中注释掉的部分打开
* 如果部署在***非*** Jetty 容器中，需要把 WEB-INF/web.xml 中如下配置注释掉：
```xml
  <servlet>
      <servlet-name>default</servlet-name>
      <servlet-class>org.eclipse.jetty.servlet.DefaultServlet</servlet-class>
      <init-param>
          <param-name>useFileMappedBuffer</param-name>
          <param-value>false</param-value>
      </init-param>
  </servlet>
```

用户注册时是启用邮件验证的，如果需要修改请参考相关代码。

注意：

本地配置完成部署后，还需要对七牛（如果使用了）和 SendCloud 进行服务配置：

* 配置_两个_七牛图片数据处理样式：
  ![qiniu](https://cloud.githubusercontent.com/assets/873584/10298674/3c7230aa-6c14-11e5-9014-2ae4e457a364.png)
* 配置 SendCloud 邮件模版：
  ![sendcloud](https://cloud.githubusercontent.com/assets/873584/10298675/3cb11b08-6c14-11e5-9fd4-025122336469.png)

如果遇到问题，可以参考一下这篇[帖子](https://hacpai.com/article/1468824093225)。

## 商用授权

请看[这里](https://github.com/b3log/symphony/wiki/%E5%95%86%E7%94%A8%E6%8E%88%E6%9D%83)。

## 思绪

在实现 [B3log 构思](https://hacpai.com/b3log)的这几年：

* 我们见证了 xAE（GAE/BAE/SAE/etc）的兴起与没落。2009 年选择了 GAE 作为服务器，并开始实现 [Latke](https://github.com/b3log/latke) 框架来解决跨云平台，直到[告别 GAE](https://hacpai.com/article/1443685401909)，不得不感叹技术更迭之快
* 感受到了自造轮子的优缺点，并且可以肯定一点：对于一个想要长久的产品来说，自制技术框架优势远大于劣势
* 一个好玩的产品或说是细节特性然并卵，需要做的是一个能够持续提供用户价值的产品/特性
* 虽然直到目前 B3log 系产品用户不多，但我们已经初步证明了：Java 用来实现博客、论坛没有什么不好的
* 使用开源软件，了解开源思想，融入开源

## Terms

* This software is open sourced under the Apache License 2.0 
* You can not get rid of the "Powered by [B3log 开源](http://b3log.org) • [Sym](https://github.com/b3log/symphony)" from any page, even which you made
* If you want to use this software for commercial purpose, please mail to support@liuyun.io for a commercial license request
* Copyright &copy; b3log.org, all rights reserved

