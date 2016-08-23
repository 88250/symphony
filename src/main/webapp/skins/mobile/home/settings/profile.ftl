<#include "macro-settings.ftl">
<@home "profile">
<div class="module">
    <div class="module-header fn-clear">
        <a rel="nofollow" href="${servePath}/member/${currentUser.userName}" target="_blank">${currentUser.userName}</a>
        <h2>${profilesLabel}</h2>
        <span>(${currentUser.userEmail})</span>
        <a class="ft-red fn-right" href="javascript:Util.logout()">${logoutLabel}</a>
    </div>
    <div class="module-panel form fn-clear">
        <label>${nicknameLabel}</label><br/>
        <input id="userNickname" type="text" value="${currentUser.userNickname}" placeholder="${selfNicknameLabel}"/>

        <label>${selfTagLabel}</label><br/>
        <input id="userTags" type="text" value="${currentUser.userTags}" placeholder="${selfDescriptionLabel}"/>

        <label>URL</label><br/>
        <input id="userURL" type="text" value="${currentUser.userURL}" placeholder="${selfURLLabel}"/>

        <label>${userIntroLabel}</label><br/>
        <textarea id="userIntro" placeholder="${selfIntroLabel}">${currentUser.userIntro}</textarea>
        <div class="fn-hr5"></div>
        <div class="fn-hr5"></div>
        <div class="tip" id="profilesTip"></div>
        <div class="fn-hr5"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('profiles', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>