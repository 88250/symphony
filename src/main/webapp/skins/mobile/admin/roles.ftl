<#include "macro-admin.ftl">
    <@admin "roles">
    <div class="admin">
        <div class="list">
            <form class="form wrapper" action="${servePath}/admin/role" method="POST">
                <input name="roleName" type="text" id="rolesTitle" placeholder="${roleNameLabel}">
                <input name="roleDescription" type="text" id="rolesDesc" placeholder="${roleDescLabel}">
                <button class="red">${createLabel}</button>
            </form>
            <ul>
                <#list roles as role>
                    <li class="fn-clear<#if role_index == roles?size - 1> last</#if>">
                        <h2 class="fn-left"><a
                                href="${servePath}/admin/role/${role.oId}/permissions">${role.roleName}</a></h2>
                        <a class="mid btn fn-right" href="${servePath}/admin/role/${role.oId}/permissions">${permissionLabel}</a>
                        <div class="fn-clear">&nbsp;&nbsp;<span class="ft-smaller ft-fade">${userCountLabel} ${role.roleUserCount}${commaLabel}${permissionCountLabel} ${role.permissions?size}</span></div>
                        <div class="ft-gray">${role.roleDescription}</div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</@admin>