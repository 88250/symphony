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
        <span class="fn-right">
            <a class="ft-green" href="${servePath}/charge/point">${rechargePointLabel}</a>
        </span>
    </div>
    <div class="module-panel form fn-clear">
         <br/>
        <div id="pointBuyInvitecodeTip" class="tip"></div> <br/>
        <button class="red fn-right" onclick="Settings.pointBuyInvitecode('${csrfToken}')">${confirmExchangeLabel}</button>
    </div>
</div>
</#if>
</@home>