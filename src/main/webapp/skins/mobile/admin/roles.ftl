<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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