<#include "macro-notifications.ftl">
<@notifications "reply">
<#if replyNotifications?size != 0>
<ul class="notification">
    <#list replyNotifications as notification>
    <li class="fn-flex comment-list-item<#if notification.hasRead> read</#if>">
        <#if "someone" != notification.commentAuthorName>
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.commentAuthorName}"></#if>
            <div class="avatar tooltipped tooltipped-se"  
                   aria-label="${notification.commentAuthorName}"
                 style="background-image:url('${notification.commentAuthorThumbnailURL}')"></div>
        <#if "someone" != notification.commentAuthorName></a></#if>
        <div class="fn-flex-1">
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <#if 1 == notification.commentArticlePerfect>
                    <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" viewBox="3 3 11 12" width="14">${perfectIcon}</svg></span>
                    </#if>
                    <#if 1 == notification.commentArticleType>
                    <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                    <#elseif 2 == notification.commentArticleType>
                    <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                    <#elseif 3 == notification.commentArticleType>
                    <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                    </#if>
                    <a rel="bookmark" href="${servePath}${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                </h2>
                <span class="ft-gray">
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
<div class="no-list">${chickenEggLabel}</div>
</#if>

<@pagination url="${servePath}/notifications/reply"/></@notifications>