<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<#if 0 == user.userFollowerStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow">
    <ol>
        <#list userHomeFollowerUsers as follower>
        <li class="fn-clear">
            <a rel="nofollow" title="${follower.userName} <#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" href="${servePath}/member/${follower.userName}" >
                <div class="avatar fn-left" style="background-image:url('${follower.userAvatarURL48}')"></div>
            </a>
            <div class="fn-left">
                <h3 class="fn-inline">
                    <a rel="nofollow" href="${servePath}/member/${follower.userName}" >${follower.userName}</a>
                </h3> &nbsp;
                <#if isLoggedIn && (currentUser.userName != follower.userName)>
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
<@pagination url="${servePath}/member/${user.userName}/followers"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>