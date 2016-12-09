<#include "macro-admin.ftl">
    <@admin "roles">
    <div class="content admin">
        <div class="module list">
            <div class="form fn-clear">
                <h2>${role.roleName}</h2>
            </div>
            <form action="${servePath}/admin/role/${role.oId}/permissions" method="POST">
                <ul>
                    <#list permissionCategories?keys as category>
                        <li class="last">
                            <div class="fn-clear">
                                <h3 class="fn-left">${category}</h3>
                            </div>
                            <#list permissionCategories[category] as permission>
                                <label><input name="${permission.oId}" type="checkbox"
                                    <#if permission.permissionGrant>checked</#if>>${permission.permissionLabel}</label><br>
                            </#list>
                        </li>
                    </#list>
                </ul>
                <div class="form fn-clear">
                    <button type="submit" class="green fn-right">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
</@admin>