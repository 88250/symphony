<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userWatchingArticleStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
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
                    <button class="small fn-right" onclick="Util.unfollow(this, '${article.oId}', 'article-watch')">${unfollowLabel}</button>
                    <#else>
                    <button class="small fn-right" onclick="Util.follow(this, '${article.oId}', 'article-watch')">${followLabel}</button>
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
