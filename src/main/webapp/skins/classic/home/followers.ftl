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
<div class="tabs-sub fn-clear">
    <a pjax-title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a pjax-title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a pjax-title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a pjax-title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a pjax-title="${followersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
    <a pjax-title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/breezemoons"<#if type == "breezemoons"> class="current"</#if>>${breezemoonLabel}</a>
</div>
<#if 0 == user.userFollowerStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list follow">
    <#if userHomeFollowerUsers?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeFollowerUsers as follower>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && follower_index == userHomeFollowerUsers?size - 1>
            class="last"</#if>>
            <div class="fn-flex">
                <a rel="nofollow" class="tooltipped tooltipped-se fn-left" aria-label="${follower.userName} <#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" 
                   href="${servePath}/member/${follower.userName}" >
                    <div class="avatar" style="background-image:url('${follower.userAvatarURL48}')"></div>
                </a>
                <div class="fn-flex-1">
                    <h2 class="fn-inline">
                        <a rel="nofollow" href="${servePath}/member/${follower.userName}" ><#if follower.userNickname != ''>${follower.userNickname}<#else>${follower.userName}</#if></a>
                    </h2>
                    <#if follower.userNickname != ''>
                    <a class='ft-fade' rel="nofollow" href="${servePath}/member/${follower.userName}" >${follower.userName}</a>
                    </#if>
                    <#if isLoggedIn && (currentUser.userName != follower.userName)>
                    <#if follower.isFollowing>
                    <button class="fn-right mid" onclick="Util.unfollow(this, '${follower.oId}', 'user')"> 
                        ${unfollowLabel}
                    </button>
                    <#else>
                    <button class="fn-right mid" onclick="Util.follow(this, '${follower.oId}', 'user')"> 
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
            </div>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/followers" pjaxTitle="${followersLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>