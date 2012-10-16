<div style="position:relative">
    <img class="avatar-big" title="${user.userName}" src="${user.userThumbnailURL}" />
    <img class="user-online" title="online" src="/images/offline.png" />
    <div>
        <div class="user-name">
            <a href="/${user.userName}">${user.userName}</a>
        </div>
        <div class="user-info">
            <span class="ft-small">Symphony</span> ${user.userNo} <span class="ft-small">${noVIPLabel}</span>
        </div>
        <#if user.userURL!="">
        <div class="user-info">
            <span class="ft-small">URL </span><a href="">${user.userURL}</a>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-small">${joinTimeLabel} </span>${user.userCreateTime?string('yyyy-MM-dd')}
        </div>
        <#if user.userIntro!="">
        <div>
            <span class="ft-small">${introLabel} </span>
            <br/>
            ${user.userIntro}
        </div>
        </#if>
    </div>
    <ul class="status fn-clear">
        <li>
            <strong>12</strong>
            <span class="ft-small">${tagLabel}</span>
        </li>
        <li>
            <strong>12</strong>
            <span class="ft-small">${articleLabel}</span>
        </li>
        <li>
            <strong>12</strong>
            <span class="ft-small">${cmtLabel}</span>
        </li>
    </ul>
</div>