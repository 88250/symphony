<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="list content admin">
    <ul>
        <#list comments as item>
        <li>
            <div class="fn-clear">
                <img class="avatar" src="${item.commentAuthorThumbnailURL}-64.jpg" title="${item.commentAuthorName}">
                <a href="${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
                <#if item.commentStatus == 0>
                <span class="ft-small">${validLabel}</span>
                <#else>
                <font class="ft-red ft-small">${banLabel}</font>
                </#if>
                <a href="/admin/comment/${item.oId}" class="fn-right icon icon-edit" title="${editLabel}"></a>
                <span class="icon icon-date fn-right ft-small" title="${createTimeLabel}"> ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')} &nbsp;</span>
            </div>
            <div class="content-reset">
                 ${item.commentContent}
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/comments"/>
</div>
</@admin>