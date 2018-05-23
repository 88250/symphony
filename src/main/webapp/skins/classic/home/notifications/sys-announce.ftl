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
<#include "macro-notifications.ftl">
<@notifications "sysAnnounce">
<#if sysAnnounceNotifications?size != 0>
<ul class="notification">
    <#list sysAnnounceNotifications as notification>
    <li class="<#if notification.hasRead>read</#if>">
        ${notification.description}
        <span class="fn-right ft-gray">${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
    </li>
    </#list>
</ul>
<#else>
<div class="no-list">
${noMessageLabel}
</div>
</#if>

<@pagination url="${servePath}/notifications/sys-announce"/>
</@notifications>