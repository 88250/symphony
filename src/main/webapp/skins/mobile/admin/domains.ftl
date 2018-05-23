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
<@admin "domains">
<div class="admin">
    <div class="list">
        <form method="GET" action="${servePath}/admin/domains" class="form wrapper">
            <input name="title" type="text" placeholder="${domainLabel}"/>
            <button type="submit" class="green">${searchLabel}</button>
            <#if permissions["domainAddDomain"].permissionGrant> &nbsp;
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-domain'">${addDomainLabel}</button>
            </#if>
        </form>
        <ul>
            <#list allDomains as item>
            <li>
                <div class="fn-clear">
                    <a target="_blank" href="${servePath}/domain/${item.domainURI}">${item.domainTitle}</a> &nbsp;
                    <#if item.domainStatus == 0>
                    <span class="ft-gray">${validLabel}</span>
                    <#else>
                    <font class="ft-red">${banLabel}</font>
                    </#if>
                    <a href="${servePath}/admin/domain/${item.oId}" class="fn-right ft-a-title">${editLabel}</a>
                </div>
                <div class="fn-clear">
                    <#if item.domainIconPath != ''>
                    ${item.domainIconPath}
                    </#if>
                    ${item.domainDescription}
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/domains"/>
    </div>
</div>
</@admin>
