<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "followers">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowerUsers as follower>
        <li<#if follower_index % 2 = 1> class="even"</#if>>
            <a rel="nofollow" title="${follower.userName}" href="/member/${follower.userName}" >
                <img class="avatar" src="${follower.userAvatarURL}"/>
            </a>
            <img class="user-online"
                 title="<#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>"
                 src="/images/<#if follower.userOnlineFlag>on<#else>off</#if>line.png" />
            <div>
                <h3>
                    <a rel="nofollow" href="/member/${follower.userName}" >${follower.userName}</a>
                </h3>
                <#if follower.userArticleCount == 0>
                <#if follower.userURL != "">
                <a class="ft-small" target="_blank" rel="friend" href="${follower.userURL?html}">${follower.userURL?html}</a>
                <#else>
                <span class="ft-small">Symphony</span>
                ${follower.userNo}
                <span class="ft-small">${numVIPLabel}</span>
                </#if>
                <#else>
                <span class="ft-small">${articleLabel}</span> ${follower.userArticleCount} &nbsp;
                <span class="ft-small">${tagLabel}</span> ${follower.userTagCount}
                </#if>
                <br/>

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
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}/followers"/>
</@home>