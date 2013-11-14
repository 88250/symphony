<#include "macro-notifications.ftl">
<@notifications "commented">
<#if commentedNotifications?size != 0>
<ul class="notification">
    <#list commentedNotifications as notification>
    <li class="fn-clear<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
           title="${notification.commentAuthorName}">
            <img class="avatar fn-left" src="${notification.commentAuthorThumbnailURL}"/>
        </a>
        <h2 class="fn-inline">
             <a rel="bookmark" href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
        </h2>
        <span class="ico-date ft-small fn-right">${notification.commentCreateTime?string('yyyy-MM-dd HH:mm')}</span>
        <div class="content-reset content-reset-p ">
            ${notification.commentContent}
        </div>
    </li>
    </#list>
</ul>
<#else>
${noMessageLabel}
</#if>

<@pagination url="/notifications/commented"/></@notifications>