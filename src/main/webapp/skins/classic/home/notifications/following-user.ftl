<#include "macro-notifications.ftl">
<@notifications "followingUser">
<#if followingUserNotifications?size != 0>
<ul class="notification">
    <#list followingUserNotifications as notification>
    <li class="fn-clear<#if notification.hasRead> read</#if>">
        <img class="avatar fn-left" src="${notification.thumbnailURL}"/>
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">${notification.authorName}</a>
        <span class="ft-small">
            <#if "article" == notification.type>
            ${createdPostLabel}
            </#if>
        </span>
        <a href="${notification.url}"> ${notification.articleTitle}</a>
        <span class="ico-date ft-small fn-right">${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
        <#if "comment" != notification.type>
        <div class="content-reset content-reset-p ">
            ${notification.content}
        </div>
        </#if>
    </li>
    </#list>
</ul>
<#else>
${noMessageLabel}
</#if>

<@pagination url="/notifications/following-user"/>
</@notifications>