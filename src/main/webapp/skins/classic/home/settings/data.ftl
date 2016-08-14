<#include "macro-settings.ftl">
<@home "data">
<div class="module">
    <div class="module-header fn-clear">
       <span class="fn-right ft-green">
            ♦ <a class="ft-green" href="${servePath}/charge/point">${chargePointLabel}</a>
        </span>
    </div>
    <div class="module-panel form fn-clear">
        <br/>
        ${dataExportTipLabel}<br/><br/>
        <button class="green fn-right" onclick="Settings.exportPosts()">${submitLabel}</button>
    </div>
</div>
</@home>