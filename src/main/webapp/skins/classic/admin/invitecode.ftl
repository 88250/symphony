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
<@admin "invitecodes">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin fn__flex">
            <label>
                <div>Id</div>
                <input onfocus="this.select()" type="text" id="oId" value="${invitecode.oId}" readonly="readonly"/>
            </label>

            <label>
                <div>Code</div>
                <input onfocus="this.select()" type="text" id="code" name="code" value="${invitecode.code}"
                       readonly="readonly"/>
            </label>

            <label>
                <div>${invitecodeUserLabel}</div>
                <input onfocus="this.select()" type="text" id="userId" name="userId" value="${invitecode.userId}"
                       readonly="readonly"/>
            </label>

            <label>
                <div>${useTimeLabel}</div>
                <input onfocus="this.select()" type="text" id="useTime" name="useTime"
                       value="<#if invitecode.useTime != 0>${invitecode.useTime?number_to_datetime}</#if>"
                       readonly="readonly"/>
            </label>
        </div>
    </div>
    <#if permissions["icUpdateICBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/invitecode/${invitecode.oId}" method="POST">
                <div class="fn__flex">
                    <label>
                        <div>${statusLabel}</div>
                        <select id="status" name="status">
                            <option value="0"<#if 0 == invitecode.status> selected</#if>>${usedLabel}</option>
                            <option value="1"<#if 1 == invitecode.status> selected</#if>>${unusedLabel}</option>
                            <option value="2"<#if 2 == invitecode.status> selected</#if>>${stopUseLabel}</option>
                        </select>
                    </label>
                </div>
                <div class="fn__flex">
                    <label>
                        <div>${memoLabel}</div>
                        <input type="text" id="memo" name="memo" value="${invitecode.memo}"/>
                    </label>
                </div>
                <br>
                <button type="submit" class="green fn-right">${submitLabel}</button>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>