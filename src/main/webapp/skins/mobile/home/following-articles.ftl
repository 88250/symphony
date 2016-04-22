<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingArticles">
<div class="list">
    <ul class="fn-clear">
        <#list userHomeFollowingArticles as article>
        <li class="fn-flex read">
            <a title="${article.articleAuthorName}"
               target="_blank" rel="nofollow" href="/member/${article.articleAuthorName}">
                <div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL}-64.jpg?${article.articleAuthor.userUpdateTime?c}')"></div>
            </a>
            <div class="fn-flex-1 has-view">
                <h2>
                    <#if 1 == article.articleType>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif 2 == article.articleType>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    <#elseif 3 == article.articleType>
                    <span class="icon-video" title="${thoughtLabel}"></span>
                    </#if>
                    <a rel="bookmark" href="${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-gray">
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" class="tag" href="/tag/${articleTag?url('UTF-8')}">
                        ${articleTag}</a>
                    </#list><br/>
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