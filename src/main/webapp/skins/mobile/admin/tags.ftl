<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="list content admin">
    <form method="GET" action="tags" class="form">
        <input name="title" type="text" placeholder="${tagLabel}"/>
        <button type="submit" class="green">${searchLabel}</button>
    </form>
    <br/>
    <ul>
        <#list tags as item>
        <li>
            <div class="fn-clear first">
                <a href="/tag/${item.tagTitle?url('utf-8')}">${item.tagTitle}</a> &nbsp;
                <#if item.tagStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="/admin/tag/${item.oId}" class="fn-right icon-edit" title="${editLabel}"></a>
            </div>
            <div class="fn-clear">
                <#if item.tagIconPath != ''>
                <div class="avatar" style="background-image:url('${staticServePath}/images/tags/${item.tagIconPath}')"></div>
                </#if>
                <span class="tags">${item.tagDescription}</span>
                <span class="fn-right ft-gray">
                    <span class="icon-view" title="${refCountLabel}"></span>
                    ${item.tagReferenceCount} &nbsp;
                    <span class="icon-cmts" title="${commentCountLabel}"></span>
                    ${item.tagCommentCount} &nbsp;
                    <span class="icon-date" title="${createTimeLabel}"></span>
                    ${item.tagCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/tags"/>
</div>
</@admin>
