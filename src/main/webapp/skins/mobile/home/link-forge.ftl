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
<#if 0 == user.userForgeLinkStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="wrapper link-forge">
    <div class="link-forge-upload form fn-clear">
        <input type="text" placeholder="${linkForgeTipLabel}" />
        <button class="fn-right green">${submitLabel}</button>
        <div id="uploadLinkTip" class="tip"></div>
    </div>
    <#list tags as tag>
    <div class="module">
        <div class="module-header">
            <h2>
                <a href="${servePath}/tag/${tag.tagURI}">
                    <#if tag.tagIconPath != ''>
                    <span class="avatar-small"  style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')"></span>
                    </#if>
                    ${tag.tagTitle}
                </a>
            </h2>
            <a class="ft-gray fn-right" rel="nofollow" href="javascropt:void(0)">${tag.tagLinksCnt} Links</a>
        </div>
        <div class="module-panel">
            <ul class="module-list">
                <#list tag.tagLinks as link>
                <li>
                <a class="title fn-ellipsis" target="_blank" rel="nofollow" href="${link.linkAddr}">${link.linkTitle}</a>
                </li>
                </#list>
            </ul>
        </div>
    </div>
    </#list>
</div>
<#else>
<div class="module">
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</div>
</#if>
</@home>