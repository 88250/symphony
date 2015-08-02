<#include "macro-notifications.ftl">
<@notifications "commented">
<#if commentedNotifications?size != 0>
<ul class="notification">
    <#list commentedNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
           title="${notification.commentAuthorName}" class="responsive-hide">
            <img class="avatar" src="${notification.commentAuthorThumbnailURL}-64"/>
        </a>
        <div class="fn-flex-1">
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <a target="_blank" rel="nofollow" href="/member/${notification.commentAuthorName}" 
                       title="${notification.commentAuthorName}">
                        <img class="avatar-small responsive-show" src="${notification.commentAuthorThumbnailURL}-64"/>
                    </a>
                    <a rel="bookmark" href="${notification.commentSharpURL}"> ${notification.commentArticleTitle}</a>
                </h2>
                <span class="ft-small">
                    <span class="icon icon-date"></span>
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