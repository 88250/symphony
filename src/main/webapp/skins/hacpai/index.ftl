<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symphonyLabel}${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="wrapper">
            <div class="fn-flex ads">
                <div class="fn-flex-1 ad ad1">
                    <span class="heading">画家</span>
                    <img src="${staticServePath}/skins/hacpai/static/images/ad3.jpg"/>
                    <a href="/" class="title fn-ellipsis">发现发现发现发现</a>
                </div>
                <div>
                    <div class="fn-clear">
                        <a class="ad fn-left" href="/">
                            <span class="heading">黑客</span>
                            <div class="title">
                                黑客黑客黑客黑客黑客黑客黑客黑客黑客黑客黑客客黑客客黑客
                            </div>
                        </a>
                        <div class="ad fn-left ad2">
                            <span class="heading">发现</span>
                            <img src="${staticServePath}/skins/hacpai/static/images/ad1.jpg"/>
                            <a href="/" class="title fn-ellipsis">发现发现发现发现</a>
                        </div>
                    </div>
                    <div class="ad fn-left ad3">
                        <span class="heading">活动</span>
                        <img src="${staticServePath}/skins/hacpai/static/images/ad2.jpg"/>
                        <a href="/" class="title fn-ellipsis">发现发现发现发现</a>
                    </div>
                </div>
            </div>

            <div class="hot-tags">
                此刻热词： 
                <#list navTrendTags as trendTag>
                <a rel="tag" href="/tags/${trendTag.tagTitle?url('UTF-8')}">${trendTag.tagTitle}</a>
                </#list>
            </div>

            <div class="fn-flex">
                <div class="fn-flex-1 content">
                    <ul class="fn-clear fn-list">
                        <li>
                            <div class="header">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="title">发表文章或作品发表文章或作品</div>
                                <div class="abstract">免费观看、下载、高清大图【无水印】最新海报#创意海报#海报 创意#平面广告#汽车平面广告#汽车海报设计#汽车平面设计#汽车创意海报 #创意广告Iveco依维柯汽车创意海报设计：带着所有的东西</div>
                            </div>
                            <div class="info">
                                <div class="fn-flex">
                                    <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                    <div class="fn-flex-1">
                                        <div class="fn-ellipsis">
                                            <span class="cmt-name">评论人：</span>
                                            <span class="cmt-content">司司法所的服务费法所的服务费</span>
                                        </div>
                                        <div class="fn-ellipsis date">2015-12-12</div>
                                    </div>
                                </div>
                                <div class="fn-clear">
                                    <div class="count fn-right">
                                        <span class="icon-view">18</span> &nbsp;
                                        <span class="icon-cmts">18</span> &nbsp;
                                        <span class="icon-heart">18</span> &nbsp;
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div class="header">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="title">发表文章或作品发表文章或作品</div>
                                <div class="abstract">免费观看、下载、高清大图【无水印】最新海报#创意海报#海报 创意#平面广告#汽车平面广告#汽车海报设计#汽车平面设计#汽车创意海报 #创意广告Iveco依维柯汽车创意海报设计：带着所有的东西</div>
                            </div>
                            <div class="info">
                                <div class="fn-flex">
                                    <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                    <div class="fn-flex-1">
                                        <div class="fn-ellipsis">
                                            <span class="cmt-name">评论人：</span>
                                            <span class="cmt-content">司司法所的服务费法所的服务费</span>
                                        </div>
                                        <div class="fn-ellipsis date">2015-12-12</div>
                                    </div>
                                </div>
                                <div class="fn-clear">
                                    <div class="count fn-right">
                                        <span class="icon-view">18</span> &nbsp;
                                        <span class="icon-cmts">18</span> &nbsp;
                                        <span class="icon-heart">18</span> &nbsp;
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div class="header">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="title">发表文章或作品发表文章或作品</div>
                                <div class="abstract">免费观看、下载、高清大图【无水印】最新海报#创意海报#海报 创意#平面广告#汽车平面广告#汽车海报设计#汽车平面设计#汽车创意海报 #创意广告Iveco依维柯汽车创意海报设计：带着所有的东西</div>
                            </div>
                            <div class="info">
                                <div class="fn-flex">
                                    <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                    <div class="fn-flex-1">
                                        <div class="fn-ellipsis">
                                            <span class="cmt-name">评论人：</span>
                                            <span class="cmt-content">司司法所的服务费法所的服务费</span>
                                        </div>
                                        <div class="fn-ellipsis date">2015-12-12</div>
                                    </div>
                                </div>
                                <div class="fn-clear">
                                    <div class="count fn-right">
                                        <span class="icon-view">18</span> &nbsp;
                                        <span class="icon-cmts">18</span> &nbsp;
                                        <span class="icon-heart">18</span> &nbsp;
                                    </div>
                                </div>
                            </div>
                        </li>
                        <li>
                            <div class="header">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="title">发表文章或作品发表文章或作品</div>
                                <div class="abstract">免费观看、下载、高清大图【无水印】最新海报#创意海报#海报 创意#平面广告#汽车平面广告#汽车海报设计#汽车平面设计#汽车创意海报 #创意广告Iveco依维柯汽车创意海报设计：带着所有的东西</div>
                            </div>
                            <div class="info">
                                <div class="fn-flex">
                                    <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                    <div class="fn-flex-1">
                                        <div class="fn-ellipsis">
                                            <span class="cmt-name">评论人：</span>
                                            <span class="cmt-content">司司法所的服务费法所的服务费</span>
                                        </div>
                                        <div class="fn-ellipsis date">2015-12-12</div>
                                    </div>
                                </div>
                                <div class="fn-clear">
                                    <div class="count fn-right">
                                        <span class="icon-view">18</span> &nbsp;
                                        <span class="icon-cmts">18</span> &nbsp;
                                        <span class="icon-heart">18</span> &nbsp;
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="side">
                    <div class="fn-clear"> 
                        <button class="btn orange">马上签到</button>
                        <button class="btn fn-right">发表文章或作品</button>
                    </div>
                    <div class="panel">
                        <div class="panel-title fn-clear">
                            最近文章
                            <span class="fn-right">+更多</span>
                        </div>
                        <ul class="fn-list">
                            <li class="fn-flex">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="fn-flex-1">
                                    <div class="title fn-ellipsis">
                                        Sym 社区介绍
                                    </div>
                                    <div class="abstract">
                                        Sym 是程序员和设计师的社交社区，汇聚了来自 Sym 是程序员和设计师的社交社区，汇聚了来自 Sym 是程序员和设计师的社交社区，汇聚了来自
                                    </div>
                                </div>
                            </li>

                            <li class="fn-flex">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="fn-flex-1">
                                    <div class="title fn-ellipsis">
                                        Sym 社区介绍
                                    </div>
                                    <div class="abstract">
                                        Sym 是程序员和设计师的社交社区，汇聚了来自
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </div>

                    <div class="panel">
                        <div class="panel-title fn-clear">
                            与你兴趣相同的伙伴
                            <span class="fn-right">+更多</span>
                        </div>
                        <div class="fn-clear">
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                            <div class="avatar-wrap">
                                <img class="avatar" src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <span class="hover"></span>
                            </div>
                        </div>
                    </div>

                    <div class="panel">
                        <div class="panel-title fn-clear">
                            随便看看
                            <span class="fn-right">+更多</span>
                        </div>
                        <ul class="fn-list">
                            <li class="fn-flex">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="fn-flex-1">
                                    <div class="title fn-ellipsis">
                                        Sym 社区介绍
                                    </div>
                                    <div class="abstract">
                                        Sym 是程序员和设计师的社交社区，汇聚了来自 Sym 是程序员和设计师的社交社区，汇聚了来自 Sym 是程序员和设计师的社交社区，汇聚了来自
                                    </div>
                                </div>
                            </li>

                            <li class="fn-flex">
                                <img src="http://localhost:8084/skins/hacpai/static/images/ad3.jpg"/>
                                <div class="fn-flex-1">
                                    <div class="title fn-ellipsis">
                                        Sym 社区介绍
                                    </div>
                                    <div class="abstract">
                                        Sym 是程序员和设计师的社交社区，汇聚了来自
                                    </div>
                                </div>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
