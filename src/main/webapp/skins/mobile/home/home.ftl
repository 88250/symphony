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
<#if 0 == user.userArticleStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <ul> 
        <#list userHomeArticles as article>
        <li>
            <div class="has-view">
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
            <#if isMyArticle && 3 != article.articleType && permissions["commonUpdateArticle"].permissionGrant>
            <div class="cmts">
                <a href="${servePath}/update?id=${article.oId}">${editLabel}</a>
            </div>
            <#else>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>