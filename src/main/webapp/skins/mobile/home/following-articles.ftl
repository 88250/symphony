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
<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userFollowingArticleStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <ul class="fn-clear">
        <#list userHomeFollowingArticles as article>
        <li class="fn-flex read">
            <#if "someone" != article.articleAuthorName>
            <a title="${article.articleAuthorName}"
               target="_blank" rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                <div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div>
            <#if "someone" != article.articleAuthorName></a></#if>
            <div class="fn-flex-1 has-view">
                <h2>
                    <@icon article.articlePerfect article.articleType></@icon>
                    <a rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-gray">
                    <#list article.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list><br/>
                    <svg><use xlink:href="#date"></use></svg>
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span> 
            </div>
            <#if isLoggedIn>
            <#if article.isFollowing>
            <button class="green small fn-right" onclick="Util.unfollow(this, '${article.oId}', 'article')">${uncollectLabel}</button>
            <#else>
            <button class="green small fn-right" onclick="Util.follow(this, '${article.oId}', 'article')">${followLabel}</button>
            </#if>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/articles"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>
