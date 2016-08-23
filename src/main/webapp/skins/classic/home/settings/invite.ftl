<#include "macro-settings.ftl">
<@home "invite">
<div class="module">
    <div class="module-header">
        <h2>${inviteTipLabel}</h2>
    </div>
    <div class="module-panel form">
        <br/>
        <input readonly="readonly" type="text" value="${serverScheme}://${serverHost}/register?r=${currentUser.userName}" onclick="this.select()"/>
        <button class="red btn" id="shareClipboard"
                data-clipboard-text="${serverScheme}://${serverHost}/register?r=${currentUser.userName}">${copyLabel}</button>
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
        <div id="pointBuyInvitecodeTip" class="tip"></div> <br/>
        <button class="red fn-right" onclick="Settings.pointBuyInvitecode('${csrfToken}')">${confirmExchangeLabel}</button>
    </div>
</div>
</#if>
</@home>
<script type="text/javascript" src="${staticServePath}/js/lib/zeroclipboard/ZeroClipboard.min.js"></script>
<script>
            ZeroClipboard.config({
                hoverClass: "red-hover",
                swfPath: Label.staticServePath + "/js/lib/zeroclipboard/ZeroClipboard.swf"
            });

            var shareClipboard = new ZeroClipboard(document.getElementById("shareClipboard"));
            shareClipboard.on("ready", function (readyEvent) {
                shareClipboard.on("aftercopy", function (event) {
                    $("#shareClipboard").text('${copiedLabel}');
                    setTimeout(function () {
                        $("#shareClipboard").text('${copyLabel}');
                    }, 2000);
                });
            });
</script>