<#include "macro-home.ftl">
<@home "followingUsers">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowingUsers as followingUser>
        <li<#if followingUser_index % 2 = 1> class="even"</#if>>
            <img class="avatar" src="${followingUser.userThumbnailURL}"/>
            <img class="user-online"
                 title="<#if followingUser.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>"
                 src="/images/<#if followingUser.userOnlineFlag>on<#else>off</#if>line.png" />
            <div>
                <h3>
                    <a rel="nofollow" href="/member/${followingUser.userName}" >${followingUser.userName}</a>
                </h3>
                <#if followingUser.userArticleCount == 0>
                <#if followingUser.userURL != "">
                <a class="ft-small" target="_blank" rel="friend" href="${followingUser.userURL}">${followingUser.userURL}</a>
                <#else>
                <span class="ft-small">Symphony</span>
                ${followingUser.userNo}
                <span class="ft-small">${noVIPLabel}</span>
                </#if>
                <#else>
                <span class="ft-small">${articleLabel}</span> ${followingUser.userArticleCount} &nbsp;
                <span class="ft-small">${tagLabel}</span> ${followingUser.userTagCount}
                </#if>
                <br/>

                <#if isLoggedIn>
                <#if followingUser.isFollowing>
                <button class="red" onclick="Util.unfollow(this, '${followingUser.oId}')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green" onclick="Util.follow(this, '${followingUser.oId}')"> 
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