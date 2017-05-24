<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="list content admin">
    <form method="GET" action="tags" class="form wrapper">
        <input name="title" type="text" placeholder="${tagLabel}"/>
        <button type="submit" class="green">${searchLabel}</button> &nbsp;
        <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-tag'">${addTagLabel}</button>
    </form>
    <ul>
        <#list tags as item>
        <li>
            <div class="fn-clear first">
                <a href="${servePath}/tag/${item.tagURI}">${item.tagTitle}</a> &nbsp;
                <#if item.tagStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="${servePath}/admin/tag/${item.oId}" class="fn-right"><svg><use xlink:href="#edit"></use></svg></a>
            </div>
            <div>
                <#if item.tagIconPath != ''>
                <div class="avatar" style="background-image:url('${staticServePath}/images/tags/${item.tagIconPath}')"></div>
                </#if>
                ${item.tagDescription}
                <div class="ft-gray fn-clear">
                    ${refCountLabel} ${item.tagReferenceCount}</span>
                ${commentCountLabel} ${item.tagCommentCount} &nbsp;</span>
                    ${createTimeLabel} ${item.tagCreateTime?string('yyyy-MM-dd HH:mm')}
                </div>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="${servePath}/admin/tags"/>
</div>
</@admin>
