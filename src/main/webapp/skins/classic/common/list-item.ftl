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
<li>
    <div class="fn-clear ft-smaller list-info">
        <#list article.articleTagObjs as articleTag>
        <a rel="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a> &nbsp;
        </#list>

        <span class="fn-right ft-fade">
            <#if article.articleCommentCount != 0>
                <a class="ft-fade" href="${servePath}${article.articlePermalink}#comments"><b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a> &nbsp;•&nbsp;
            </#if>

            <#if article.articleViewCount != 0>
                <a class="ft-fade" href="${servePath}${article.articlePermalink}"><span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>"><#if article.articleViewCount < 1000>${article.articleViewCount}<#else>${article.articleViewCntDisplayFormat}</#if></span> ${viewLabel}</a>
            </#if>
        </span>
    </div>
    <h2 class="fn-ellipsis">
        <@icon article.articlePerfect article.articleType></@icon>
        <a class="ft-a-title" data-id="${article.oId}" data-type="${article.articleType}" rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}
        </a>
    </h2>
    <div class="fn-flex">
        <div class="fn-flex-1">
            <div class="fn-flex">
                <#if article.articleAnonymous == 0>
                    <a rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if><div
                    class="avatar"
                    style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>

                <div class="fn-ellipsis ft-fade ft-smaller list-info">
                    <#if article.articleAnonymous == 0>
                        <a rel="nofollow" class="author"
                           href="${servePath}/member/${article.articleAuthorName}"></#if>
                    ${article.articleAuthorName}
                    <#if article.articleAnonymous == 0></a></#if>

                    <#if article.articleAuthor.userIntro != '' && article.articleAnonymous == 0>
                        - ${article.articleAuthor.userIntro}
                    </#if>
                    <br>
                    <#if "" != article.articleLatestCmterName>
                        <#if "" != article.articleLatestCmt.clientCommentId>
                            <span class="author">${article.articleLatestCmterName}</span>
                        <#else>
                            <#if article.articleLatestCmterName != 'someone'>
                                    <a rel="nofollow" class="author" href="${servePath}/member/${article.articleLatestCmterName}"></#if><span class="author">${article.articleLatestCmterName}</span><#if article.articleLatestCmterName != 'someone'></a>
                            </#if>
                        </#if>
                        ${article.cmtTimeAgo}${cmtLabel}
                    </#if>
                </div>
            </div>
            <#if isLoggedIn && 1 == currentUser.userListViewMode>
            <a class="abstract" href="${servePath}${article.articlePermalink}">
                ${article.articlePreviewContent}
            </a>
            </#if>
        </div>
        <#if "" != article.articleThumbnailURL && isLoggedIn && 1 == currentUser.userListViewMode>
            <a href="${servePath}${article.articlePermalink}" class="abstract-img" style="background-image:url('${article.articleThumbnailURL}')"></a>
        </#if>
    </div>

    <span class="heat tooltipped tooltipped-n" aria-label="${postActivityLabel}" style="width:${article.articleHeat*3}px"></span>

    <#if article.articleStick gt 0>
        <span class="cb-stick tooltipped tooltipped-e" aria-label="<#if article.articleStick < 9223372036854775807>${stickLabel}${remainsLabel} ${article.articleStickRemains?c} ${minuteLabel}<#else>${adminLabel}${stickLabel}</#if>"><svg class="icon-pin"><use xlink:href="#pin"></use></svg></span>
    </#if>
</li>