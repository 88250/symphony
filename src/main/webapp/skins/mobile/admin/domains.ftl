<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "domains">
<div class="list content admin">
    <div class="fn-hr10"></div>
    <form method="GET" action="domains" class="form wrapper">
        <input name="title" type="text" placeholder="${domainLabel}"/>
        <button type="submit" class="green">${searchLabel}</button>
    <button type="button" class="btn red" onclick="window.location = '/admin/add-domain'">${addDomainLabel}</button>
    </form>
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
                <div class="avatar" style="background-image:url('${staticServePath}/images/domains/${item.domainIconPath}')"></div>
                </#if>
                ${item.domainDescription}
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/domains"/>
</div>
</@admin>
