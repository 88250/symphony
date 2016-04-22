<#include "macro-notifications.ftl">
<@notifications "commented">
<#if commentedNotifications?size != 0>
<ul class="notification">
    <#list commentedNotifications as notification>
    <li class="fn-flex comment-list-item<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
           title="${notification.commentAuthorName}">
            <div class="avatar" style="background-image:url('${notification.commentAuthorThumbnailURL}-64.jpg?${notification.thumbnailUpdateTime?c}')"></div>
        </a>
        <div class="fn-flex-1">
            <div>
                <h2>
                    <#if notification.commentArticleType == 1>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif notification.commentArticleType == 2>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    </#if>
                    <a rel="bookmark" href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                </h2>
                <span class="ft-gray fn-sub">
                    <span class="icon-date"></span>
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
${noMessageLabel}
</#if>

<@pagination url="/notifications/commented"/></@notifications>