<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "users">
<div class="list content admin">
    <ul>
        <#list users as item>
        <li class="fn-clear">
            <img class="avatar-small" title="${item.userName}" 
                 src="${item.userThumbnailURL}">
            <a href="/member/${item.userName}">${item.userName}</a>
            <a href="/admin/user/${item.oId}" class="fn-right edit" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a> &nbsp;
             <#if item.userStatus == 0>
            ${validLabel}
            <#else>
            <font style="color: red">
            ${banLabel}
            </font>
            </#if>
            <br/>
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
        </li>
        </#list>
    </ul>
    <@pagination url="/admin/users"/>
</div>
</@admin>
