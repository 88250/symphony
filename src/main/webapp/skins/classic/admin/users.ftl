<#include "macro-admin.ftl">
<#include "../macro-pagination.ftl">
<@admin "users">
<div class="list content admin">
    <ul>
        <#list users as item>
        <li class="fn-clear" onclick="window.location = '/admin/user/${item.oId}'">
            <img class="avatar-small" title="${item.userName}" 
                 src="${item.userThumbnailURL}">
            ${item.userName}
            <a href="/admin/user/${item.oId}" class="fn-right edit" title="${editLabel}">
                <span class="icon icon-edit"></span>
            </a>
            <br/>
            <span class="icon icon-email" title="${emailLabel}"></span>
            ${item.userEmail} &nbsp;
            <span class="icon icon-userrole" title="${roleLabel}"></span>
            ${item.userRole} 
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
