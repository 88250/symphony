<#include "macro-settings.ftl">
<@home "invite">
    <#if permissions["commonUseIL"].permissionGrant>
        <div class="module">
            <div class="module-header">
                <h2>${inviteTipLabel}</h2>
            </div>
            <div class="module-panel form">
                <input readonly="readonly" type="text" value="${serverScheme}://${serverHost}/register?r=${currentUser.userName}" onclick="this.select()"/>
                <button class="red btn" id="shareClipboard">${copyLabel}</button>
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