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
<#include "../macro-pagination.ftl">
<@admin "reservedWords">
<div class="content admin">
    <div class="module list">
        <#if permissions["rwAddReservedWord"].permissionGrant>
        <div class="form">
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-reserved-word'">${allReservedWordLabel}</button>
        </div>
        </#if>
        <ul>
            <#list words as item>
            <li>
                <div class="fn-clear">
                    ${item.optionValue}
                    <a href="${servePath}/admin/reserved-word/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/tags"/>
    </div>
</div>
</@admin>
