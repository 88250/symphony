<#include "macro-admin.ftl">
<@admin "roles">
    <div class="content admin">
        <div class="module list">
            <form class="form">
                <input type="text" placeholder="title" id="rolesTitle">
                <input type="text" placeholder="desc" id="rolesDesc">
                <button class="red">${createLabel}</button>
            </form>
            <ul>
                <#list roles as role>
                    <li class="fn-clear<#if role_index == roles?size - 1> last</#if>">
                        <h2 class="fn-left"><a href="${servePath}/admin/role/${role.oId}/permissions">${role.roleName}</a></h2>
                        <a class="mid btn fn-right" href="${servePath}/admin/role/${role.oId}/permissions">${permissionLabel}</a>
                        <div class="fn-clear"></div>
                        <div class="ft-gray">${role.roleDescription}</div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</@admin>