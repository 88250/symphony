<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "reservedWords">
<div class="content admin">
    <div class="module list">
        <div class="form">
            <button type="button" class="btn red" onclick="window.location = '/admin/add-reserved-word'">${allReservedWordLabel}</button>
        </div>
        <br/>
        <ul>
            <#list words as item>
            <li>
                <div class="fn-clear">
                    ${item.optionValue}
                    <a href="${servePath}/admin/reserved-word/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-icon" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="/admin/tags"/>
    </div>
</div>
</@admin>
