<#include "macro-settings.ftl">
<@home "b3">
<div class="module">
    <div class="module-header">
        <a href="https://hacpai.com/article/1440820551723" target="_blank">${symphonyLabel}${symClientSyncLabel}</a>
    </div>
    <div class="module-panel form fn-clear">
        <label>B3log Key</label>
        <input id="soloKey" type="text" value="${currentUser.userB3Key}" /> 

        <label>${clientArticleLabel}</label>
        <input id="soloPostURL" type="text" value="${currentUser.userB3ClientAddArticleURL}" />

        <label>${clientUpdateArticleLabel}</label>
        <input id="soloUpdateURL" type="text" value="${currentUser.userB3ClientUpdateArticleURL}" />

        <label>${clientCmtLabel}</label>
        <input id="soloCmtURL" type="text" value="${currentUser.userB3ClientAddCommentURL}" />

        <label>
            ${syncWithSymphonyClientLabel}
            <input id="syncWithSymphonyClient" <#if currentUser.syncWithSymphonyClient> checked="checked"</#if> type="checkbox" /> 
        </label>

        <br/><br/>
        <div class="fn-clear"></div>
        <div id="syncb3Tip" class="tip"></div><br/>
        <button class="green fn-right" onclick="Settings.update('sync/b3', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>