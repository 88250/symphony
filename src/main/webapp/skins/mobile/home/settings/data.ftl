<#include "macro-settings.ftl">
<@home "data">
<div class="module">
    <div class="module-header">${dataExportTipLabel}</div>
    <div class="module-panel form fn-clear">
        <br>
        ${articleLabel}${colonLabel}${currentUser.userArticleCount?c}&nbsp;&nbsp;&nbsp;&nbsp;
        ${cmtLabel}${colonLabel}${currentUser.userCommentCount?c}
        <button class="green fn-right" onclick="Settings.exportPosts()">${exportLabel}</button>
    </div>
</div>
</@home>