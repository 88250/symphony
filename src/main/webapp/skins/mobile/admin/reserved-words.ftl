<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "reservedWords">
<div class="admin">
    <div class="list">
        <#if permissions["rwAddReservedWord"].permissionGrant>
        <div class="form wrapper">
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-reserved-word'">${allReservedWordLabel}</button>
        </div>
        </#if>
        <ul>
            <#list words as item>
            <li>
                <div class="fn-clear">
                    ${item.optionValue}
                    <a href="${servePath}/admin/reserved-word/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/tags"/>
    </div>
</div>
</@admin>
