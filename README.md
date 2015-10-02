# Symphony [![Build Status](https://img.shields.io/travis/b3log/symphony.svg?style=flat)](https://travis-ci.org/b3log/symphony)

## 简介

Sym 是一个用 Java 写的实时社区。

* 原始创意请看[这里](http://hacpai.com/b3log)
* 细节特性请看[这里](http://hacpai.com/article/1440573175609)

> 黑客派是使用 Sym 搭建的，欢迎[**加入**](http://hacpai.com/register?r=Vanessa)！

## 安装

需求：Maven3+、MySQL5+、Jetty9+

1. [下载](https://github.com/b3log/symphony/archive/master.zip)源码
2. 解压后修改 `src/main/resources/local.properties` 中的数据库配置，并创建数据库
3. _可能需要_修改 `latke.properties` 中的端口为容器端口
4. _可能需要_修改 `init.properties` 中的管理员账号
3. 使用 `mvn install` 进行构建
4. 部署到 Servlet 容器的 ROOT 下并启动容器，数据库表会在第一次启动时自动建立

注意：

* 只能部署到 ROOT 中，线上环境建议使用反向代理

## 配置

* 图片上传使用的是[七牛](http://www.qiniu.com)，需要配置 `symphony.properties` 中的 `qiniu.*` 属性
* 邮件发送使用的是 [SendCloud](http://sendcloud.sohu.com)，需要配置 `symphony.properties` 中的 `sendcloud.*` 属性

用户注册时是启用邮件验证的，如果需要修改请参考相关代码。

## 商用授权

请看[这里](https://github.com/b3log/symphony/wiki/%E5%95%86%E7%94%A8%E6%8E%88%E6%9D%83)。

## Terms

* This software is open sourced under the Apache License 2.0
* You can not get rid of the "Powered by [B3log](http://b3log.org)" from any page, even which you made
* If you want to use this software for commercial purpose, please mail to support@liuyun.io for a commercial license request
* Copyright &copy; b3log.org, all rights reserved
