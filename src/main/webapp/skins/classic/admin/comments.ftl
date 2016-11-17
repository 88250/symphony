<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="content admin">
    <div class="module list">
        <ul>
            <#list comments as item>
            <li>
                <div class="fn-clear">
                    <div class="avatar tooltipped tooltipped-w" style="background-image:url('${item.commentAuthorThumbnailURL}')" 
                         aria-label="${item.commentAuthorName}"></div>
                    <a href="${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
                    <#if item.commentStatus == 0>
                    <span class="ft-gray">${validLabel}</span>
                    <#else>
                    <font class="ft-red">${banLabel}</font>
                    </#if>
                    <a href="${servePath}/admin/comment/${item.oId}" class="fn-right tooltipped tooltipped-e ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                    <span class="fn-right ft-gray tooltipped tooltipped-w" aria-label="${createTimeLabel}"> 
                        <span class="icon-date"></span>
                        ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')} &nbsp;</span>
                </div>
                <div class="content-reset">
                    ${item.commentContent}
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/comments"/>
    </div>
</div>
</@admin>