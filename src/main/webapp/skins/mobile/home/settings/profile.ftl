<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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