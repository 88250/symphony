<div class="ft-center">
    <div id="avatarURLDom" class="avatar-big" title="${user.userName}" style="background-image:url('${user.userAvatarURL}-260.jpg?${user.userUpdateTime?c}')"></div>
    <div>
        <div class="user-name">
            <a href="/member/${user.userName}">${user.userName}</a>
            <img title="<#if user.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" src="${staticServePath}/images/<#if user.userOnlineFlag>on<#else>off</#if>line.png" />
            <#if "adminRole" == user.userRole>
            <span class="ft-13 icon-userrole" title="${administratorLabel}"></span>
            </#if>
            <#if isAdminLoggedIn>
            <a class="ft-13 icon-setting" href="/admin/user/${user.oId}" title="${adminLabel}"></a>
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
            <button class="green small" onclick="location.href='/post?type=1&at=${user.userName}'"> 
                ${privateMessageLabel}
            </button>
            </#if>
        </div>

        <#if user.userIntro!="">
        <div class="user-intro" id="userIntroDom">
            ${user.userIntro}
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-gray">${symphonyLabel}</span>
            ${user.userNo?c}
            <span class="ft-gray">${numVIPLabel}</span>, <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
        </div>
        <#if "" != user.userTags>
        <div class="user-info">
            <span class="ft-gray">${selfTagLabel}</span> <#list user.userTags?split(',') as tag> ${tag?html}<#if tag_has_next>,</#if></#list>
        </div>
        </#if>
        <#if "" != user.userCity && 0 == user.userGeoStatus>
        <div class="user-info">
            <span class="ft-gray">${geoLable}</span> <#if "中国" == user.userCountry>${user.userCity}<#else>${user.userCountry} ${user.userCity}</#if>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-gray">${pointLabel}</span>
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
            <a id="userURLDom" target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-gray">${joinTimeLabel}</span> ${user.userCreateTime?string('yyyy-MM-dd HH:mm:ss')}
        </div>
        <div class="user-info">
            <span class="ft-gray">${checkinStreakPart0Label}</span>
            ${user.userLongestCheckinStreak?c} 
            <span class="ft-gray">${checkinStreakPart1Label}</span> 
            ${user.userCurrentCheckinStreak?c}
            <span class="ft-gray">${checkinStreakPart2Label}</span>
        </div>

        <ul class="status fn-flex">
            <li>
                <strong>${user.userTagCount?c}</strong>
                <span class="ft-gray">${tagLabel}</span>
            </li>
            <li>
                <strong>${user.userArticleCount?c}</strong>
                <span class="ft-gray">${articleLabel}</span>
            </li>
            <li>
                <strong>${user.userCommentCount?c}</strong>
                <span class="ft-gray">${cmtLabel}</span>
            </li>
        </ul>
    </div>
</div>