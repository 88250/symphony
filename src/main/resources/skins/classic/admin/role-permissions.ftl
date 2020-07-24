<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
                                    <label><input name="${permission.oId}" <#if "adminRole" == role.oId>onclick="return false;"</#if> type="checkbox"
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
        <div class="module">
            <div class="module-header">
                <h2 class="ft-red">${removeDataLabel}</h2>
            </div>
            <div class="module-panel form fn-clear form--admin">
                <form action="${servePath}/admin/role/${role.oId}/remove" method="POST"
                      class="fn__flex"
                      onsubmit="return window.confirm('${confirmRemoveLabel}')">
                    <label>
                        <div>${role.roleName}</div>
                    </label>
                    <div>
                        <button type="submit" class="red fn-right">${submitLabel}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</@admin>