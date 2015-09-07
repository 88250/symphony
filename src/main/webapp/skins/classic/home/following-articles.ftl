<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingArticles">
<div class="list">
    <ul class="fn-clear">
        <#list userHomeFollowingArticles as article>
        <li class="fn-flex read">
            <a title="${article.articleAuthorName}" class="responsive-hide"
               target="_blank" rel="nofollow" href="/member/${article.articleAuthorName}">
                <img class="avatar" src="${article.articleAuthorThumbnailURL}-64.jpg?${article.articleAuthor.userUpdateTime?c}"/>
            </a>
            <div class="fn-flex-1 has-view">
                <h2>
                    <a title="${article.articleAuthorName}" 
                       target="_blank" rel="nofollow" href="/member/${article.articleAuthorName}">
                        <img class="avatar-small responsive-show" src="${article.articleAuthorThumbnailURL}-64.jpg?${article.articleAuthor.userUpdateTime?c}"/>
                    </a>
                    <a rel="bookmark" href="${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-gray">
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" class="tag" href="/tags/${articleTag?url('UTF-8')}">
                        ${articleTag}</a>
                    </#list> &nbsp; 
                    <span class="icon-date"></span>
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span> 
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>