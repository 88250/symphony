<#include "macro-top.ftl">
<@top "checkin">
<div class="content content-reset">
    <h2><span class="ft-gray">♣</span> ${checkinTopLabel}</h2>
</div>
<div class="list top">
    <ul>
        <#list topCheckinUsers as user>
        <li>
            <div class="fn-flex">
                <a rel="nofollow"
                   href="/member/${user.userName}" 
                   title="${user.userName}"><div class="avatar" style="background-image:url('${user.userAvatarURL}-64.jpg?${user.userUpdateTime}')"></div></a>
                <div class="has-view fn-flex-1">
                    <h2>
                        ${user_index + 1}.
                        <a rel="bookmark" href="/member/${user.userName}">${user.userName}</a>
                    </h2>
                    <div class="ft-gray">
                        <#if user.userIntro!="">
                        <div>
                            ${user.userIntro}
                        </div>
                        </#if>
                        <#if user.userURL!="">
                        <div>
                            <a target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
                        </div>
                        </#if>
                        <div>
                            ${symphonyLabel} ${user.userNo?c} ${numVIPLabel},
                            <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
                        </div>
                    </div>
                    <div class="cmts" title="${user.userPoint?c}">
                        ${user.userCurrentCheckinStreak}/<span class="ft-red">${user.userLongestCheckinStreak}</span>
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
    <br/>
</div>
</@top>