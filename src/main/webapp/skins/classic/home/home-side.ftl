<div class="ft-center module">
    <div id="avatarURLDom" class="avatar-big" style="background-image:url('${user.userAvatarURL210}')"></div>
    <div>
        <div class="user-name">
            <div id="userNicknameDom"><b>${user.userNickname}</b></div>
            <div class="ft-gray">${user.userName}</div>

            <div>
                <#if isLoggedIn && (currentUser.userName != user.userName)>
                    <button class="green small" onclick="location.href = '${servePath}/post?type=1&at=${user.userName}'">
                        ${privateMessageLabel}
                    </button>
                </#if>
                <#if (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName)) || 0 == user.userOnlineStatus>
                    <span class="tooltipped tooltipped-n" aria-label="<#if user.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>">
                        <span class="<#if user.userOnlineFlag>online<#else>offline</#if>"><img src="${staticServePath}/images/H-20.png" /></span>
                    </span>
                </#if>
                <span class="tooltipped tooltipped-n offline" aria-label="${roleLabel}"> ${user.roleName}</span>
                <#if permissions["userAddPoint"].permissionGrant ||
                        permissions["userAddUser"].permissionGrant ||
                        permissions["userExchangePoint"].permissionGrant ||
                        permissions["userDeductPoint"].permissionGrant ||
                        permissions["userUpdateUserAdvanced"].permissionGrant ||
                        permissions["userUpdateUserBasic"].permissionGrant>
                    <a class="ft-13 tooltipped tooltipped-n ft-a-title" href="${servePath}/admin/user/${user.oId}" aria-label="${adminLabel}"><svg class="fn-text-top"><use xlink:href="#setting"></use></svg></a>
                </#if>
            </div>
            
            <#if isLoggedIn && (currentUser.userName != user.userName)>
            <#if isFollowing>
            <button class="follow" onclick="Util.unfollow(this, '${followingId}', 'user')"> 
                ${unfollowLabel}
            </button>
            <#else>
            <button class="follow" onclick="Util.follow(this, '${followingId}', 'user')"> 
                ${followLabel}
            </button>
            </#if>
            </#if>
        </div>
        
        <div class="user-details">
        <#if user.userIntro!="">
        <div class="user-intro" id="userIntroDom">
            ${user.userIntro}
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-gray">${symphonyLabel}</span>
            ${user.userNo?c}
            <span class="ft-gray">${numVIPLabel}</span>, <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
        <span class="ft-gray">${pointLabel}</span>
        <a href="${servePath}/member/${user.userName}/points" class="tooltipped tooltipped-n" aria-label="${user.userPoint?c}">
                <#if 0 == user.userAppRole>
                0x${user.userPointHex}
                <#else>
                <div class="painter-point" style="background-color: #${user.userPointCC}"></div>
                </#if>
            </a>
        </div>
        <#if "" != user.userTags>
        <div class="user-info">
            <span class="ft-gray">${selfTagLabel}</span> 
            <span id="userTagsDom"><#list user.userTags?split(',') as tag> ${tag?html}<#if tag_has_next>,</#if></#list></span>
        </div>
        </#if>
        <#if "" != user.userCity && 0 == user.userGeoStatus>
        <div class="user-info">
            <span class="ft-gray">${geoLabel}</span> <#if "中国" == user.userCountry>${user.userCity}<#else>${user.userCountry} ${user.userCity}</#if>
        </div>
        </#if>
        <#if user.userURL!="">
        <div class="user-info">
            <a id="userURLDom" target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
        </div>
        </#if>
        <div class="user-info">
            <span class="ft-gray">${joinTimeLabel}</span> ${user.userCreateTime?string('yyyy-MM-dd HH:mm')}
        </div>
        <div class="user-info">
            <span class="ft-gray">${checkinStreakPart0Label}</span>
            ${user.userLongestCheckinStreak?c} 
            <span class="ft-gray">${checkinStreakPart1Label}</span> 
            ${user.userCurrentCheckinStreak?c}
            <span class="ft-gray">${checkinStreakPart2Label}</span>
        </div>
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