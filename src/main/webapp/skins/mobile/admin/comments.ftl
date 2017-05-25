<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="list content admin">
    <ul>
        <#list comments as item>
        <li>
            <div class="fn-clear">
                <div class="avatar" style="background-image:url('${item.commentAuthorThumbnailURL}')" title="${item.commentAuthorName}"></div>
                <a href="${servePath}${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
                <#if item.commentStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="${servePath}/admin/comment/${item.oId}" class="fn-right">${editLabel}</a>
                <div class="fn-hr5"></div>
                ${createTimeLabel} ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')} &nbsp;
            </div>
            <div class="fn-hr5"></div>
            <div class="content-reset">
                 ${item.commentContent}
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="${servePath}/admin/comments"/>
</div>
</@admin>