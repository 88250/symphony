<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "domains">
<div class="list content admin">
    <form method="GET" action="domains" class="form">
        <input name="title" type="text" placeholder="${domainLabel}"/>
        <button type="submit" class="green">${searchLabel}</button>
    <button type="button" class="btn red" onclick="window.location = '/admin/add-domain'">${addDomainLabel}</button>
    </form>
    <br/>
    <ul>
        <#list domains as item>
        <li>
            <div class="fn-clear first">
                <a target="_blank" href="/domain/${item.domainURI}">${item.domainTitle}</a> &nbsp;
                <#if item.domainStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="/admin/domain/${item.oId}" class="fn-right icon-edit" title="${editLabel}"></a>
            </div>
            <div class="fn-clear">
                <#if item.domainIconPath != ''>
                ${item.domainIconPath}
                </#if>
                <span class="domains">${item.domainDescription}</span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/domains"/>
</div>
</@admin>
