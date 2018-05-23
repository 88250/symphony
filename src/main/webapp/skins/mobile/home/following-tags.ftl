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
<#if 0 == user.userFollowingTagStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow">
    <ol>
        <#list userHomeFollowingTags as followingTag>
        <li class="fn-flex">
            <a href="${servePath}/tag/${followingTag.tagURI}">
                <#if "" != followingTag.tagIconPath>
                <div title="${followingTag.tagTitle}" class="avatar fn-left" style="background-image:url('${staticServePath}/images/tags/${followingTag.tagIconPath}')"></div>
                <#else>
                <svg class="avatar"><use xlink:href="#tags"></use></svg>
                </#if>
            </a>
            <div class="fn-flex-1">
                <h3 class="fn-inline">
                    <a href="${servePath}/tag/${followingTag.tagURI}">${followingTag.tagTitle}</a>
                </h3>
                &nbsp;
                <#if isLoggedIn> 
                <#if followingTag.isFollowing>
                <button class="red small fn-right" onclick="Util.unfollow(this, '${followingTag.oId}', 'tag')"> 
                    ${unfollowLabel}
                </button>
                <#else>
                <button class="green small fn-right" onclick="Util.follow(this, '${followingTag.oId}', 'tag')"> 
                    ${followLabel}
                </button>
                </#if>
                </#if>
                <div>
                    <span class="ft-gray">${referenceLabel}</span> ${followingTag.tagReferenceCount?c}
                    <span class="ft-gray">${cmtLabel}</span> ${followingTag.tagCommentCount?c} 
                </div>
            </div>
        </li>
        </#list>
    </ol>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/tags"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>