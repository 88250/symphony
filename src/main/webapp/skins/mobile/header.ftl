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
<div class="nav"> 
    <div class="wrapper fn-clear">
        <div class="head-fn fn-left">
            <h1>
                <a href="${servePath}" aria-label="${symphonyLabel}">
                    <svg><use xlink:href="#logo"></use></svg>
                </a>
            </h1>
        </div>

        <div class="fn-right user-nav">
            <#if isLoggedIn>
            <#if permissions["menuAdmin"].permissionGrant>
            <a href="${servePath}/admin" title="${adminLabel}" class="last"><svg><use xlink:href="#userrole"></use></svg></a>
            </#if>
            <a href="${servePath}/member/${currentUser.userName}" title="Home" class="<#if 'adminRole' != userRole>last </#if>nav-avatar">
                <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL20}')"></span>
            </a>
            <a href="${servePath}/activities" title="${activityLabel}"><svg><use xlink:href="#flag"></use></svg></a>
            <#if permissions["commonAddArticle"].permissionGrant>
                <a href="${servePath}/pre-post" title="${addArticleLabel}"><svg><use xlink:href="#addfile"></use></svg></a>
            </#if>
            <a id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
            <a href="${servePath}/recent"><svg><use xlink:href="#refresh"></use></svg></a>
            <#else>
                <a href="javascript: Util.goLogin();" title="${loginLabel}" class="unlogin">${loginLabel}</a>
                <a id="aRegister" href="javascript:Util.goRegister()" class="last ft-blue unlogin"
                 title="${registerLabel}">${registerLabel}</a>
            </#if>
        </div>
    </div>
</div>
