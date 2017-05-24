<#include "macro-notifications.ftl">
<@notifications "reply">
<#if replyNotifications?size != 0>
<ul class="notification">
    <#list replyNotifications as notification>
    <li class="fn-flex comment-list-item<#if notification.hasRead> read</#if>">
        <#if "someone" != notification.commentAuthorName>
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.commentAuthorName}" 
           title="${notification.commentAuthorName}"></#if>
            <div class="avatar" style="background-image:url('${notification.commentAuthorThumbnailURL}')"></div>
        <#if "someone" != notification.commentAuthorName></a></#if>
        <div class="fn-flex-1">
            <div>
                <h2>
                    <@icon notification.commentArticlePerfect notification.commentArticleType></@icon>
                    <a rel="bookmark" href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                </h2>
                <span class="ft-gray fn-sub">
                    <svg><use xlink:href="#date"></use></svg>
                    ${notification.commentCreateTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>

            <div class="content-reset comment">
                ${notification.commentContent}
            </div>
        </div>

    </li>
    </#list>
</ul>
<#else>
<div class="fn-hr10"></div>
<div class="ft-center">${noMessageLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/reply"/></@notifications>