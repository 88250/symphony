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
        </div>
        <#include "footer.ftl">
    </body>
</html>
