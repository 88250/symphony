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
<@admin "reports">
<div class="content admin">
    <div class="module list">
        <ul class="notification">
            <#list reports as item>
                <li class="<#if item.reportHandled != 0>read</#if>">
                    <div class="fn-flex">
                        <div class="fn-flex-1">
                            ${item.reportUserName}
                            ${reportLabel}
                            ${item.reportDataTypeStr}
                            ${item.reportData}
                            <div class="ft-smaller ft-gray">
                                ${item.reportTypeStr} •
                                ${item.reportTime?string('yyyy-MM-dd HH:mm')}
                                <#if item.reportHandled == 1>
                                • <span class="ft-green">${processLabel}</span>
                                <#elseif item.reportHandled == 2>
                                • <span class="ft-fade">${ignoreLabel}</span>
                                </#if>
                            </div>
                        </div>
                <#if item.reportHandled == 0>
                <div>
                    <button class="mid green" onclick="AdminReportHandled(this, '${item.oId}')">${processLabel}</button>
                    &nbsp;
                    <button class="mid" onclick="AdminReportCancel(this, '${item.oId}')">${ignoreLabel}</button>
                </div>
                </#if>
                    </div>
                    <div class="content-reset">
                        ${item.reportMemo}
                    </div>
                </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/reports"/>
    </div>
</div>
</@admin>
