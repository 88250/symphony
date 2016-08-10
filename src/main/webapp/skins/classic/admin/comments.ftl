<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="list content admin">
    <ul>
        <#list comments as item>
        <li>
            <div class="fn-clear">
                <div class="avatar tooltipped tooltipped-w" style="background-image:url('${item.commentAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')" 
                     aria-label="${item.commentAuthorName}"></div>
                <a href="${item.commentSharpURL}">${item.commentArticleTitle}</a> &nbsp;
                <#if item.commentStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="${servePath}/admin/comment/${item.oId}" class="fn-right tooltipped tooltipped-e ft-a-icon" aria-label="${editLabel}"><span class="icon-edit"></span></a>
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
    <@pagination url="/admin/comments"/>
</div>
</@admin>