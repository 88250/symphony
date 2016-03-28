<#include "macro-notifications.ftl">
<@notifications "at">
<#if atNotifications?size != 0>
<ul class="notification">
    <#list atNotifications as notification>
    <li class="comment-list-item fn-flex<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}">
            <div class="avatar" style="background-image:url('${notification.thumbnailURL}-64.jpg?${notification.thumbnailUpdateTime?c}')"></div>
        </a>

        <#if !notification.atInArticle>
        <div class="fn-flex-1">
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <#if notification.articleType == 1>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif notification.articleType == 2>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    </#if>
                    <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
                </h2>
                <span class="ft-gray fn-sub">    
                    <span class="icon-date"></span>
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </span>
            </div>
            <div class="content-reset comment">
                ${notification.content}
            </div>
        </div>
        <#else>
        <div class="fn-flex-1 has-view">
            <h2>
                <#if notification.articleType == 1>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif notification.articleType == 2>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    </#if>
                <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
            </h2>
            <p class="ft-gray">
                <#list notification.articleTags?split(",") as articleTag>
                <a rel="tag" class="tag" href="/tag/${notification?url('UTF-8')}">
                    ${articleTag}</a>
                </#list>
                &nbsp; 
                <span class="icon-date"></span>
                ${notification.createTime?string('yyyy-MM-dd HH:mm')}
            </p>
            <#if notification.articleCommentCount != 0>
            <div class="cmts" title="${cmtLabel}">
                <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
            </div>
            </#if>
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