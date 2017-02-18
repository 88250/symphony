<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userFollowingUserStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow">
    <ol>
        <#list userHomeFollowingUsers as followingUser>
        <li class="fn-flex">
            <a rel="nofollow" title="${followingUser.userName} <#if followingUser.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" href="${servePath}/member/${followingUser.userName}">
                <div class="avatar fn-left" style="background-image:url('${followingUser.userAvatarURL}')"></div>
            </a>
            <div class="fn-flex-1">
                <h3 class="fn-inline">
                    <a rel="nofollow" href="${servePath}/member/${followingUser.userName}" >${followingUser.userName}</a>
                </h3> &nbsp;
                <#if isLoggedIn && (userName != followingUser.userName)> 
                <#if followingUser.isFollowing>
                <button class="red small fn-right" onclick="Util.unfollow(this, '${followingUser.oId}', 'user')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green small fn-right" onclick="Util.follow(this, '${followingUser.oId}', 'user')"> 
                    ${followLabel}
                </button>
                </#if>
                </#if>
                <div>
                    <#if followingUser.userArticleCount == 0>
                    <#if followingUser.userURL != "">
                    <a class="ft-gray" target="_blank" rel="friend" href="${followingUser.userURL?html}">${followingUser.userURL?html}</a>
                    <#else>
                    <span class="ft-gray">${symphonyLabel}</span>
                    ${followingUser.userNo?c}
                    <span class="ft-gray">${numVIPLabel}</span>
                    </#if>
                    <#else>
                    <span class="ft-gray">${articleLabel}</span> ${followingUser.userArticleCount?c} &nbsp;
                    <span class="ft-gray">${tagLabel}</span> ${followingUser.userTagCount?c}
                    </#if>
                </div>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/users"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>