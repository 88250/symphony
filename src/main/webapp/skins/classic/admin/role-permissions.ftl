<#include "macro-admin.ftl">
    <@admin "roles">
    <div class="content admin">
        <div class="module">
            <form action="${servePath}/admin/role/${role.oId}/permissions" method="POST">
                <div class="module-header fn-clear">
                    <h2>${role.roleName}</h2>
                </div>
                <div class="module-panel list">
                    <ul>
                        <#list permissionCategories?keys as category>
                            <li class="fn-clear form">
                                <div><big class="ft-gray">${category}</big></div>
                                <#list permissionCategories[category] as permission>
                                    <label><input name="${permission.oId}" type="checkbox"
                                        <#if permission.permissionGrant>checked</#if>
                                        > ${permission.permissionLabel} &nbsp; &nbsp;</label>
                                </#list>
                            </li>
                        </#list>
                        <li class="fn-clear last">
                            <button type="submit" class="green fn-right">${submitLabel}</button>
                        </li>
                    </ul>
                </div>
            </form>
        </div>
    </div>
</@admin>