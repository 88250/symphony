<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="list content admin">
    <form method="GET" action="tags" class="form">
        <input name="title" type="text" placeholder="${tagLabel}"/>
        <button type="submit" class="green">${searchLabel}</button>
    </form>

    <ul>
        <#list tags as item>
        <li>
            <div class="fn-clear first">
                <a href="/tags/${item.tagTitle}">${item.tagTitle}</a> &nbsp;
                <#if item.tagStatus == 0>
                <span class="ft-small">${validLabel}</span>
                <#else>
                <font class="ft-red ft-small">${banLabel}</font>
                </#if>
                <a href="/admin/tag/${item.oId}" class="fn-right icon-edit-wrap" title="${editLabel}">
                    <span class="icon icon-edit"></span>
                </a>
            </div>
            <div class="fn-clear">
                <img class="avatar" src="${staticServePath}/images/tags/${item.tagIconPath}" onerror="this.src=''">
                <span class="tags">${item.tagDescription}</span>
                <span class="fn-right ft-small">
                    <span class="icon icon-view" title="${refCountLabel}"></span>
                    ${item.tagReferenceCount} &nbsp;
                    <span class="icon icon-cmts" title="${commentCountLabel}"></span>
                    ${item.tagCommentCount} &nbsp;
                    <span class="icon icon-date" title="${createTimeLabel}"></span>
                    ${item.tagCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/tags"/>
</div>
</@admin>
