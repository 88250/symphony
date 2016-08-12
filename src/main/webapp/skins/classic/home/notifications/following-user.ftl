<#include "macro-notifications.ftl">
<@notifications "followingUser">
<#if followingUserNotifications?size != 0>
<ul class="notification">
    <#list followingUserNotifications as notification>
    <li class="fn-flex<#if notification.hasRead> read</#if>">
        <#if "someone" != notification.authorName>
        <a target="_blank" rel="nofollow" href="${servePath}/member/${notification.authorName}"></#if>
            <div class="avatar tooltipped tooltipped-se" 
           aria-label="${notification.authorName}"
           style="background-image:url('${notification.thumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></div>
        <#if "someone" != notification.authorName></a></#if>
        <div class="fn-flex-1 has-view">
            <h2>
                <#if 1 == notification.aticlePerfect>
                <span class="tooltipped tooltipped-w" aria-label="${perfectLabel}"><svg height="20" viewBox="3 0 11 12" width="14">${perfectIcon}</svg></span>
                </#if>
                <#if notification.articleType == 1>
                <span class="tooltipped tooltipped-w" aria-label="${discussionLabel}"><span class="icon-locked"></span></span>
                <#elseif notification.articleType == 2>
                <span class="tooltipped tooltipped-w" aria-label="${cityBroadcastLabel}"><span class="icon-feed"></span></span>
                <#elseif 3 == notification.articleType>
                <span class="tooltipped tooltipped-w" aria-label="${thoughtLabel}"><span class="icon-video"></span></span>
                </#if>
                <a rel="bookmark" href="${notification.url}"> ${notification.articleTitle}</a>
            </h2>
            <div class="ft-gray">
                <#list notification.articleTags?split(",") as articleTag>
                <a class="tag" rel="tag" href="${servePath}/tag/${articleTag?url('UTF-8')}">
                    ${articleTag}</a>
                </#list> &nbsp;
                <span class="icon-date"></span>
                ${notification.createTime?string('yyyy-MM-dd HH:mm')}
            </div> 
        </div>
        <#if notification.articleCommentCount != 0>
        <div class="cmts tooltipped tooltipped-w" aria-label="${cmtLabel}${quantityLabel}">
            <a class="count ft-gray" href="${notification.url}">${notification.articleCommentCount}</a>
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