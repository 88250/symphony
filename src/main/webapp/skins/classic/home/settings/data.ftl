<#include "macro-settings.ftl">
<@home "data">
<div class="module">
    <div class="module-header">${dataExportTipLabel}</div>
    <div class="module-panel form fn-clear">
        ${articleLabel}${colonLabel}${currentUser.userArticleCount}&nbsp;&nbsp;&nbsp;&nbsp;
        ${cmtLabel}${colonLabel}${currentUser.userCommentCount}
        <button class="green fn-right" onclick="Settings.exportPosts()">${exportLabel}</button>
    </div>
</div>
</@home>