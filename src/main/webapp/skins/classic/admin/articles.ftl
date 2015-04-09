<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "articles">
<div class="list content admin">
    <ul>
        <#list articles as item>
        <li>
            <a href="${item.articlePermalink}">${item.articleTitle}</a> &nbsp;
            <#if item.articleStatus == 0>
            ${validLabel}
            <#else>
            <font style="color: red">
            ${banLabel}
            </font>
            </#if>
            <a href="/admin/article/${item.oId}" class="fn-right edit" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a>
            <br/>
            <img class="avatar" src="${item.articleAuthorThumbnailURL}">${item.articleAuthorName} &nbsp;
            <span class="icon icon-tags" title="${tagLabel}"></span>
            <span class="tags">
                ${item.articleTags}
            </span> 
            <span class="fn-right ft-small">
                <span class="icon icon-view" title="${viewCountLabel}"></span>
                ${item.articleViewCount} &nbsp;
                <span class="icon icon-cmts" title="${commentCountLabel}"></span>
                ${item.articleCommentCount} &nbsp;
                <span class="icon icon-date" title="${createTimeLabel}"></span>
                ${item.articleCreateTime?string('yyyy-MM-dd HH:mm')}
            </span>

        </li>
        </#list>
    </ul>
    <@pagination url="/admin/articles"/>
</div>
</@admin>
