<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "users">
<div class="list content admin">
    <form method="GET" action="users" class="form">
        <input name="userNameOrEmail" type="text" placeholder="${userNameLabel}/${userEmailLabel}"/>
        <button type="submit" class="green">${searchLabel}</button>
    </form>

    <ul>
        <#list users as item>
        <li>
            <div class="fn-clear first">
                <img class="avatar-small" title="${item.userName}" 
                     src="${item.userAvatarURL}"> &nbsp;
                <a href="/member/${item.userName}">${item.userName}</a>
                <a href="/admin/user/${item.oId}" class="fn-right icon icon-edit" title="${editLabel}"></a> &nbsp;
                <#if item.userStatus == 0>
                <span class="ft-small">${validLabel}</span>
                <#else>
                <font class="ft-red ft-small">${banLabel}</font>
                </#if>
            </div>
            <div class="fn-clear">
                <span class="icon icon-email" title="${emailLabel}"></span>
                ${item.userEmail} &nbsp;
                <span class="icon icon-userrole" title="${roleLabel}"></span>
                <#if item.userRole == "adminRole">
                ${administratorLabel}
                <#elseif item.userRole == "defaultCommenterRole">
                ${defaultCommenterLabel}
                <#else>
                ${defaultUserLabel}
                </#if>
                <span class="fn-right ft-small">
                    <span class="icon icon-articles" title="${articleCountLabel}"></span>
                    ${item.userArticleCount} &nbsp;
                    <span class="icon icon-cmts" title="${commentCountLabel}"></span>
                    ${item.userCommentCount} &nbsp;
                    <span class="icon icon-date" title="${createTimeLabel}"></span>
                    ${item.userCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/users"/>
</div>
</@admin>
