<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "reservedWords">
<div class="list content admin">
    <div class="fn-hr10"></div>
    <div class="form wrapper">
        <button type="button" class="btn red" onclick="window.location = '/admin/add-reserved-word'">${allReservedWordLabel}</button>
    </div>
    <ul>
        <#list words as item>
        <li>
            <div class="fn-clear">
                ${item.optionValue}

                <a href="/admin/reserved-word/${item.oId}" class="fn-right icon-edit" title="${editLabel}"></a>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/tags"/>
</div>
</@admin>
