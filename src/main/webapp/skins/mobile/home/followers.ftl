<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followers">
<div class="follow">
    <ol>
        <#list userHomeFollowerUsers as follower>
        <li class="fn-clear">
            <a rel="nofollow" title="${follower.userName} <#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" href="/member/${follower.userName}" >
                <div class="avatar fn-left" style="background-image:url('${follower.userAvatarURL}')"></div>
            </a>
            <div class="fn-left">
                <h3 class="fn-inline">
                    <a rel="nofollow" href="/member/${follower.userName}" >${follower.userName}</a>
                </h3> &nbsp;
                <#if isLoggedIn && (userName != follower.userName)> 
                <#if follower.isFollowing>
                <button class="red small" onclick="Util.unfollow(this, '${follower.oId}', 'user')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green small" onclick="Util.follow(this, '${follower.oId}', 'user')"> 
                    ${followLabel}
                </button>
                </#if>
                </#if>
                <div>
                    <#if follower.userArticleCount == 0>
                    <#if follower.userURL != "">
                    <a class="ft-gray" target="_blank" rel="friend" href="${follower.userURL?html}">${follower.userURL?html}</a>
                    <#else>
                    <span class="ft-gray">${symphonyLabel}</span>
                    ${follower.userNo?c}
                    <span class="ft-gray">${numVIPLabel}</span>
                    </#if>
                    <#else>
                    <span class="ft-gray">${articleLabel}</span> ${follower.userArticleCount?c} &nbsp;
                    <span class="ft-gray">${tagLabel}</span> ${follower.userTagCount?c}
                    </#if>
                </div>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/followers"/>
</@home>