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
<@admin "users">
<div class="content admin">
    <div class="module list">
        <form method="GET" action="${servePath}/admin/users" class="form">
            <input name="query" type="text" placeholder="${userNameLabel}/${userEmailLabel}/Id"/>
            <button type="submit" class="green">${searchLabel}</button> &nbsp;
            <#if permissions["userAddUser"].permissionGrant>
            <button type="button" class="btn red" onclick="window.location = '${servePath}/admin/add-user'">${addUserLabel}</button>
            </#if>
        </form>
        <ul>
            <#list users as item>
            <li>
                <div class="fn-clear">
                    <div class="avatar-small tooltipped tooltipped-se" aria-label="${item.userName}" 
                         style="background-image:url('${item.userAvatarURL}')"></div> &nbsp;
                    <a href="${servePath}/member/${item.userName}">${item.userName}</a>
                    <a href="${servePath}/admin/user/${item.oId}" class="fn-right tooltipped tooltipped-w ft-a-title" aria-label="${editLabel}"><svg><use xlink:href="#edit"></use></svg></a> &nbsp;
                    <#if item.userStatus == 0>
                    <span class="ft-gray">${validLabel}</span>
                    <#elseif item.userStatus == 1>
                    <span class="ft-red">${banLabel}</span>
                    <#elseif item.userStatus == 2>
                    <span class="ft-red">${notVerifiedLabel}</span>
                    <#else>
                    <span class="ft-red">${invalidLoginLabel}</span>
                    </#if>
                </div>
                <div class="fn-clear">
                    <span class="tooltipped tooltipped-n" aria-label="${emailLabel}"><svg><use xlink:href="#email"></use></svg></span>
                    ${item.userEmail} &nbsp;
                    <span class="tooltipped tooltipped-n" aria-label="${roleLabel}"><svg><use xlink:href="#userrole"></use></svg></span>
                    ${item.roleName}
                    <span class="fn-right ft-gray">
                        <span class="tooltipped tooltipped-n" aria-label="${articleCountLabel}"><svg><use xlink:href="#articles"></use></svg></span>
                        ${item.userArticleCount} &nbsp;
                        <span class="tooltipped tooltipped-n" aria-label="${commentCountLabel}"><svg><use xlink:href="#cmts"></use></svg></span>
                        ${item.userCommentCount} &nbsp;
                        <span class="tooltipped tooltipped-n" aria-label="${createTimeLabel}"><svg><use xlink:href="#date"></use></svg></span>
                        ${item.userCreateTime?string('yyyy-MM-dd HH:mm')}
                    </span>
                </div>
            </li>
            </#list>
        </ul>
        <@pagination url="${servePath}/admin/users"/>
    </div>
</div>
</@admin>
