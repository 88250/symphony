<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followingUsers">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowingUsers as followingUser>
        <li<#if followingUser_index % 2 = 1> class="even"</#if>>
            <a rel="nofollow" title="${followingUser.userName} <#if followingUser.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" href="/member/${followingUser.userName}">
                <img class="avatar" src="${followingUser.userAvatarURL}"/>
            </a>
            <div>
                <h3>
                    <a rel="nofollow" href="/member/${followingUser.userName}" >${followingUser.userName}</a>
                </h3>
                <#if followingUser.userArticleCount == 0>
                <#if followingUser.userURL != "">
                <a class="ft-small" target="_blank" rel="friend" href="${followingUser.userURL?html}">${followingUser.userURL?html}</a>
                <#else>
                <span class="ft-small">Symphony</span>
                ${followingUser.userNo}
                <span class="ft-small">${numVIPLabel}</span>
                </#if>
                <#else>
                <span class="ft-small">${articleLabel}</span> ${followingUser.userArticleCount?c} &nbsp;
                <span class="ft-small">${tagLabel}</span> ${followingUser.userTagCount?c}
                </#if>
                <br/>

                <#if isLoggedIn && (userName != followingUser.userName)> 
                <#if followingUser.isFollowing>
                <button class="red small" onclick="Util.unfollow(this, '${followingUser.oId}', 'user')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green small" onclick="Util.follow(this, '${followingUser.oId}', 'user')"> 
                    ${followLabel}
                </button>
                </#if>
                </#if>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/following/users"/>
</@home>