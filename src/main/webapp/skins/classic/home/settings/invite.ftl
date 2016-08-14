<#include "macro-settings.ftl">
<@home "invite">
<div class="module">
    <div class="module-header">
        <h2>${inviteTipLabel}</h2>
    </div>
    <div class="module-panel form">
        <br/>
        <input type="text" value="${serverScheme}://${serverHost}/register?r=${currentUser.userName}" onclick="this.select()"/>
        <button class="red" id="shareClipboard"
                data-clipboard-text="${serverScheme}://${serverHost}/register?r=${currentUser.userName}">${copyLabel}</button>
    </div>
</div>
</@home>
<script type="text/javascript" src="${staticServePath}/js/lib/zeroclipboard/ZeroClipboard.min.js"></script>
<script>
            var shareClipboard = new ZeroClipboard(document.getElementById("shareClipboard"));
            shareClipboard.on("ready", function (readyEvent) {
                shareClipboard.on("aftercopy", function (event) {
                    $("#shareClipboard").text('${copiedLabel}');
                });
            });
</script>