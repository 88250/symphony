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
<@notifications "at">
<#if atNotifications?size != 0>
<ul class="notification">
    <#list atNotifications as notification>
    <li class="comment-list-item fn-flex<#if notification.hasRead> read</#if>">
        <#if 2 == notification.dataType>
            <a rel="nofollow" href="${servePath}/member/${notification.authorName}"
               title="${notification.authorName}">
                <div class="avatar" style="background-image:url('${notification.thumbnailURL}')"></div>
            </a>
            <#if !notification.atInArticle>
            <div class="fn-flex-1">
                <div>
                    <h2>
                        <@icon notification.articlePerfect notification.articleType></@icon>
                        <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                    </h2>
                    <span class="ft-gray fn-sub">
                        <svg><use xlink:href="#date"></use></svg>
                        ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
                <div class="vditor-reset comment">
                    ${notification.content}
                </div>
            </div>
            <#else>
            <div class="fn-flex-1">
                <h2>
                    <@icon notification.articlePerfect notification.articleType></@icon>
                    <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                </h2>

                <p class="ft-gray">
                    <#list notification.articleTagObjs as articleTag>
                    <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">
                        ${articleTag.tagTitle}</a>
                    </#list>
                    <br/>
                    <svg><use xlink:href="#date"></use></svg>
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </p>
                <#if notification.articleCommentCount != 0>
                <div class="cmts" title="${cmtLabel}">
                    <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
                </div>
                </#if>
            </div>
            </#if>
        <#else>
            <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.userName}">
            <div class="avatar tooltipped tooltipped-se" aria-label="${notification.userName}" style="background-image:url('${notification.thumbnailURL}')"></div>
            </a>
            <div class="fn-flex-1">
                <div>
                    <h2>${notification.description}</h2>
                    <span class="ft-gray fn-sub">
                        <svg><use xlink:href="#date"></use></svg>
                        ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
            </div>
        </#if>
    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/at"/>
</@notifications>