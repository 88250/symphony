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
                    <li class="last">
                        <div class="fn-flex">
                            <h2 class="fn-flex-1"><a href="#">${role.roleName}</a></h2>
                            <div class="">${role.permissions?size}</div>
                            <div class="">
                                <a class="mid btn" href="${servePath}/admin/role/${role.oId}/permissions">${permissionLabel}</a>
                            </div>
                        </div>
                        <div class="ft-gray">${role.roleDescription}</div>
                    </li>
                </#list>
            </ul>
        </div>
    </div>
</@admin>