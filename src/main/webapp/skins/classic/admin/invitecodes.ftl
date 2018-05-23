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
<@admin "invitecodes">
<div class="content admin">
    <div class="module list">
        <#if permissions["icGenIC"].permissionGrant>
        <form method="POST" action="${servePath}/admin/invitecodes/generate" class="form">
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
                    <a href="${servePath}/admin/invitecode/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/invitecodes"/>
    </div>
</div>
</@admin>
