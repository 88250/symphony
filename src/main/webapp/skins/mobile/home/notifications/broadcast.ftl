<#include "macro-notifications.ftl">
<@notifications "broadcast">
<#if broadcastNotifications?size != 0>
<ul class="notification">
    <#list broadcastNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">
            <div class="avatar" style="background-image:url('${notification.thumbnailURL}-64.jpg?${notification.thumbnailUpdateTime?c}')"></div>
        </a>
        <div class="fn-flex-1 has-view">
            <h2>
                <#if notification.articleType == 1>
                <span class="icon-locked" title="${discussionLabel}"></span>
                <#elseif notification.articleType == 2>
                <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                </#if>
                <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
            </h2>
            <div class="ft-gray">
                <#list notification.articleTags?split(",") as articleTag>
                <a class="tag" rel="tag" href="/tag/${articleTag?url('UTF-8')}">
                    ${articleTag}</a>
                </#list> <br/>
                <span class="icon-date"></span>
                ${notification.createTime?string('yyyy-MM-dd HH:mm')}
            </div> 
        </div>
        <#if notification.articleCommentCount != 0>
        <div class="cmts" title="${cmtLabel}">
            <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
        </div>
        </#if>
    </li>
    </#list>
</ul>
<#else>
${noMessageLabel}
</#if>

<@pagination url="/notifications/broadcast"/>
</@notifications>