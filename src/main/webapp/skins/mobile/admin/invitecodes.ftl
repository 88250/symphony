<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "invitecodes">
<div class="admin">
    <div class="list">
        <#if permissions["icGenIC"].permissionGrant>
        <form method="POST" action="${servePath}/admin/invitecodes/generate" class="form wrapper">
            <input name="quantity" type="number" placeholder="${quantityLabel}" style="width: 30%" />
            <input name="memo" type="text" placeholder="${memoLabel}" autocomplete="off" />
            <button type="submit" class="green">${generateLabel}</button>
        </form>
        </#if>
        <ul>
            <#list invitecodes as item>
            <li>
                <div class="fn-clear">
                    <span class="content-reset"><kbd>${item.code}</kbd></span>&nbsp;
                    <#if 0 == item.status>
                    <span class="ft-blue">${usedLabel}</span>
                    <#elseif 1 == item.status>
                    <font class="ft-green">${unusedLabel}</font>
                    <#else>
                    <font class="ft-red">${stopUseLabel}</font>
                    </#if>
                    <font class="ft-gray">${item.memo}</font>
                    <a href="${servePath}/admin/invitecode/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><span class="icon-edit"></span></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/invitecodes"/>
    </div>
</div>
</@admin>
