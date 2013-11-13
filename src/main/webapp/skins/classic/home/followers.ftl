<#include "macro-home.ftl">
<@home "followers">
<div class="follow">
    <ol class="fn-clear">
        <#list userHomeFollowerUsers as follower>
        <li<#if follower_index % 2 = 1> class="even"</#if>>
            <img class="avatar" src="${follower.userThumbnailURL}"/>
            <img class="user-online"
                 title="<#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>"
                 src="/images/<#if follower.userOnlineFlag>on<#else>off</#if>line.png" />
            <div>
                <h3>
                    <a rel="nofollow" href="/member/${follower.userName}" >${follower.userName}</a>
                </h3>
                <#if follower.userArticleCount == 0>
                <#if follower.userURL != "">
                <a class="ft-small" target="_blank" rel="friend" href="${follower.userURL}">${follower.userURL}</a>
                <#else>
                <span class="ft-small">Symphony</span>
                ${follower.userNo}
                <span class="ft-small">${noVIPLabel}</span>
                </#if>
                <#else>
                <span class="ft-small">${articleLabel}</span> ${follower.userArticleCount} &nbsp;
                <span class="ft-small">${tagLabel}</span> ${follower.userTagCount}
                </#if>
                <br/>
                <button class="red" onclick="Util.unfollow(this, '${follower.oId}')">${unfollowLabel}</button>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="/member/${user.userName}"/>
</@home>