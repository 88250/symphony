<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="list content admin">
    <ul>
        <#list comments as item>
        <li>
            <a href="${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
            <#if item.commentStatus == 0>
            ${validLabel}
            <#else>
            <font style="color: red">
            ${banLabel}
            </font>
            </#if>
            <a href="/admin/comment/${item.oId}" class="fn-right edit" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a>
            <br/>
            <img class="avatar" src="${item.commentAuthorThumbnailURL}">${item.commentAuthorName} &nbsp;
            <span class="icon icon-cmts" title="${cmtLabel}"></span>
            <span class="tags">
            ${item.commentContent}
            </span>
            <span class="fn-right ft-small">
                <span class="icon icon-date" title="${createTimeLabel}"></span>
                ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')}
            </span>

        </li>
        </#list>
    </ul>
    <@pagination url="/admin/comments"/>
</div>
</@admin>