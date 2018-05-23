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
<#macro subNav type curDomain>
<div class="main">
    <div class="domains fn-clear fn-flex">
        <#list domains as domain>
        <a href="${servePath}/domain/${domain.domainURI}"<#if curDomain == domain.domainURI> class="selected"</#if>>${domain.domainTitle}</a>
        </#list>
        <#if isLoggedIn && "" != currentUser.userCity>
        <a href="${servePath}/city/my"<#if 'city' == type> class="selected"</#if>>${currentUser.userCity}</a>
        </#if>
        <#if isLoggedIn>
            <a href="${servePath}/watch"<#if selected?? && 'watch' == selected> class="current"</#if>>
            ${followLabel}</a>
        </#if>
    </div>
</div>
</#macro>