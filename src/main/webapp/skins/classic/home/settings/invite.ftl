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
<#include "macro-settings.ftl">
<@home "invite">
    <#if permissions["commonUseIL"].permissionGrant>
        <div class="module">
            <div class="module-header">
                <h2>${inviteTipLabel}</h2>
            </div>
            <div class="module-panel form">
                <input readonly="readonly" type="text" value="${serverScheme}://${serverHost}${contextPath}/register?r=${currentUser.userName}" onclick="this.select()"/>
                <button class="btn" id="shareClipboard">${copyLabel}</button>
            </div>
        </div>
    </#if>
<#if "2" == allowRegister>
    <#if permissions["commonExchangeIC"].permissionGrant>
        <div class="module">
            <div class="module-header">
                <h2>${buyInvitecodeLabel}</h2>
            </div>
            <div class="module-panel form">
                <div class="fn-clear">
                    <button class="red fn-right" onclick="Settings.pointBuyInvitecode('${csrfToken}')">${confirmExchangeLabel}</button>
                </div>
                <div class="list" id="pointBuyInvitecode">
                    <ul>
                        <#list invitecodes as invitecode>
                        <li class="content-reset"><code>${invitecode.code}</code> ${invitecode.memo}</li>
                        </#list>
                    </ul>
                </div>

                <div id="pointBuyInvitecodeTip" class="tip"></div>
            </div>
        </div>
    </#if>
<div class="module">
    <div class="module-header">
        <h2>${queryInvitecodeStateLabel}</h2> 
    </div>
    <div class="module-panel form fn-clear">
        <input id="invitecode" type="text" placeholder="${inputInvitecodeLabel}"/><br/><br/>
        <div class="tip" id="invitecodeStateTip"></div><br/>
        <button class="fn-right" onclick="Settings.queryInvitecode('${csrfToken}')">${submitLabel}</button>
    </div>
</div>
</#if>
</@home>
<script>
    Util.clipboard($('#shareClipboard'), $('#shareClipboard').prev(), function () {
        $('#shareClipboard').text('${copiedLabel}');
        setTimeout(function () {
            $('#shareClipboard').text('${copyLabel}');
        }, 2000);
    });
</script>