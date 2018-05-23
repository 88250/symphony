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
<div class="tabs-sub fn-clear">
    <a pjax-title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a pjax-title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a pjax-title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a pjax-title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
    <a pjax-title="${followersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
    <a pjax-title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/breezemoons"<#if type == "breezemoons"> class="current"</#if>>${breezemoonLabel}</a>
</div>
<#if 0 == user.userFollowingArticleStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <#if userHomeFollowingArticles?size == 0>
    <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul class="fn-clear">
        <#list userHomeFollowingArticles as article>
        <li class="fn-flex<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && article_index == userHomeFollowingArticles?size - 1>
             last</#if>">
            <#if "someone" != article.articleAuthorName>
            <a aria-label="${article.articleAuthorName}" class="tooltipped tooltipped-s"
               target="_blank" rel="nofollow" href="${servePath}/member/${article.articleAuthorName}"></#if>
                <div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div>
                <#if "someone" != article.articleAuthorName></a></#if>
            <div class="fn-flex-1">
                <h2>
                    <@icon article.articlePerfect article.articleType></@icon>
                    <a rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-fade ft-smaller">
                    <#list article.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list>  •  
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span> 
            </div>
            <#if isLoggedIn>
            <#if article.isFollowing>
            <button class="mid" onclick="Util.unfollow(this, '${article.oId}', 'article')">${uncollectLabel}</button>
            <#else>
            <button class="mid" onclick="Util.follow(this, '${article.oId}', 'article')">${followLabel}</button>
            </#if>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/articles" pjaxTitle="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>