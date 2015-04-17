<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="list content admin">
    <div>
        <form method="GET" action="tags">
            <input name="title" style="margin-right: 10px; " placeholder="${tagLabel}"/>
            <button type="submit" class="green">${searchLabel}</button>
        </form>
    </div>

    <ul>
        <#list tags as item>
        <li>
            <a href="/tags/${item.tagTitle?url('utf-8')}">${item.tagTitle}</a> &nbsp;
            <#if item.tagStatus == 0>
            ${validLabel}
            <#else>
            <font style="color: red">
            ${banLabel}
            </font>
            </#if>
            <a href="/admin/tag/${item.oId}" class="fn-right edit" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a>
            <br/>
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

        </li>
        </#list>
    </ul>
    <@pagination url="/admin/tags"/>
</div>
</@admin>
