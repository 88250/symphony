<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingArticles">
<div class="home list">
    <ul class="fn-clear">
        <#list userHomeFollowingArticles as article>
        <li>
            <div>
                <h2> 
                    <a title="${article.articleAuthorName}"
                        target="_blank" rel="nofollow" href="/member/${article.articleAuthorName}">
                        <img class="avatar-small fn-left" src="${article.articleAuthorThumbnailURL}-64"/>
                    </a>
                    &nbsp;
                    <a rel="bookmark" href="${article.articlePermalink}">${article.articleTitleEmoj}</a>
                </h2>
                <span class="ft-small">
                    <span class="icon icon-tags"></span>
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                        ${articleTag}</a><#if articleTag_has_next>, </#if>
                    </#list> &nbsp; 
                    <span class="icon icon-date"></span>
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-small" href="${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="/member/${user.userName}/following/tags"/>
</@home>