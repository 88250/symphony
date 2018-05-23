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
<#include "macro-head.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
    </head>
    <body class="index">
        ${HeaderBannerLabel}
        <#include "header.ftl">
        <@subNav '' ''/>
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="module">
                    <div class="module-header" style="background-color: #97cf76;">
                        <a href="${servePath}/recent">${latestLabel}</a>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list recentArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title fn-ellipsis" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                                <a class="fn-right count ft-gray ft-smaller" href="${servePath}${article.articlePermalink}"><#if article.articleViewCount < 1000>
                                    ${article.articleViewCount}<#else>${article.articleViewCntDisplayFormat}</#if></a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="module">
                    <div class="module-header" style="background-color: #dfb169;">
                        <a href="${servePath}/perfect">${perfectLabel}</a>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list perfectArticles as article>
                            <li<#if !article_has_next> class="last"</#if>>
                                <#if "someone" != article.articleAuthorName>
                                <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                                    <span class="avatar-small tooltipped tooltipped-se slogan"
                                          aria-label="${article.articleAuthorName}"
                                          style="background-image:url('${article.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != article.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title fn-ellipsis" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                                <a class="fn-right count ft-gray ft-smaller" href="${servePath}${article.articlePermalink}">${article.articleViewCount}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <#if tags?size != 0>
        <div class="index-wrap">
            <div class="wrapper">
                <ul class="tag-desc fn-clear">
                    <#list tags as tag>
                    <li>
                        <a rel="nofollow" href="${servePath}/tag/${tag.tagURI}">
                            <#if tag.tagIconPath!="">
                            <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" />
                            </#if>
                            ${tag.tagTitle}
                        </a>
                    </li>
                    </#list>
                </ul>
            </div>
        </div>
        </#if>
        <div class="fn-hr10"></div>
        <div class="main">
            <div class="wrapper">
                <#if ADLabel != ''>
                <div class="module">
                    <div class="module-header" style="background-color: #7ea5c8">
                        <a href="https://hacpai.com/article/1460083956075">${sponsorLabel}</a>
                    </div>
                    <div class="ad module-panel fn-clear">
                        ${ADLabel}
                    </div>
                </div>
                </#if>
                <div class="module">
                    <div class="module-header" style="background-color: #9cd462">
                        <a href="${servePath}/pre-post">${postArticleLabel}</a>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <li><a class="title" href="<#if useCaptchaCheckin??>${servePath}/activity/checkin<#else>${servePath}/activity/daily-checkin</#if>">${activityDailyCheckinLabel}</a></li>
                            <li><a class="title" href="${servePath}/activity/yesterday-liveness-reward">${activityYesterdayLivenessRewardLabel}</a></li>
                            <li><a class="title" href="${servePath}/activity/1A0001">${activity1A0001Label}</a></li>
                            <li><a class="title" href="${servePath}/activity/character">${characterLabel}</a></li>
                        </ul>
                    </div>
                </div>
        </div>
    </div>

    <div class="slogan">
        ${indexIntroLabel}&nbsp;
        <a href="https://github.com/b3log/symphony" target="_blank">
            <svg><use xlink:href="#github"></use></svg></a>
        <a href="http://weibo.com/u/2778228501" target="_blank">
            <svg><use xlink:href="#weibo"></use></svg></a>
        <a target="_blank"
           href="http://shang.qq.com/wpa/qunwpa?idkey=981d9282616274abb1752336e21b8036828f715a1c4d0628adcf208f2fd54f3a">
            <svg><use xlink:href="#qq"></use></svg></a>
        <a target="_blank" rel="noopener"
           href="https://t.me/b3log">
            <svg><use xlink:href="#icon-telegram"></use></svg></a>
    </div>
    <#include "footer.ftl">
</body>
</html>