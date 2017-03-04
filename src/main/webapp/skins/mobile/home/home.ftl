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
                    <#if 1 == article.articlePerfect>
                    <svg height="20" viewBox="3 4 11 12" width="14">${perfectIcon}</svg>
                    </#if>
                    <#if 1 == article.articleType>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif 2 == article.articleType>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    <#elseif 3 == article.articleType>
                    <span class="icon-video" title="${thoughtLabel}"></span>
                    </#if>
                    <a rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-gray">
                    <#list article.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list><br/>
                    <span class="icon-date"></span>
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
            <#if isMyArticle && 3 != article.articleType && permissions["commonUpdateArticle"].permissionGrant>
            <div class="cmts">
                <a class="icon-edit" href="${servePath}/update?id=${article.oId}" title="${editLabel}"></a>
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