<img src="https://cloud.githubusercontent.com/assets/873584/19897669/e6a6f5ce-a093-11e6-8cf3-8e5c2acea033.png">  

<p align = "center">
<a href="https://github.com/b3log/symphony/blob/master/README.md"><strong>English</strong></a> | <a href="https://github.com/b3log/symphony/blob/master/README_zh_CN.md"><strong>中文</strong></a>
</p>

* [Introduction](#introduction)
  * [Motivation](#motivation)
  * [Usecases](#usecases)
* [Features](#features)
  * [Good editors](#good-editors)
  * [Intelligent and flexible information architecture](#intelligent-and-flexible-information-architecture)
  * [To meet the diverse needs of post](#to-meet-the-diverse-needs-of-post)
  * [Humanized interactive replies](#humanized-interactive-replies)
  * [User personalization](#user-personalization)
  * [Edit history and anonymous posting](#edit-history-and-anonymous-posting)
  * [Search engine friendly](#search-engine-friendly)
  * [Real\-time message notification](#real-time-message-notification)
  * [Fun activities](#fun-activities)
  * [Powered management background](#powered-management-background)
  * [Open content API](#open-content-api)
  * [Integrated cloud mail service](#integrated-cloud-mail-service)
  * [Integrated cloud search service](#integrated-cloud-search-service)
* [Roadmap](#roadmap)
  * [trust system](#trust-system)
  * [badge system](#badge-system)
* [Installation](#installation)
  * [Requirements](#requirements)
  * [Steps](#steps)
  * [Configuration](#configuration)
* [License](#license)
  * [Open source license](#open-source-license)
  * [Commercial license](#commercial-license)
* [Contribution](#contribution)
  * [Authors](#authors)
  * [Discussion area](#discussion-area)
* [Feeling](#feeling)
* [Credits](#credits)

[![activities](https://graphs.waffle.io/b3log/symphony/throughput.svg)](https://waffle.io/b3log/symphony/metrics/throughput)

## Introduction

[Symphony](https://github.com/b3log/symphony) is a modern community platform, because it:

* implements a content oriented discussion forum
* includes a social network for users to share, make friends, and play games
* the ability to aggregate independent bloggers, to build and share quality resources
* and `100%` open source

Welcome to Sym official community - [HacPai](https://hacpai.com) for more details.

### Motivation

Sym was born with the following reasons:

(genuine)

* a lot of system interfaces still keep the old style, far from keeping up with the pace of development of the times, they do not have the characteristics of innovation, fun, lack of modern interactive elements and user experience
* most systems are designed from the programmer's point of view, without considering the actual product, operational requirements, such systems function is too simple, the details are not refined enough, the lack of long-term maintenance
* in addition, we are exploring a new community mode, to achieve [Solo](https://github.com/b3log/solo)) and [Symphony](https://github.com/b3log/symphony) combined with [community new experience](https://hacpai.com/b3log)

(funny)

* universal GitHub even can not find a Java community system, Sym to fill the gaps in the universe
* do the most popular open source community system, is expected to be a few years after 82% of the community will be built in Sym
* Ruby/Python/Node.js/(especially) PHP how can do better than Java

### Usecases

Personal maintenance:

* [黑客派](https://hacpai.com)
* [宽客网](http://www.cnq.net)
* [贵州IT](http://www.gzit.info)
* [超级产品经理](https://imspm.com)
* [Titandb 学习主站](https://titandb.cn)

Company maintenance:

* [四方环视](http://bbs.ivrpano.com)

If you build it, you are welcome to add your site to this list by Pull Request :-p

## Features

Specific function point details can be viewed at [Sym 功能点脑图](http://naotu.baidu.com/file/cd31354ac9abc047569c73c560a5a913?token=b9750ae13f39ef9a), the following lists the main features of Sym, and illustrates why Sym is **modern**.

### Good editors

![1](https://cloud.githubusercontent.com/assets/873584/19893287/afb9dc8a-a083-11e6-84d9-944d03550415.gif)

* Markdown: support GFM syntax and some extended syntax
* format adjustment: bold, italics, hyperlinks, references, etc. the list toolbar button also supports shortcut keys
* file upload: support for copy and paste or drag and drop to upload pictures; support for uploading ordinary files; MP3 will use the online player for rendering
* clipboard processing: automatically copy the contents of the conversion to Markdown format; the image of the chain automatically uploaded to the station
* @ username: according to the automatic completion, support shortcut key
* Emoji: most of the mainstream support for Emoji expression, shortcut keys for automatic completion
* mathematical formula: support LaTex mathematical formula rendering
* data temporary local browser: support for temporary storage of data, to avoid accidental edits lost

### Intelligent and flexible information architecture

![2](https://cloud.githubusercontent.com/assets/873584/19887754/407a71ea-a065-11e6-9595-8f352115ed03.png)

The traditional node type community requires the post must belong to a node, the information architecture is top-down. Sym is not a top-down node type information architecture, the post does not require a fixed classification, through the tag to **aggregate** into a domain.

* tag: according to the content of the post intelligent extraction keyword tag automatic completion, a post to multiple tags. The structure of the graph with edge weight is described, which is convenient to calculate the correlation
* domain: a domain contains multiple tags, tags will automatically by post polymerization to specific areas, at any time can increase and decrease the associated tags so as to adjust the scope of the domain, for the final polymerization card list

### To meet the diverse needs of post

![3](https://cloud.githubusercontent.com/assets/873584/19893288/afc3a74c-a083-11e6-8d1a-356fcb17e94a.png)

Currently supports 4 types of posts, to meet the different user preferences:

* General Post: ask or share experiences and insights that help others
* Thoughts: to record and replay the writing process, the text version of the watch [(?)](https://hacpai.com/article/1441942422856)
* Private Discussion: invite friends to communicate in a private space
* City Broadcast: the recruitment of your city, Meetup, etc.

In addition, all posts can be set **playing area**, it can be placed some "treasure" content, only after the user can enjoy the reward. Play area editor also supports Markdown, Emoji and file upload and other characteristics. Play area to support content updates, and can adjust the value at any time.

For testing posts, you can use the Sandbox mechanism: with the Sandbox tag posts will be considered as a test post, will not show in the home or in a field, will only show in the post of their own posts list.

### Humanized interactive replies

![4](https://cloud.githubusercontent.com/assets/873584/19893286/afb61794-a083-11e6-80d7-032397e08cc5.gif)

* in real time: replies submitted after the other readers can not refresh the page to see your reply in time
* floating scroll: at any time at any time convenient reply editor replies to scroll the screen to see other content, do not have to worry about losing focus
* real reply: reply for the reply, which is based on the user @, @ is not mentioned in reply, many systems are not considering this point. Reply / reference can be carried through in the current position, can also jump to reply, and support for cross page jump
* intelligent robots: robots can also send in a discussion and active atmosphere at the same time maybe robots can really solve some problems

### User personalization

![5](https://cloud.githubusercontent.com/assets/873584/19893289/afc52176-a083-11e6-917d-8d01743e194f.gif)

Users can set a lot of parameters to meet the individual needs:

* customizes size of a list per page
* replies: the traditional browsing mode (press release time ascending, real-time push refresh); real time (press release time DESC, real-time push new replies)
* picture browse mode: original (GIF support graph); static map
* Chrome notification / message subscription / keyboard shortcut switch
* set commonly used Emoji, to facilitate the release of the contents of the rapid insertion of expression

In addition to the function of personalized configuration items, there are a lot of privacy switch:

* whether public posts / replies list
* do you have a public focus on the users / tags / fans, the collection posts, and the list of points
* is open online
* whether public UA information
* whether public geographic location
* whether to participate in the wealth / consumption ranking

In addition, users can also complete export data, including posts and replies.

### Edit history and anonymous posting

![6](https://cloud.githubusercontent.com/assets/873584/19893290/afca2270-a083-11e6-9755-9a779c6d91b9.png)

Post can be updated, but each update will produce a history of editing, visitors can see and compare the changes in its content. There is no support for subsequent replies update, will join, please look forward to.

Posts and replies can be used to publish anonymous identity, you know.

### Search engine friendly

* tags, fields can be custom links, and can be individually configured with title/keywords/description meta
* back end Markdown rendering, output crawler readable HTML
* administrator settings browsing permissions do not affect the crawler crawl content
* through some HTML details properties (such as canonical) for SEO

### Real-time message notification

![8](https://cloud.githubusercontent.com/assets/873584/19893304/b6c59b0e-a083-11e6-8349-e95947cca27d.png)

* to display messages by category, mark has read
* with the message type: receive replies, receive a reply, @ I, I pay attention to people, integral city, information systems and other related
* friendly Chrome desktop alerts

### Fun activities

![7](https://cloud.githubusercontent.com/assets/873584/19893531/7198983c-a084-11e6-9b12-8ff7f4f6b4d9.gif)

* receive attendance awards, receive an active reward yesterday
* Shanghai Lottery
* Word [(?)](https://hacpai.com/article/1465995560698)
* Snake

Follow up will continue to add some fun online games.

### Powered management console

![9](https://cloud.githubusercontent.com/assets/873584/19893291/afeeda8e-a083-11e6-9232-620833f9d22e.png)

* background: data statistics, version check
* user management: search users, add users, user data maintenance (state settings, points, etc.)
* post management: add new search posts, posts, post maintenance data (top, rebuild index etc.)
* replies Management: setting up, update
* domain management: add fields, field data maintenance (associated labels, icons, URI, CSS, description, etc.)
* label management: add tags, tag data maintenance (pictures, URI, CSS, description, etc.)
* keep word management: add the reserved word, keep the word data maintenance
* invitation code management: generate invitation code, invitation code data maintenance
* advertising management: sidebar exclusive booth maintenance
* other management post / replies: open / close / switch; user registration invitation code switch

### Open content API

Sym [API](https://hacpai.com/article/1457158841475) synchronous posts, replies [B3log idea](https://hacpai.com/b3log), the [Solo](https://github.com/b3log/solo), [Typecho](https://github.com/DT27/B3logForHacPai), [Z-BlogPHP](https://github.com/zblogapp/hacpai), [WordPress](https://github.com/zh-h/hacpai-sync-wordpress) have provided plug-ins to content synchronization, welcome to access!

In the future will be added to the new interface, to facilitate the production of APP client or some of the value of the application.

### Integrated cloud mail service

![10](https://cloud.githubusercontent.com/assets/873584/19893292/aff5f918-a083-11e6-96be-8519149ff43f.png)

Through the configuration can be integrated [SendCloud](http://sendcloud.sohu.com) cloud mail service, the service rate is more reliable to build their own mail service.

* user login authentication, password reset by trigger type
* weekly subscription push through batch type

### Integrated cloud search service

![11](https://cloud.githubusercontent.com/assets/873584/19893293/b0014ba6-a083-11e6-9c9a-9debad14c826.png)

* through the configuration can be integrated [Algolia](https://www.algolia.com/referrals/1faf0d17/join) cloud search service, can customize the search field, and the weight and ranking optimization
* can also build their own Elasticsearch and integrated through the configuration

Follow up on the search function to strengthen, to achieve the conditions for filtering, the results highlight, please look forward to.

## Roadmap

### trust system

Through the historical data to carry on the classification to the user, lets the user gradually change to the participant, the organizer, the management, realizes the autonomous community ecosystem.

### badge system

Record the achievements of the users and the contribution to the community.

## Installation

### Requirements

* JDK8
* Maven3+
* MySQL5.5+
* Jetty9 or Tomcat9

### Steps

1. [Download](https://github.com/b3log/symphony/archive/master.zip) source code
2. to modify the database after extracting `src/main/resources/local.properties` configuration, and create a database
3. _may_ need to modify the `latke.properties` port for container port
4. _may_ need to modify the `init.properties` administrator account
3. use `mvn install` to build
4. to deploy the war package to the container, the database table will be created automatically at the start of the first

Notice:

* there is no database table SQL script, manual database, table automatically generated in the first boot
* production environment is recommended to use reverse proxy, and the need to configure the WebSocket agent
* Tomcat with more than 9 version, it is best to use the latest version
* reference [Latke 配置剖析](https://hacpai.com/article/1474087427032)

### Configuration

* image upload the default is to upload the server locally, to use [七牛](https://portal.qiniu.com/signup?code=3lewbghpvrqky) can be configured in the `qiniu.*` attribute `symphony.properties`
* mail is sent using [SendCloud](http://sendcloud.sohu.com), you need to configure the `sendcloud.*` attribute in `symphony.properties`
* user registration required to verify the mailbox, so you must first configure the SendCloud

If you encounter problems, you can refer to this [post](https://hacpai.com/article/1468824093225).

## License

There is no difference between a commercial license and an open source license, and you can choose one of the following two ways.

### Open source license

Sym is the use of GPL as an open source agreement, if you choose the open source license, you must fully comply with the relevant provisions of GPL.

### Commercial license

If you need to use Sym for commercial use, including but not limited to the following scenarios:

* company use
* profitability

You must pay the price for $2000, please contact QQ 845765 or email (DL88250@gmail.com) for detailed consultation.

## Contribution

### Authors

The main authors of Sym are [Daniel](https://github.com/88250) and [Vanessa](https://github.com/Vanessa219), all contributors can see [here](https://github.com/b3log/symphony/graphs/contributors).

We are very much looking forward to your joining in this project, whether it is the use of feedback or code patches, are on the Sym a full of love: heart:

### Discussion area

* to Sym official [discussion area](https://hacpai.com/tag/Sym) post (Recommended Practice)
* new a [issue](https://github.com/b3log/symphony/issues/new)
* join Sym development support QQ group 17370164

## Feeling

In the implementation of [B3log idea](https://hacpai.com/b3log) of these years:

* we have witnessed the rise and fall of xAE (GAE/BAE/SAE/etc). In 2009 GAE was selected as a server, and began to implement [Latke](https://github.com/b3log/latke) framework to solve the cross platform cloud, until [GAE] (https://hacpai.com/article/1443685401909), bid farewell to sigh technical change quickly
* feel the advantages and disadvantages of self-made wheels, and certainly a bit: for a long to product, technology advantages far outweigh the disadvantages of self-made frame
* a playful product or the details of the characteristics of the egg, it needs to be done is a product / feature that can continue to provide user value.
* although until the current B3log product users are not many, but we have a preliminary proof: Java used to achieve the blog, forum, there is nothing bad
* the use of open source software, to understand the idea of open source, into the open source ecology
* [如果你想做个程序员相关的论坛，请三思](https://hacpai.com/article/1471007706462)
* [你怎么看待社群、社区这两个词？](https://hacpai.com/article/1465652829809)
* [UGC 社区价值生态](https://hacpai.com/article/1462028669762)

## Credits

Sym's birth can not be separated from the following open source projects:

* [jQuery](https://github.com/jquery/jquery): Front end JavaScript tool library
* [CodeMirror](https://github.com/codemirror/CodeMirror): Front end Markdown editor kernel
* [Highlight.js](https://github.com/isagalaev/highlight.js): Front end code highlighter
* [emojify.js](https://github.com/Ranks/emojify.js): Front end Emoji tool library
* [APlayer](https://github.com/DIYgod/APlayer): Front end audio library
* [ECharts](https://github.com/ecomfe/echarts): Front end interactive chart library
* [MathJax](https://github.com/mathjax/MathJax): Front end Math rendering library
* [SoundRecorder](https://github.com/rderveloy/JavaScript-Sound-Recorder): Front end HTML sound recorder
* [ZeroClipboard](https://github.com/zeroclipboard/zeroclipboard): Front end clipboard
* [JavaScript MD5](http://pajhome.org.uk/crypt/md5/index.html): Front endJavaScript MD5
* [ReconnectingWebSocket](https://github.com/joewalnes/reconnecting-websocket): Front end WebSocket reconnecting
* [to-markdown](https://github.com/domchristie/to-markdown): Front end HTML to Markdown
* [UAParser.js](https://github.com/faisalman/ua-parser-js): Front end User-Agent parser
* [Sass](http://sass-lang.com): Front end CSS preprocessor
* [jsoup](https://github.com/jhy/jsoup): Java HTML parser
* [pegdown](https://github.com/sirthias/pegdown): Java Markdown parser
* [Apache Commons](http://commons.apache.org): Java tool library
* [Jodd](https://github.com/oblac/jodd): Java tool library
* [emoji-java](https://github.com/vdurmont/emoji-java): Java Emoji tool library
* [User-Agent-Utils](https://github.com/HaraldWalker/user-agent-utils): Java User-Agent parser
* [Druid](https://github.com/alibaba/druid): Java database connection pool
* [FreeMarker](http://freemarker.org): popular Java template engine
* [Latke](https://github.com/b3log/latke): full-stack Java Web framework
* [NetBeans](https://netbeans.org): wonderful IDE
* [IntelliJ IDEA](https://www.jetbrains.com/idea): another wonderful IDE

----

<p align = "center">
<strong>Next generation community system, build for the future</strong>
<br><br>
<img src="https://cloud.githubusercontent.com/assets/873584/19897809/84c4ed56-a094-11e6-8498-43e9337c475f.png">
</p>

