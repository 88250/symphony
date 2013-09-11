<#include "macro-notifications.ftl">
<@notifications "at">
<#if atNotifications?size != 0>
<ul class="notification">
    <#list atNotifications as notification>
    <li class="fn-clear<#if notification.hasRead> read</#if>">
        <img class="avatar fn-left" src="${notification.thumbnailURL}"/>
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">${notification.authorName}</a>
        <span class="ft-small">
            <#if !notification.atInArticle>
            ${commentOnLabel}${cmtLabel}${articleLabel}
            <#else>
            ${commentOnLabel}${articleLabel}
            </#if>
        </span>
        <a href="${notification.url}"> ${notification.articleTitle}</a>
        <span class="ft-small">
            <#if !notification.atInArticle>
            ${atYouLabel}
            <#else>
            ${at1YouLabel}
            </#if>
        </span>
        <span class="ico-date ft-small fn-right">${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
        <#if !notification.atInArticle>
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

<@pagination url="/notifications/at"/>
</@notifications>