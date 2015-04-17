<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="list content admin">
    <ul>
        <#list comments as item>
        <li>
            <div class="fn-clear first">
                <a href="${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
                <#if item.commentStatus == 0>
                <span class="ft-small">${validLabel}</span>
                <#else>
                <font class="ft-red ft-small">${banLabel}</font>
                </#if>
                <a href="/admin/comment/${item.oId}" class="fn-right icon-edit-wrap" title="${editLabel}">
                    <span class="icon icon-edit"></span>
                </a>
            </div>
            <div class="fn-clear">
                <img class="avatar" src="${item.commentAuthorThumbnailURL}">${item.commentAuthorName} &nbsp;
                <span class="icon icon-cmts" title="${cmtLabel}"></span>
                <span class="tags">
                    ${item.commentContent}
                </span>
                <span class="fn-right ft-small">
                    <span class="icon icon-date" title="${createTimeLabel}"></span>
                    ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/comments"/>
</div>
</@admin>