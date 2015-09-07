<#include "macro-notifications.ftl">
<@notifications "at">
<#if atNotifications?size != 0>
<ul class="notification">
    <#list atNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
           title="${notification.authorName}" class="responsive-hide">
            <img class="avatar" src="${notification.thumbnailURL}-64.jpg?${notification.thumbnailUpdateTime?c}"/>
        </a>

        <#if !notification.atInArticle>
        <div class="fn-flex-1">
            <div class="fn-flex">
                <h2 class="fn-flex-1">
                    <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
                       title="${notification.authorName}">
                        <img class="avatar-small responsive-show" src="${notification.thumbnailURL}-64.jpg?${notification.thumbnailUpdateTime?c}"/>
                    </a>
                    <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a></h2>
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
                <a target="_blank" rel="nofollow" href="/member/${notification.authorName}" 
                   title="${notification.authorName}">
                    <img class="avatar-small responsive-show" src="${notification.thumbnailURL}-64.jpg?${notification.thumbnailUpdateTime}"/>
                </a>
                <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a></h2>
            <div class="comment">
                <p class="ft-gray">
                    <#list notification.articleTags?split(",") as articleTag>
                    <a rel="tag" class="tag" href="/tags/${notification?url('UTF-8')}">
                        ${articleTag}</a>
                    </#list>
                    &nbsp; 
                    <span class="icon-date"></span>
                    ${notification.createTime?string('yyyy-MM-dd HH:mm')}
                </p>
            </div>  
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