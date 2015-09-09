<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "home">
<div class="list">
    <ul> 
        <#list userHomeArticles as article>
        <li>
            <div>
                <h2><a rel="bookmark" href="${article.articlePermalink}">${article.articleTitleEmoj}</a></h2>
                <span class="ft-gray">
                    <#list article.articleTags?split(",") as articleTag>
                    <a class="tag" rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                        ${articleTag}</a>
                    </#list> &nbsp; 
                    <span class="icon-date"></span>
                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
            <#if isMyArticle>
            <div class="cmts">
                <a class="icon-edit" href="/update?id=${article.oId}" title="${editLabel}"></a>
            </div>
            <#else>
            <#if article.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${article.articlePermalink}">${article.articleCommentCount}</a>
            </div>
            </#if>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="/member/${user.userName}"/>
</@home>