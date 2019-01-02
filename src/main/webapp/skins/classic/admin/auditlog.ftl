<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2019, b3log.org & hacpai.com

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
<#include "../macro-pagination.ftl">
<@admin "auditlog">
<div class="content admin">
    <div class="module list">
        <ul class="notification">
            <#list operations as item>
                <li>
                    ${item.operationContent}
                    <span class="auditlogUA ft-smaller ft-fade" data-ua="${item.operationUA}"></span>
                    <div class="ft-smaller ft-gray">
                        ${item.operationUserName}
                        ${item.operationTime} • ${item.operationIP}
                    </div>
                </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/auditlog"/>
    </div>
</div>
</@admin>
