<#include "macro-settings.ftl">
<@home "invite">
<div class="module">
    <div class="module-header">
        <h2>${inviteTipLabel}</h2>
    </div>
    <div class="module-panel form">
        <br/>
        <input type="text" readonly="" value="${serverScheme}://${serverHost}/register?r=${currentUser.userName}" onclick="this.select()"/>
    </div>
</div>
<#if "2" == allowRegister>
<div class="module">
    <div class="module-header">
        <h2>${buyInvitecodeLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <br/>
        <div id="pointBuyInvitecodeTip" class="tip"></div> <br/>
        <button class="red fn-right" onclick="Settings.pointBuyInvitecode('${csrfToken}')">${confirmExchangeLabel}</button>
    </div>
    <ul>
        <#list invitecodes as invitecode>
        <li>${invitecode.code} ${invitecode.memo}</li>
        </#list>
    </ul>
</div>
<div class="module">
    <div class="module-header">
        <h2>${queryInvitecodeStateLabel}</h2> 
    </div>
    <div class="module-panel form fn-clear">
        <br/>
        <input id="invitecode" type="text" placeholder="${inputInvitecodeLabel}"/><br/><br/>
        <div class="tip" id="invitecodeStateTip"></div><br/>
        <button class="green fn-right" onclick="Settings.queryInvitecode('${csrfToken}')">${submitLabel}</button>
    </div>
</div>
</#if>
</@home>