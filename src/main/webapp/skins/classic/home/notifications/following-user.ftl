<#include "macro-notifications.ftl">
<@notifications "followingUser">
<#if followingUserNotifications?size != 0>
<ul class="notification">
    <#list followingUserNotifications as notification>
    <li class="fn-clear<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">
            <img class="avatar fn-left" src="${notification.thumbnailURL}"/>
        </a>
        <h2> <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a></h2>
        <div class="content-reset-p ">
            <p class="ft-small">
                <#list notification.articleTags?split(",") as articleTag>
                <a rel="tag" href="/tags/${notification?url('UTF-8')}">
                    ${articleTag}</a><#if notification_has_next>, </#if>
                </#list>
                <span class="ico-date">${notification.createTime?string('yyyy-MM-dd HH:mm')}</span>
            </p>
        </div>
        <#if notification.articleCommentCount != 0>
        <div class="ft-small ico-cmt">
            ${notification.articleCommentCount}
            ${cmtLabel}
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