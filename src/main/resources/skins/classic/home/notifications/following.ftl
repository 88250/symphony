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
<@notifications "following">
<#if followingNotifications?size != 0>
<ul class="notification">
    <#list followingNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.authorName}">
            <div class="avatar tooltipped tooltipped-se" 
           aria-label="${notification.authorName}"
           style="background-image:url('${notification.thumbnailURL}')"></div>
        </a>

        <#if notification.isComment>
            <div class="fn-flex-1">
                <div class="fn-flex">
                    <h2 class="fn-flex-1">
                        <@icon notification.articlePerfect notification.articleType></@icon>
                        <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                    </h2>
                    <span class="ft-gray">
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </span>
                </div>
                <div class="vditor-reset comment">
                    ${notification.content}
                </div>
            </div>
        <#else>
            <div class="fn-flex-1 has-view">
                <h2>
                    <@icon notification.articlePerfect notification.articleType></@icon>
                    <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>&nbsp;<#if 20 == notification.dataType><span class="ft-green ft-smaller">${updateArticleLabel}</span></#if>
                </h2>
                <div class="ft-fade ft-smaller">
                    <#list notification.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list> •
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </div>
            </div>
            <#if notification.articleCommentCount != 0>
            <div class="cmts tooltipped tooltipped-w" aria-label="${cmtLabel}${quantityLabel}">
                <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
            </div>
            </#if>
        </#if>
    </li>
    </#list>
</ul>
<#else>
<div class="no-list">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/following"/>
</@notifications>