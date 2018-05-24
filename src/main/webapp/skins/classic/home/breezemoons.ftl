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
    <a pjax-title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers">
       class="current"</#if>>${watchingArticlesLabel}</a>
    <a pjax-title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers">
       class="current"</#if>>${followingUsersLabel}</a>
    <a pjax-title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags">
       class="current"</#if>>${followingTagsLabel}</a>
    <a pjax-title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles">
       class="current"</#if>>${followingArticlesLabel}</a>
    <a pjax-title="${followersLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/followers"<#if type == "followers">
       class="current"</#if>>${followersLabel}</a>
    <a pjax-title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"
       href="${servePath}/member/${user.userName}/breezemoons"<#if type == "breezemoons">
       class="current"</#if>>${breezemoonLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
</div>
    <#if permissions["commonAddBreezemoon"].permissionGrant && isLoggedIn && currentUser.userName == user.userName>
    <div class="list">
        <ul class="form">
            <li>
                <input id="breezemoonInput" type="text" style="padding-right: 89px;">
                <button onclick="Breezemoon.add()" id="breezemoonBtn"  data-csrftoken="${csrfToken}"
                        class="absolute">${breezemoonLabel}</button>
            </li>
        </ul>
    </div>
    </#if>
    <#if 0 == user.userBreezemoonStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
    <div class="list">
        <#if userHomeBreezemoons?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
        </#if>
        <ul id="breezemoonList">
            <#list userHomeBreezemoons as item>
                <li class="fn-flex" id="${item.oId}">
                    <a class="tooltipped tooltipped-n avatar"
                       style="background-image:url('${item.breezemoonAuthorThumbnailURL48}')"
                       rel="nofollow" href="${servePath}/member/${item.breezemoonAuthorName}"
                       aria-label="Vanessa">
                    </a>
                    <div class="fn-flex-1">
                        <div class="ft-fade">
                            <a href="${servePath}/member/${item.breezemoonAuthorName}">${item.breezemoonAuthorName}</a>
                            •
                            <span class="ft-smaller">
                                ${item.timeAgo}
                            </span>
                            <span class="ft-smaller ua" data-ua="${item.breezemoonUA}"></span>

                            <div class="fn-right">
                                <#if isLoggedIn && permissions["commonRemoveBreezemoon"].permissionGrant && item.breezemoonAuthorName == currentUser.userName>
                                <span class="tooltipped tooltipped-n ft-red rm" aria-label="${removeLabel}">
                                    <svg><use xlink:href="#remove"></use></svg>
                                </span>
                                </#if>
                                <#if isLoggedIn && permissions["commonUpdateBreezemoon"].permissionGrant && item.breezemoonAuthorName == currentUser.userName>
                                &nbsp;&nbsp;
                                <span class="tooltipped tooltipped-n ft-a-title edit"
                                      aria-label="${editLabel}">
                                    <svg><use xlink:href="#edit"></use></svg>
                                </span>
                                &nbsp;&nbsp;
                                 </#if>
                            <#--
                            <span class="tooltipped tooltipped-n ft-a-title copy" aria-label="${copyLabel}">
                                <svg><use xlink:href="#articles"></use></svg>
                            </span>
                            -->
                                <textarea
                                        style="position: fixed;left: -10000px;">${servePath}/member/${user.userName}/breezemoons#${item.oId}</textarea>
                            </div>
                        </div>
                        <div class="content-reset">${item.breezemoonContent}</div>
                    </div>
                </li>
            </#list>
        </ul>
    </div>
        <@pagination url="${servePath}/member/${user.userName}/breezemoons" pjaxTitle="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"/>
    <#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
    </#if>
</@home>