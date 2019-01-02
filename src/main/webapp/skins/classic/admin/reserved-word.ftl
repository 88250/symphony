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
<@admin "reservedWords">
<div class="content">
    <div class="module">
        <div class="module-header">
            <h2>${unmodifiableLabel}</h2>
        </div>
        <div class="module-panel form form--admin fn-clear">
            <label>
                <div>Id</div>
                <input onfocus="this.select()" type="text" id="oId" value="${word.oId}" readonly="readonly"/>
            </label>
        </div>
    </div>

    <#if permissions["rwUpdateReservedWordBasic"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2>${modifiableLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/reserved-word/${word.oId}" method="POST" class="fn__flex">
                <label>
                    <div>${reservedWordLabel}</div>
                    <input type="text" id="optionValue" name="optionValue" value="${word.optionValue}"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="green fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>

    <#if permissions["rwRemoveReservedWord"].permissionGrant>
    <div class="module">
        <div class="module-header">
            <h2 class="ft-red">${removeDataLabel}</h2>
        </div>
        <div class="module-panel form fn-clear form--admin">
            <form action="${servePath}/admin/remove-reserved-word" class="fn__flex" method="POST"
                  onsubmit="return window.confirm('${confirmRemoveLabel}')">
                <label>
                    <div>Id</div>
                    <input type="text" id="id" name="id" value="${word.oId}" readonly class="input--admin-readonly"/>
                </label>
                <div>
                    &nbsp; &nbsp;
                    <button type="submit" class="red fn-right btn--admin">${submitLabel}</button>
                </div>
            </form>
        </div>
    </div>
    </#if>
</div>
</@admin>