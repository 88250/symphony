<div class="fn-relative ft-center">
    <img class="avatar-big" title="${user.userName}" src="${user.userAvatarURL}-260" />
    <div>
        <div class="user-name">
            <a href="/member/${user.userName}">${user.userName}</a>
            <img title="<#if user.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" src="/images/<#if user.userOnlineFlag>on<#else>off</#if>line.png" />
            <#if "adminRole" == user.userRole>
            <span class="icon icon-userrole" title="${administratorLabel}"></span>
            </#if>
            <#if isAdminLoggedIn>
            <a class="ft-small icon icon-setting" href="/admin/user/${user.oId}" title="${adminLabel}"></a>
            </#if>
            <#if isLoggedIn && (userName != user.userName)>
            <#if isFollowing>
            <button class="red small" onclick="Util.unfollow(this, '${followingId}', 'user')"> 
                ${unfollowLabel}
            </button>
            <#else>
            <button class="green small" onclick="Util.follow(this, '${followingId}', 'user')"> 
                ${followLabel}
            </button>
            </#if>
            </#if>
        </div>

        <#if user.userIntro!="">
        <div>
            ${user.userIntro}
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-small">Symphony</span>
            ${user.userNo}
            <span class="ft-small">${numVIPLabel}</span>, <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
        </div>
        <#if "" != user.userTags>
        <div class="user-info">
            <span class="ft-small">${selfTagLabel}</span> <#list user.userTags?split(',') as tag> ${tag?html}<#if tag_has_next>,</#if></#list>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-small">${pointLabel}</span>
            <a href="/member/${user.userName}/points" title="${user.userPoint?c}">
                <#if 0 == user.userAppRole>
                0x${user.userPointHex}
                <#else>
                <div class="painter-point" style="background-color: #${user.userPointCC}"></div>
                </#if>
            </a>
        </div>
        <#if user.userURL!="">
        <div class="user-info">
            <span class="ft-small">URL</span>
            <a target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-small">${joinTimeLabel}</span> ${user.userCreateTime?string('yyyy-MM-dd HH:mm:ss')}
        </div>
        <div class="user-info">
            <span class="ft-small">${checkinStreakPart0Label}</span>
            ${user.userLongestCheckinStreak} 
            <span class="ft-small">${checkinStreakPart1Label}</span> 
            ${user.userCurrentCheckinStreak}
            <span class="ft-small">${checkinStreakPart2Label}</span>
        </div>

        <ul class="status fn-flex">
            <li>
                <strong>${user.userTagCount}</strong>
                <span class="ft-small">${tagLabel}</span>
            </li>
            <li>
                <strong>${user.userArticleCount}</strong>
                <span class="ft-small">${articleLabel}</span>
            </li>
            <li>
                <strong>${user.userCommentCount}</strong>
                <span class="ft-small">${cmtLabel}</span>
            </li>
        </ul>
    </div>
</div>