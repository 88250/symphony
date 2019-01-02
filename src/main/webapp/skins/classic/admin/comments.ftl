<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2019, b3log.org & hacpai.com

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
<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "comments">
<div class="content admin">
    <div class="module list">
        <ul>
            <#list comments as item>
            <li>
                <div class="fn-flex">
                    <div class="avatar tooltipped tooltipped-w" style="background-image:url('${item.commentAuthorThumbnailURL}')"
                         aria-label="${item.commentAuthorName}"></div>
                    <div class="fn-flex-1">
                        <h2>
                            <a href="${servePath}${item.commentSharpURL}">${item.commentArticleTitle}</a>
                            <span class="ft-smaller ft-gray">
                            <#if item.commentStatus == 0>${validLabel}<#else>
                            <font class="ft-red">${banLabel}</font>
                            </#if> • ${item.commentCreateTime?string('yyyy-MM-dd HH:mm')}
                            </span>
                        </h2>

                        <div class="content-reset">
                            ${item.commentContent}
                        </div>
                    </div>
                    <a href="${servePath}/admin/comment/${item.oId}" class="fn-right tooltipped tooltipped-e ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/comments"/>
    </div>
</div>
</@admin>