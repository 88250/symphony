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
    <a pjax-title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
    <a pjax-title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a pjax-title="${followersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
    <a pjax-title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/breezemoons"<#if type == "breezemoons"> class="current"</#if>>${breezemoonLabel}</a>
</div>
<#if 0 == user.userFollowingTagStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="follow list">
    <#if userHomeFollowingTags?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeFollowingTags as followingTag>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && followingTag_index == userHomeFollowingTags?size - 1>
             class="last"</#if>>
            <div class="fn-flex">
                <a href="${servePath}/tag/${followingTag.tagURI}">
                    <#if "" != followingTag.tagIconPath>
                    <div class="avatar fn-left ft-gray tooltipped tooltipped-se"  
                         aria-label="${followingTag.tagTitle}" style="background-image:url('${staticServePath}/images/tags/${followingTag.tagIconPath}')"></div>
                    <#else>
                        <div class="tooltipped tooltipped-se fn-left ft-a-title" aria-label="${followingTag.tagTitle}">
                            <svg class="avatar"><use xlink:href="#tags"></use></svg>
                        </div>
                    </#if>
                </a>
                <div class="fn-flex-1">
                    <h2 class="fn-inline">
                        <a href="${servePath}/tag/${followingTag.tagURI}">${followingTag.tagTitle}</a>
                    </h2>
                    &nbsp;
                    <#if isLoggedIn> 
                    <#if followingTag.isFollowing>
                    <button class="fn-right mid" onclick="Util.unfollow(this, '${followingTag.oId}', 'tag')"> 
                        ${unfollowLabel}
                    </button>
                    <#else> 
                    <button class="fn-right mid" onclick="Util.follow(this, '${followingTag.oId}', 'tag')"> 
                        ${followLabel}
                    </button>
                    </#if>
                    </#if>
                    <div>
                        <span class="ft-gray">${referenceLabel}</span> ${followingTag.tagReferenceCount?c} &nbsp;
                        <span class="ft-gray">${cmtLabel}</span> ${followingTag.tagCommentCount?c} 
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/following/tags" pjaxTitle="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>