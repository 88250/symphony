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
<#macro list listData>
<#include "common/title-icon.ftl">
<div class="article-list list">
    <ul>
        <#assign articleIds = "">
        <#list listData as article>
        <#assign articleIds = articleIds + article.oId>
        <#if article_has_next><#assign articleIds = articleIds + ","></#if>
        <li<#if article.articleStickRemains gt 0 && articleStickCheck??> class="stick"</#if>>
            <div class="fn-flex">
                <#if article.articleAnonymous == 0>
                <a rel="nofollow" class="ft-gray"
                   href="${servePath}/member/${article.articleAuthorName}"
                   ></#if><div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>
                <div class="fn-flex-1 has-view">
                    <h2 class="fn-ellipsis">
                        <@icon article.articlePerfect article.articleType></@icon>
                        <a data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark"
                           href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
                        </a>
                        <#if article.articleStick gt 0 && article.articleStick < 9223372036854775807>
                        <span class="ft-smaller ft-red stick-remains fn-none">${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}</span>
                        </#if>
                    </h2>
                    <div class="ft-smaller">
                    <span class="ft-fade">${article.timeAgo}</span>
                    <#if "" != article.articleLatestCmterName>
                    <span class="ft-fade">•&nbsp;${latestCmtFromLabel}</span> 
                    <#if article.articleLatestCmterName != 'someone'>
                    <a rel="nofollow" class="ft-gray" href="${servePath}/member/${article.articleLatestCmterName}"><#else><span class="ft-gray"></#if>${article.articleLatestCmterName}<#if article.articleLatestCmterName != 'someone'></a><#else></span></#if>
                    </#if>
                    </div>
                </div>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </li>
        </#list>
    </ul>
</div>
</#macro>
<#macro listScript>
<#if articleIds??>
<script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
<script>
    // Init [Article List] channel
    ArticleListChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/article-list-channel?articleIds=${articleIds}");
</script>
</#if>
</#macro>