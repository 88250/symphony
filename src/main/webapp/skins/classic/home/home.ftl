<#include "macro-home.ftl">
<@home "home">
<div class="<#if isMyArticle>article-list<#else>home</#if> list">
    <ul> 
        <#list userHomeArticles as article>
        <li>
            <div style="width:606px">
                <h2><a rel="bookmark" href="${article.articlePermalink}">${article.articleTitle}</a></h2>
                <span class="ft-small">
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                        ${articleTag}</a><#if articleTag_has_next>, </#if>
                    </#list>
                    <span class="ico-date">${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}</span>
                </span>
            </div>
            <#if article.articleCommentCount != 0>
            <div class="ft-small ico-cmt">
                ${article.articleCommentCount}
                ${cmtLabel}
            </div>
            </#if>
            <#if isMyArticle>
            <div class="commenters">
                <a href="${servePath}/update-article?id=${article.oId}" title="${editLabel}"><span class="ico-edit"></span></a>
            </div>
            </#if>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="/member/${user.userName}"/>
</@home>