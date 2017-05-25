<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "domains">
<div class="admin">
    <div class="list">
        <form method="GET" action="${servePath}/admin/domains" class="form wrapper">
            <input name="title" type="text" placeholder="${domainLabel}"/>
            <button type="submit" class="green">${searchLabel}</button>
            <#if permissions["domainAddDomain"].permissionGrant> &nbsp;
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-domain'">${addDomainLabel}</button>
            </#if>
        </form>
        <ul>
            <#list domains as item>
            <li>
                <div class="fn-clear">
                    <a target="_blank" href="${servePath}/domain/${item.domainURI}">${item.domainTitle}</a> &nbsp;
                    <#if item.domainStatus == 0>
                    <span class="ft-gray">${validLabel}</span>
                    <#else>
                    <font class="ft-red">${banLabel}</font>
                    </#if>
                    <a href="${servePath}/admin/domain/${item.oId}" class="fn-right ft-a-title">${editLabel}</a>
                </div>
                <div class="fn-clear">
                    <#if item.domainIconPath != ''>
                    ${item.domainIconPath}
                    </#if>
                    ${item.domainDescription}
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/domains"/>
    </div>
</div>
</@admin>
