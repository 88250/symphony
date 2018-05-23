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
<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "tags">
<div class="list content admin">
    <form method="GET" action="tags" class="form wrapper">
        <input name="title" type="text" placeholder="${tagLabel}"/>
        <button type="submit" class="green">${searchLabel}</button> &nbsp;
        <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-tag'">${addTagLabel}</button>
    </form>
    <ul>
        <#list tags as item>
        <li>
            <div class="fn-clear first">
                <a href="${servePath}/tag/${item.tagURI}">${item.tagTitle}</a> &nbsp;
                <#if item.tagStatus == 0>
                <span class="ft-gray">${validLabel}</span>
                <#else>
                <font class="ft-red">${banLabel}</font>
                </#if>
                <a href="${servePath}/admin/tag/${item.oId}" class="fn-right"><svg><use xlink:href="#edit"></use></svg></a>
            </div>
            <div>
                <#if item.tagIconPath != ''>
                <div class="avatar" style="background-image:url('${staticServePath}/images/tags/${item.tagIconPath}')"></div>
                </#if>
                ${item.tagDescription}
                <div class="ft-gray fn-clear">
                    ${refCountLabel} ${item.tagReferenceCount}</span>
                ${commentCountLabel} ${item.tagCommentCount} &nbsp;</span>
                    ${createTimeLabel} ${item.tagCreateTime?string('yyyy-MM-dd HH:mm')}
                </div>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="${servePath}/admin/tags"/>
</div>
</@admin>
