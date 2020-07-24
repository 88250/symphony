<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-present, b3log.org

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
<@notifications "commented">
<#if commentedNotifications?size != 0>
<ul class="notification">
    <#list commentedNotifications as notification>
    <li class="fn-flex comment-list-item<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.commentAuthorName}">
            <div class="avatar tooltipped tooltipped-se"  
                   aria-label="${notification.commentAuthorName}"
                 style="background-image:url('${notification.commentAuthorThumbnailURL}')"></div>
        </a>
        <div class="fn-flex-1">
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <@icon notification.commentArticlePerfect notification.commentArticleType></@icon>
                    <a rel="bookmark" href="${servePath}${notification.commentSharpURL}">${notification.commentArticleTitle}</a>
                </h2>
                <span class="ft-gray">
                    ${notification.commentCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>

            <div class="vditor-reset comment">
                ${notification.commentContent}
            </div>
        </div>

    </li>
    </#list>
</ul>
<#else>
<div class="no-list">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/commented"/></@notifications>