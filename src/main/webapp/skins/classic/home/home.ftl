<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a pjax-title="${articleLabel} - ${user.userName} - ${symphonyLabel}" href="${servePath}/member/${user.userName}"<#if type == "home"> class="current"</#if>>${articleLabel}<#if type == "home"> &nbsp;<span class="count">${paginationRecordCount?c}</span></#if></a>
    <a pjax-title="${cmtLabel} - ${user.userName} - ${symphonyLabel}" href="${servePath}/member/${user.userName}/comments"<#if type == "comments"> class="current"</#if>>${cmtLabel}</a>
    <#if currentUser?? && currentUser.userName == user.userName>
    <a pjax-title="${anonymousArticleLabel} - ${user.userName} - ${symphonyLabel}"<#if type == "articlesAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/articles/anonymous">${anonymousArticleLabel}<#if type == "articlesAnonymous"> &nbsp;<span class="count">${paginationRecordCount?c}</span></#if></a>
    <a pjax-title="${anonymousCommentLabel} - ${user.userName} - ${symphonyLabel}"<#if type == "commentsAnonymous"> class="current"</#if> href="${servePath}/member/${user.userName}/comments/anonymous">${anonymousCommentLabel}</a>
    </#if>
</div>
<#if 0 == user.userArticleStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list">
    <#if userHomeArticles?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul> 
        <#list userHomeArticles as article>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && article_index == userHomeArticles?size - 1>
            class="last"
        </#if>>
            <div class="has-view fn-flex-1">
                <h2>
                    <#if 1 == article.articlePerfect>
                    <span class="tooltipped tooltipped-e" aria-label="${perfectLabel}"><svg height="20" viewBox="3 3 11 12" width="14">${perfectIcon}</svg></span>
                    </#if>
                    <#if 1 == article.articleType>
                    <span class="tooltipped tooltipped-e" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                    <#elseif 2 == article.articleType>
                    <span class="tooltipped tooltipped-e" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                    <#elseif 3 == article.articleType>
                    <span class="tooltipped tooltipped-e" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                    </#if>
                    <a rel="bookmark" href="${servePath}${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-fade ft-smaller">
                    <#list article.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list> •
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
            <#if isMyArticle && 3 != article.articleType && permissions["commonUpdateArticle"].permissionGrant>
            <div class="cmts">
                <a class="ft-a-title tooltipped tooltipped-w" href="${servePath}/update?id=${article.oId}" aria-label="${editLabel}"><span class="icon-edit"></span></a>
            </div>
            <#else>
            <#if article.articleCommentCount != 0>
            <div class="cmts tooltipped tooltipped-w" aria-label="${cmtLabel}${quantityLabel}">
                <a class="count ft-gray" href="${servePath}${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}" pjaxTitle="${articleLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>